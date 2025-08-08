package com.seal.ttapp_base.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("swagger.info")
public class SwaggerInfo {

    private String title = "抖音app Spring Boot标准化示例项目";

    private String desc = "Spring Boot标准化";

    private String version = "1.0";
}
