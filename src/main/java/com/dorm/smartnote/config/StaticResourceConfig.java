package com.dorm.smartnote.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 这一步是告诉 Spring：只要是请求 HTML 或静态文件，直接去 static 目录下找，不要拦截！
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}