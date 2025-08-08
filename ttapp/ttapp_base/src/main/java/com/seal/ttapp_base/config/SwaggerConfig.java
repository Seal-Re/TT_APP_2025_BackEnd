package com.seal.ttapp_base.config;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

@EnableSwagger2
@Configuration
@ConfigurationProperties("swagger")
@ConditionalOnClass(Docket.class)
public class SwaggerConfig {
    @Value("${swagger.enable:false}")
    private boolean enableSwagger;

    @Autowired
    private SwaggerInfo apiInfo;

    @Bean
    public Docket api(){
        return new Docket(DocumentationType.SWAGGER_2)
                .enable(enableSwagger)
                .apiInfo(apiInfo())
                .select()
                //为当前包下的Controller生成API文档
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
//		System.out.println("-----"+ apiInfo.getTitle());
        return new ApiInfo(
                apiInfo.getTitle(),
                apiInfo.getDesc(),
                apiInfo.getVersion(),
                null,
                null, null, null, Collections.emptyList());
    }
}
