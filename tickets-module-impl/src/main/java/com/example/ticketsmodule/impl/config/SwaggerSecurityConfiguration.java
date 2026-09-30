package com.example.ticketsmodule.impl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SwaggerSecurityConfiguration {

    private static final String[] SWAGGER_ENDPOINTS = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**"
    };

    @Bean
    @Order(0)
    SecurityFilterChain swaggerSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(SWAGGER_ENDPOINTS)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }
}