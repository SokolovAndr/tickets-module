package com.example.ticketsmodule.config;

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
    private static final String LOGIN_PATH = "/login";
    private static final String REGISTER_PATH = "/register";
    private static final String TOKEN_PATH = "/token";
    private static final String TICKETS_PATH = "/tickets";
    private static final String USERS_ME_PATH = "/users/me";

    private static final String CONTENT_TYPE = "application/json; charset=UTF-8";

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
                        .requestMatchers(HttpMethod.POST,
                                API+LOGIN_PATH,
                                API+TOKEN_PATH,
                                API+REGISTER_PATH)
                        .permitAll()
                        .requestMatchers(HttpMethod.POST,
                                API+TICKETS_PATH+"/{id}/buy",
                                API+TICKETS_PATH+"/{id}/return",
                                API+USERS_ME_PATH+"/password")
                        .hasAnyRole(ROLE_USER, ROLE_ADMIN)
                        .requestMatchers(HttpMethod.PATCH, API+USERS_ME_PATH).hasAnyRole(ROLE_USER, ROLE_ADMIN)
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
                            response.setContentType(CONTENT_TYPE);

                            ErrorResponse authErrorResponse = new ErrorResponse()
                                    .message(authException.getMessage())
                                    .userMessage("Отсутствует или невалиден JWT-токен");

                            response.getWriter().write(objectMapper.writeValueAsString(authErrorResponse));

                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType(CONTENT_TYPE);

                            ErrorResponse accessErrorResponse = new ErrorResponse()
                                    .message(accessDeniedException.getMessage())
                                    .userMessage("Пользователь не имеет ни одной из необходимых ролей");

                            response.getWriter().write(objectMapper.writeValueAsString(accessErrorResponse));
                }));
        return http.build();
    }

}
