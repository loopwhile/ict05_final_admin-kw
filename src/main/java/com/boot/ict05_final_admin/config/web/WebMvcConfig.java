// src/main/java/com/boot/ict05_final_admin/config/web/WebMvcConfig.java
package com.boot.ict05_final_admin.config.web;

import com.boot.ict05_final_admin.config.interceptor.AuthInterceptor;
import com.boot.ict05_final_admin.config.interceptor.NavBlockInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    private final AuthInterceptor authInterceptor;
    private final NavBlockInterceptor navBlockInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor).order(1);

        registry.addInterceptor(navBlockInterceptor)
                .order(2)
                .addPathPatterns(
                        "/store/**","/menu/**","/receive/**","/inventory/**",
                        "/material/**","/store/material/**","/staff/**",
                        "/analytics/**","/notice/**"
                );
    }
}
