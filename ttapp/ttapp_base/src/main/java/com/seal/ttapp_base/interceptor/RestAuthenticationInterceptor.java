package com.seal.ttapp_base.interceptor;

import com.seal.ttapp_base.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Component
@Slf4j
public class RestAuthenticationInterceptor implements  HandlerInterceptor{
	private static final int TOKEN_UPDATE_INTERVAL_IN_MINUTE = 5;
	
	
    @Autowired
    private AccessTokenService accessTokenService;
    @Autowired
    private UserService userService;
	
    @Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if(request.getRequestURI().contains("emergencyContact") || request.getRequestURI().contains("getLastestVersion")){
            return true;
        }
		if (!(handler instanceof HandlerMethod)) {
			return true;
		}
		log.info("----------进入 Authentication Interceptor");
		String ipAddress = HttpHelper.getIpAddress(request);
        String clientIp;
        if (ipAddress == null) {
            log.error("Unable to get ip address");
            clientIp = null;
        } else {
            clientIp = ipAddress.split("\\s*,\\s*")[0].trim();
        }
        MDC.put(AtConstants.IP_AS_MDC_KEY, clientIp);
        
        if (shouldBypass(handler)) {
            log.info("Bypassing authentication");
            return true;
        }
        // 若为两网互联的转换请求直接通过
        if (request.getHeader("ConversionUser") != null) {
            User user = userService.getUserByUserId(request.getHeader("ConversionUser"));
            this.saveMDC(user, "");
            log.info("Bypassing authentication:Conversion request");
            return true;
        }
            if (request.getHeader("Authorization") != null) {
        	if (!userHasValidToken(request.getHeader("Authorization"))) {
        		log.info("Unauthorized access!");

        		response.setStatus(HttpStatus.SC_UNAUTHORIZED);
        		return true;
        	}
        } else if (StringUtils.equals(request.getHeader("feignSign"), "feign")) {
            log.info("feign request");
            return true;
        }else {
        	if (!userHasValidToken(cookiesToMap(request.getCookies()))) {
        		log.info("Unauthorized access!");

        		response.setStatus(HttpStatus.SC_UNAUTHORIZED);
        		return true;
        	}
        }
        
//		response.sendError(HttpStatus.SC_UNAUTHORIZED, "无权限");
		return true;
	}
	
	private Map<String, Cookie> cookiesToMap(Cookie[] cookies) {
		HashMap<String, Cookie> cookieMap = new HashMap<>();
		if (cookies==null ||cookies.length == 0) {
			return cookieMap;
		}
		for(Cookie cookie : cookies) {
			cookieMap.put(cookie.getName(), cookie);
		}
		return cookieMap;
	}
	
	
	private boolean shouldBypass(Object handler) {
		HandlerMethod handlerMethod = (HandlerMethod)handler;

        List<Annotation> annotations = new ArrayList<>();
        Class<?> clazz = handlerMethod.getBean().getClass();
        if (clazz != null) {
            annotations.addAll(Arrays.asList(clazz.getAnnotations()));
        }
        Method method = handlerMethod.getMethod();
        if (method != null) {
            annotations.addAll(Arrays.asList(method.getAnnotations()));
        }
        for (Annotation an : annotations) {
            if (an instanceof NotAuthenticated) {
                return true;
            }
        }
        try {
            InitialContext initialContext = new InitialContext();
            String environment = (String) initialContext.lookup("java:comp/env/fastop");
            if (!environment.equals("test")) {
                return false;
            } else {
                boolean enableAuthentication = System.getProperties().containsKey("enableAuthentication")
                        && System.getProperty("enableAuthentication").equals("true");
                return !enableAuthentication;
            }
        } catch (NamingException e) {
            return false;
        }
    }
	
	private boolean userHasValidToken(String token) {
        AccessToken tokenInDB = accessTokenService.getTokenByTokenValue(token);
        if (tokenInDB == null) {
            log.info("No token found in DB");
            return false;  // no token in DB
        } else {
            try {
                User user = userService.getUserByAccessToken(tokenInDB.getToken());
                saveMDC(user, token);
                if (user.getLocked()) {
                    deleteToken(tokenInDB);
                    log.info("User got locked.");
                    return false;  // user got locked
                }
                LocalDateTime localDateTimeOfTokenInDB = LocalDateTime.ofInstant(tokenInDB.getExpireAt().toInstant(), ZoneId.systemDefault());
                if (LocalDateTime.now().isAfter(localDateTimeOfTokenInDB)) {
                    deleteToken(tokenInDB);
                    log.info("Expired token deleted");
                    return false;  // token expired
                } else {
                    updateTokenExpireDate(localDateTimeOfTokenInDB, tokenInDB.getAccessTokenId());
                    return true;
                }
            } catch (Exception e) {
                log.debug("error with token:" + tokenInDB.toString(), e);
                return false;
            }
        }
    }
	
	private boolean userHasValidToken(Map<String, Cookie> cookies) {
        if (cookies.containsKey(NotAuthenticated.COOKIE_NAME_FOR_TOKEN)) {
            Cookie cookie = cookies.get(NotAuthenticated.COOKIE_NAME_FOR_TOKEN);
//            List<String> token = new ArrayList<>();
//            token.add(cookie.getValue());
            return userHasValidToken(cookie.getValue());
        }
        return false;
    }
	
	private void deleteToken(AccessToken tokenInDB) {
        accessTokenService.deleteTokenByTokenValue(tokenInDB.getToken());
    }
	
	private void saveMDC(User user, String accesstoken) {
        MDC.put(AtConstants.USER_AS_TOKEN, accesstoken);
        MDC.put(AtConstants.USER_AS_MDC_KEY, user.getName());
        MDC.put(AtConstants.USERID_AS_MDC_KEY, user.getUserId());
    }
	
    private void updateTokenExpireDate(LocalDateTime expireAt, String tokenId) {
//      List<StrategyRule> rules = getService(IStrategyService.class).get().getStrategyBuilderByStrategyCode(StrategyCode.TIMEOUT_MANAGEMENT).getStrategyRules();
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.MINUTE, AtConstants.defaultIdleTime);
      Date newExpireDate = calendar.getTime();
      if (LocalDateTime.ofInstant(newExpireDate.toInstant(), ZoneId.systemDefault()).isAfter(expireAt.plusMinutes(TOKEN_UPDATE_INTERVAL_IN_MINUTE))) {
          accessTokenService.updateTokenExpireDate(tokenId, newExpireDate);
          log.info("Access token expiration date updated");
      }

  }
	
}
