package com.example.ticketsmoduleimpl.oauth.service;

import com.example.ticketsmoduleimpl.config.JwtProperties;
import com.example.ticketsmoduleimpl.users.domain.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import tickets.model.LoginResponse;

import java.time.Duration;
import java.time.Instant;

/**
 * Сервис работы с JWT-токенами (access и refresh).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    public static final String CLAIM_TYP = "typ";
    public static final String CLAIM_ROLE = "role";
    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public LoginResponse createTokenPair(UserEntity user) {
        Instant now = Instant.now();
        Duration accessTtl = jwtProperties.getAccessTokenTtl();
        Instant accessExpires = now.plus(accessTtl);
        Instant refreshExpires = now.plus(jwtProperties.getRefreshTokenTtl());

        String accessToken = encodeToken(user, now, accessExpires, TOKEN_TYPE_ACCESS, true);
        String refreshToken = encodeToken(user, now, refreshExpires, TOKEN_TYPE_REFRESH, false);

        LoginResponse response = new LoginResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setExpiresIn((int) accessTtl.toSeconds());
        return response;
    }

    private String encodeToken(
            UserEntity user,
            Instant issuedAt,
            Instant expiresAt,
            String tokenType,
            boolean includeRole
    ) {
        JwtClaimsSet.Builder builder = JwtClaimsSet.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(user.getId().toString())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("login", user.getLogin())
                .claim(CLAIM_TYP, tokenType);

        if (includeRole) {
            builder.claim(CLAIM_ROLE, user.getRole().name());
        }

        return jwtEncoder.encode(
                JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS256).build(),
                        builder.build()
                )
        ).getTokenValue();
    }
}