package com.seal.ttapp_base.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class FormatlizeHttpReqRespInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        // 格式化 req-401错误
//        formatlize401Status(response);

//        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }


//    private void formatlize401Status(HttpServletResponse response){
//        if(response.getStatus() == HttpStatus.SC_UNAUTHORIZED){
//            try {
//                response.setStatus(HttpStatus.SC_OK);
//                response.setHeader("Content-Type","application/json;charset=UTF-8");
//                PrintWriter writer = response.getWriter();
//                Response<Object> resp = ResponseFactory.failure(ResponseCode.UNAUTHORIZED, "权限异常");
//                writer.write(JSONUtil.toJsonStr(resp));
//                writer.flush();
//            }catch (IOException exception){
//                log.warn("添加权限异常错误");
//            }
//        }
//    }
}
