package com.example.docsmndbackend.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class CorsConfigure implements WebMvcConfigurer {
    private final AppProperties appProperties;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] allowedOrigins = appProperties.getCors()
                .getAllowedOrigins().split(",");

        String[] allowedMethods = appProperties.getCors()
                .getGetAllowedMethod().split(",");

        String[] allowedHeaders = appProperties.getCors()
                .getAllowedHeaders().split(",");

        registry.addMapping("/api/**")
                .allowedOriginPatterns(allowedOrigins)
                .allowedMethods(allowedMethods)
                .allowedHeaders(allowedHeaders)
                .allowCredentials(true)
                .maxAge(3600);
    }
}
