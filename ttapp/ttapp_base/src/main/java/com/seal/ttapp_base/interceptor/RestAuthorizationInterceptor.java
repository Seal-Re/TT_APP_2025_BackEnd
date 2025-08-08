package com.seal.ttapp_base.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class RestAuthorizationInterceptor implements HandlerInterceptor{
	

	@Autowired
    private UserService userService;
    @Autowired
    private PrivilegeService privilegeService;

//    @Value("${gateway.white}")
//    private List<String> gatewayWhite;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (!(handler instanceof HandlerMethod)) {
			return true;
		}
		log.info("initializing privilege check filter");
		HandlerMethod handlerMethod = (HandlerMethod)handler;
		Method method = handlerMethod.getMethod();
		Annotation annotation = method.getAnnotation(Authorization.class);
		if (annotation == null) {
			return true;
		}

//        if(CollectionUtil.isNotEmpty(gatewayWhite)){
//            for(String item : gatewayWhite){
//                if(pathMatcher.match(item,request.getRequestURI())){
//                    return true;
//                }
//            }
//        }


        // 若为两网互联的转换请求直接通过
        if (request.getHeader("ConversionUser") != null) {
            return true;
        }
        if (StringUtils.equals(request.getHeader("feignSign"), "feign")) {
            return true;
        }
		String accessToken = null;
        if (request.getHeader("Authorization") != null) {
            accessToken = request.getHeader("Authorization");
        } else {
            Map<String, Cookie> cookies = this.cookiesToMap(request.getCookies());
            accessToken = cookies.get(NotAuthenticated.COOKIE_NAME_FOR_TOKEN)== null ? null:cookies.get(NotAuthenticated.COOKIE_NAME_FOR_TOKEN).getValue();
        }
        log.info("accesstoken {} obtained", accessToken);
		
        if (accessToken == null) {
            log.warn("accesstoken is null!");
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            return false;
        }
        PrivilegeCategory target = ((Authorization) annotation).target();
        PrivilegeOption action = ((Authorization) annotation).action();
		
        User user = userService.getUserByAccessToken(accessToken);
        if (user == null) {
            log.warn("Cannot find the corresponding user with accesstoken {}", accessToken);
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            return false;
        }
        log.info("user {} obtained", user);
        List<Role> roles = user.getRoles();
        Role role;
        if (roles != null && roles.size() > 0) {
            roles = user.getRoles();
            role = roles.get(0);
            log.info("role {} obtained", role);
            if ("0".equals(role.getRoleId()) && AtConstants.ADMIN_USER_NAME.equals(user.getName())) {
                log.info("role {}, full permission", role);
                return true;
            }
        } else {
            log.warn("Cannot find the corresponding roles with user {}", user);
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            return false;
        }
        
        log.info("checking userPrivilege ---> accesstoken: {}, category: {}, action:{}", accessToken, target, action);
//        if (!privilegeService.ifUserHasPrivilegeByToken(accessToken, target, action)) {
//        	response.setStatus(HttpStatus.SC_UNAUTHORIZED);
//            return false;
//        }

        log.debug("返回true");
		return true;
	}
	
	private Map<String, Cookie> cookiesToMap(Cookie[] cookies) {
		HashMap<String, Cookie> cookieMap = new HashMap<>();
		if (cookies==null || cookies.length == 0) {
			return cookieMap;
		}
		for(Cookie cookie : cookies) {
			cookieMap.put(cookie.getName(), cookie);
		}
		return cookieMap;
	}
}
