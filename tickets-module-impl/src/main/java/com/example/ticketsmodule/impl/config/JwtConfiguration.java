package com.example.ticketsmodule.impl.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfiguration {

    private final JwtProperties jwtProperties;
    private static final String ALGORITHM = "HmacSHA256";

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
