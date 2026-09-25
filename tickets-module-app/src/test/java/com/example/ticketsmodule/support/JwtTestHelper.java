package com.example.ticketsmodule.support;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class JwtTestHelper {

    private JwtTestHelper() {}

    public static Jwt jwtWithRole(String role) {
        return jwtWithRoleAndSubject(role, UUID.randomUUID());
    }

    public static Jwt jwtWithRoleAndSubject(String role, UUID subject) {
        Instant now = Instant.now();
        return Jwt.withTokenValue("test-token-" + UUID.randomUUID())
                .header("alg", "HS256")
                .subject(subject.toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .claim("role", role)
                .claim("typ", "access")
                .build();
    }

    public static RequestPostProcessor withRole(String role) {
        Jwt jwt = jwtWithRole(role);
        return SecurityMockMvcRequestPostProcessors.authentication(
                new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_" + role)))
        );
    }
}