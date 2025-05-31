package com.devteria.identity_service.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Cho phép truy cập URL /uploads/** từ thư mục local "uploads/"
        System.out.println(">>> Mapping /uploads/** to file:D:/Code/Java Code/shopapp-BE/src/main/uploads/");

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:D:/Code/Java Code/shopapp-BE/src/main/uploads/");
    }

}
