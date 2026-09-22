package com.example.ticketsmoduleimpl.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import tickets.model.ErrorResponse;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtProperties.class)  // важно
public class SecurityConfiguration {

    private final JwtProperties jwtProperties;
    private final ObjectMapper objectMapper;
    private static final String ALGORITHM = "HmacSHA256";

    private static final String API = "/api";

    private static final String[] SWAGGER_UI_ENDPOINTS = {
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**"};

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, API+"/login", API+"/token", API+"/register").permitAll()
                        .requestMatchers(SWAGGER_UI_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.POST, API+"/tickets/{id}/buy", API+"/tickets/{id}/return").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET).hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE).hasRole("ADMIN")
                        .anyRequest().authenticated()
                ).oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter()))
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

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    JwtEncoder jwtEncoder() {
        byte[] key = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        var secretKey = new SecretKeySpec(key, ALGORITHM);
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
    }

    @Bean
    JwtDecoder refreshTokenJwtDecoder() {
        return createJwtDecoder();
    }

    @Bean
    JwtDecoder jwtDecoder() { return createJwtDecoder(); }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String role = jwt.getClaimAsString("role"); // как в JwtService.CLAIM_ROLE
            if (role == null || role.isBlank()) {
                return List.of();
            }
            return List.of(new SimpleGrantedAuthority("ROLE_" + role));
        });
        return converter;
    }

    /**
     * Создаёт JwtDecoder с использованием симметричного ключа.
     * Используется и для access-токенов, и для refresh-токенов,
     * так как оба подписываются одним и тем же секретом.
     */
    private JwtDecoder createJwtDecoder() {
        byte[] key = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        var secretKey = new SecretKeySpec(key, ALGORITHM);
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }

}
