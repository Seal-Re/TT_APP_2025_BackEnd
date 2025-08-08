package com.seal.ttapp_base.config;

import com.seal.ttapp_base.interceptor.FormatlizeHttpReqRespInterceptor;
import com.seal.ttapp_base.interceptor.RestAuthenticationInterceptor;
import com.seal.ttapp_base.interceptor.RestAuthorizationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @Author yanyanyang
 * @Date 2022/1/18
 * @Describe:
 */
@Configuration
public class RoleAuthInterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private RestAuthenticationInterceptor restAuthenticationInterceptor;
    @Autowired
    private RestAuthorizationInterceptor restAuthorizationInterceptor;
    @Resource
    private FormatlizeHttpReqRespInterceptor formatlizeHttpReqRespInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(restAuthenticationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/error/**")
                .excludePathPatterns("/swagger-ui.html/**", "/swagger-resources/**", "webjars/**");
        registry.addInterceptor(restAuthorizationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/error/**")
                .excludePathPatterns("/swagger-ui.html/**", "/swagger-resources/**", "webjars/**");
        registry.addInterceptor(formatlizeHttpReqRespInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/error/**")
                .excludePathPatterns("/swagger-ui.html/**", "/swagger-resources/**", "webjars/**");
    }

    @Bean
    public FormatlizeHttpReqRespInterceptor formatlizeHttpReqRespInterceptor(){
        return new FormatlizeHttpReqRespInterceptor();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.defaultContentType(MediaType.APPLICATION_JSON);
    }
}
