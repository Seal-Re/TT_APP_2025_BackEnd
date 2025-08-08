package com.seal.ttapp_base;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.seal.ttapp_base.config")
@AutoConfigureBefore(ValidationAutoConfiguration.class)
@MapperScan(basePackages = {"com.seal.ttapp_base.dal"})
public class ServiceConfigAutoConfiguration {
}