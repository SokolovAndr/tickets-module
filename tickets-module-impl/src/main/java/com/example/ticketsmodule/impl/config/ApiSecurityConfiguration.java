package com.example.ticketsmodule.impl.config;

import com.example.ticketsmodule.impl.users.domain.UserRoleEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import com.example.ticketsmodule.api.model.ErrorResponse;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class ApiSecurityConfiguration {

    private final ObjectMapper objectMapper;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    private static final String ROLE_ADMIN = UserRoleEnum.ADMIN.name();
    private static final String ROLE_USER = UserRoleEnum.USER.name();

    private static final String API = "/api";

    private static final String[] SWAGGER_UI_ENDPOINTS = {
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**"};

    @Bean
    @Order(1)
    SecurityFilterChain apiSecurityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        http
                .securityMatcher(API + "/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, API+"/login", API+"/token", API+"/register").permitAll()
                        .requestMatchers(SWAGGER_UI_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.POST, API+"/tickets/{id}/buy", API+"/tickets/{id}/return").hasAnyRole(ROLE_USER, ROLE_ADMIN)
                        .requestMatchers(HttpMethod.GET).hasAnyRole(ROLE_USER, ROLE_ADMIN)
                        .requestMatchers(HttpMethod.POST).hasRole(ROLE_ADMIN)
                        .requestMatchers(HttpMethod.PUT).hasRole(ROLE_ADMIN)
                        .requestMatchers(HttpMethod.PATCH).hasRole(ROLE_ADMIN)
                        .requestMatchers(HttpMethod.DELETE).hasRole(ROLE_ADMIN)
                        .anyRequest().authenticated()
                ).oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter))
                        .authenticationEntryPoint((request, response, authException)
                                -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType("application/json; charset=UTF-8");

                            ErrorResponse authErrorResponse = new ErrorResponse()
                                    .message(authException.getMessage())
                                    .userMessage("Отсутствует или невалиден JWT-токен");

                            response.getWriter().write(objectMapper.writeValueAsString(authErrorResponse));

                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType("application/json; charset=UTF-8");

                            ErrorResponse accessErrorResponse = new ErrorResponse()
                                    .message(accessDeniedException.getMessage())
                                    .userMessage("Пользователь не имеет ни одной из необходимых ролей");

                            response.getWriter().write(objectMapper.writeValueAsString(accessErrorResponse));
                }));
        return http.build();
    }

}
