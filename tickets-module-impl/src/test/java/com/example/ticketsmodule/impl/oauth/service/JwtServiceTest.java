package com.example.ticketsmodule.impl.oauth.service;

import com.example.ticketsmodule.api.model.LoginResponse;
import com.example.ticketsmodule.impl.config.JwtProperties;
import com.example.ticketsmodule.impl.users.domain.UserEntity;
import com.example.ticketsmodule.impl.users.domain.UserRoleEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService unit tests")
class JwtServiceTest {

    @Mock private JwtEncoder jwtEncoder;
    @Mock private JwtProperties jwtProperties;

    @InjectMocks
    private JwtService jwtService;

    private UUID userId;
    private UserEntity user;

    private static final Duration ACCESS_TTL = Duration.ofHours(10);
    private static final Duration REFRESH_TTL = Duration.ofDays(7);
    private static final String ISSUER = "tickets-module";

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        user = new UserEntity();
        user.setId(userId);
        user.setLogin("admin");
        user.setRole(UserRoleEnum.ADMIN);

        // Мокаем JwtProperties — используем lenient() для тестов, где не все стабы нужны
        lenient().when(jwtProperties.getAccessTokenTtl()).thenReturn(ACCESS_TTL);
        lenient().when(jwtProperties.getRefreshTokenTtl()).thenReturn(REFRESH_TTL);
        lenient().when(jwtProperties.getIssuer()).thenReturn(ISSUER);
    }

    // ---------- createTokenPair() ----------

    @Nested
    @DisplayName("createTokenPair()")
    class CreateTokenPairTests {

        @Test
        @DisplayName("should return response with both tokens and Bearer type")
        void shouldReturnResponseWithBothTokens() {
            stubEncoderToReturn("access-token-value", "refresh-token-value");

            LoginResponse response = jwtService.createTokenPair(user);

            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("access-token-value");
            assertThat(response.getRefreshToken()).isEqualTo("refresh-token-value");
            assertThat(response.getTokenType()).isEqualTo("Bearer");
        }

        @Test
        @DisplayName("should set expiresIn to access token TTL in seconds")
        void shouldSetExpiresInToAccessTtlSeconds() {
            stubEncoderToReturn("access", "refresh");

            LoginResponse response = jwtService.createTokenPair(user);

            assertThat(response.getExpiresIn()).isEqualTo((int) ACCESS_TTL.toSeconds());
        }

        @Test
        @DisplayName("should call encoder exactly twice")
        void shouldCallEncoderTwice() {
            stubEncoderToReturn("access", "refresh");

            jwtService.createTokenPair(user);

            verify(jwtEncoder, times(2)).encode(any(JwtEncoderParameters.class));
        }

        @Test
        @DisplayName("should propagate exception when encoder fails")
        void shouldPropagateExceptionWhenEncoderFails() {
            when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                    .thenThrow(new IllegalStateException("Encoder failure"));

            assertThatThrownBy(() -> jwtService.createTokenPair(user))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Encoder failure");
        }
    }

    // ---------- Claims в access-токене ----------

    @Nested
    @DisplayName("access token claims")
    class AccessTokenClaimsTests {

        @Test
        @DisplayName("should contain typ=access, role, login, subject, issuer")
        void shouldContainAllAccessClaims() {
            stubEncoderToReturn("access", "refresh");

            jwtService.createTokenPair(user);

            JwtClaimsSet accessClaims = captureClaims(0);

            assertThat(accessClaims.getClaimAsString(JwtService.CLAIM_TYP))
                    .isEqualTo(JwtService.TOKEN_TYPE_ACCESS);
            assertThat(accessClaims.getClaimAsString(JwtService.CLAIM_ROLE))
                    .isEqualTo("ADMIN");
            assertThat(accessClaims.getClaimAsString("login")).isEqualTo("admin");
            assertThat(accessClaims.getSubject()).isEqualTo(userId.toString());
            assertThat(accessClaims.getClaimAsString("iss")).isEqualTo(ISSUER);
        }

        @Test
        @DisplayName("should set expiresAt = issuedAt + accessTtl")
        void shouldSetAccessExpiresAt() {
            stubEncoderToReturn("access", "refresh");

            jwtService.createTokenPair(user);

            JwtClaimsSet accessClaims = captureClaims(0);
            Instant issuedAt = accessClaims.getIssuedAt();
            Instant expiresAt = accessClaims.getExpiresAt();

            assertThat(issuedAt).isNotNull();
            assertThat(expiresAt).isNotNull();
            assertThat(Duration.between(issuedAt, expiresAt)).isEqualTo(ACCESS_TTL);
        }
    }

    // ---------- Claims в refresh-токене ----------

    @Nested
    @DisplayName("refresh token claims")
    class RefreshTokenClaimsTests {

        @Test
        @DisplayName("should contain typ=refresh, login, subject, issuer — but NOT role")
        void shouldContainRefreshClaimsWithoutRole() {
            stubEncoderToReturn("access", "refresh");

            jwtService.createTokenPair(user);

            JwtClaimsSet refreshClaims = captureClaims(1);

            assertThat(refreshClaims.getClaimAsString(JwtService.CLAIM_TYP))
                    .isEqualTo(JwtService.TOKEN_TYPE_REFRESH);
            assertThat(refreshClaims.getClaimAsString("login")).isEqualTo("admin");
            assertThat(refreshClaims.getSubject()).isEqualTo(userId.toString());
            assertThat(refreshClaims.getClaimAsString("iss")).isEqualTo(ISSUER);
            assertThat(refreshClaims.getClaimAsString(JwtService.CLAIM_ROLE)).isNull();
        }

        @Test
        @DisplayName("should set expiresAt = issuedAt + refreshTtl")
        void shouldSetRefreshExpiresAt() {
            stubEncoderToReturn("access", "refresh");

            jwtService.createTokenPair(user);

            JwtClaimsSet refreshClaims = captureClaims(1);
            Instant issuedAt = refreshClaims.getIssuedAt();
            Instant expiresAt = refreshClaims.getExpiresAt();

            assertThat(Duration.between(issuedAt, expiresAt)).isEqualTo(REFRESH_TTL);
        }
    }

    // ---------- helpers ----------

    /**
     * Мокаем encode() так, чтобы первый вызов вернул access-токен,
     * второй — refresh-токен.
     */
    private void stubEncoderToReturn(String accessToken, String refreshToken) {
        Jwt jwt1 = Jwt.withTokenValue(accessToken)
                .header("alg", "HS256")
                .subject(userId.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        Jwt jwt2 = Jwt.withTokenValue(refreshToken)
                .header("alg", "HS256")
                .subject(userId.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(jwt1, jwt2);
    }

    /**
     * Захватываем аргументы encode() и возвращаем claims из указанного вызова.
     */
    private JwtClaimsSet captureClaims(int invocationIndex) {
        ArgumentCaptor<JwtEncoderParameters> captor =
                ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder, times(2)).encode(captor.capture());
        List<JwtEncoderParameters> allParams = captor.getAllValues();
        return allParams.get(invocationIndex).getClaims();
    }
}