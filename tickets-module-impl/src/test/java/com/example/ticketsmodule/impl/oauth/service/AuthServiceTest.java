package com.example.ticketsmodule.impl.oauth.service;

import com.example.ticketsmodule.api.model.LoginRequest;
import com.example.ticketsmodule.api.model.LoginResponse;
import com.example.ticketsmodule.api.model.RefreshTokenRequest;
import com.example.ticketsmodule.api.model.RegisterRequest;
import com.example.ticketsmodule.domain.user.model.User;
import com.example.ticketsmodule.domain.user.repository.UsersRepositoryPort;
import com.example.ticketsmodule.impl.users.conversion.RegisterMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService unit tests")
class AuthServiceTest {

    @Mock private UsersRepositoryPort usersRepositoryPort; // <-- Порт
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private JwtDecoder refreshTokenJwtDecoder;
    @Mock private RegisterMapper registerMapper;

    @InjectMocks
    private AuthService authService;

    private UUID userId;
    private User user; // <-- Домен
    private LoginResponse expectedResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        user = User.builder()
                .id(userId)
                .login("admin")
                .password("$2a$10$hashed")
                .fullName("Admin Adminov")
                .build();

        expectedResponse = new LoginResponse();
    }

    // ---------- login() ----------

    @Nested
    @DisplayName("login()")
    class LoginTests {

        @Test
        @DisplayName("should return token pair when credentials valid")
        void shouldReturnTokenPairWhenCredentialsValid() {
            LoginRequest request = new LoginRequest();
            request.setLogin("admin");
            request.setPassword("secret");

            when(usersRepositoryPort.findByLogin("admin")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("secret", user.getPassword())).thenReturn(true);
            when(jwtService.createTokenPair(user)).thenReturn(expectedResponse);

            LoginResponse actual = authService.login(request);

            assertThat(actual).isSameAs(expectedResponse);
            verify(usersRepositoryPort).findByLogin("admin");
            verify(passwordEncoder).matches("secret", user.getPassword());
            verify(jwtService).createTokenPair(user);
        }

        @Test
        @DisplayName("should throw BadCredentialsException when user not found")
        void shouldThrowWhenUserNotFound() {
            LoginRequest request = new LoginRequest();
            request.setLogin("unknown");
            request.setPassword("secret");

            when(usersRepositoryPort.findByLogin("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessageContaining("Invalid login or password");

            verifyNoInteractions(passwordEncoder, jwtService);
        }

        @Test
        @DisplayName("should throw BadCredentialsException when password does not match")
        void shouldThrowWhenPasswordMismatch() {
            LoginRequest request = new LoginRequest();
            request.setLogin("admin");
            request.setPassword("wrong");

            when(usersRepositoryPort.findByLogin("admin")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrong", user.getPassword())).thenReturn(false);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessageContaining("Invalid login or password");

            verifyNoInteractions(jwtService);
        }
    }

    // ---------- refresh() ----------

    @Nested
    @DisplayName("refresh()")
    class RefreshTests {

        @Test
        @DisplayName("should return new token pair when refresh token valid")
        void shouldReturnNewTokenPairWhenRefreshValid() {
            RefreshTokenRequest request = new RefreshTokenRequest();
            request.setRefreshToken("valid.refresh.token");

            Jwt jwt = buildJwt(userId, JwtService.TOKEN_TYPE_REFRESH);

            when(refreshTokenJwtDecoder.decode("valid.refresh.token")).thenReturn(jwt);
            when(usersRepositoryPort.findById(userId)).thenReturn(Optional.of(user));
            when(jwtService.createTokenPair(user)).thenReturn(expectedResponse);

            LoginResponse actual = authService.refresh(request);

            assertThat(actual).isSameAs(expectedResponse);
            verify(jwtService).createTokenPair(user);
        }

        @Test
        @DisplayName("should throw when token type is not refresh")
        void shouldThrowWhenTokenTypeIsNotRefresh() {
            RefreshTokenRequest request = new RefreshTokenRequest();
            request.setRefreshToken("access.token");

            Jwt jwt = buildJwt(userId, "access");

            when(refreshTokenJwtDecoder.decode("access.token")).thenReturn(jwt);

            assertThatThrownBy(() -> authService.refresh(request))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessageContaining("Invalid refresh token");

            verifyNoInteractions(usersRepositoryPort, jwtService);
        }

        @Test
        @DisplayName("should throw when user from token not found")
        void shouldThrowWhenUserNotFound() {
            RefreshTokenRequest request = new RefreshTokenRequest();
            request.setRefreshToken("valid.refresh.token");

            Jwt jwt = buildJwt(userId, JwtService.TOKEN_TYPE_REFRESH);

            when(refreshTokenJwtDecoder.decode("valid.refresh.token")).thenReturn(jwt);
            when(usersRepositoryPort.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.refresh(request))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessageContaining("User not found");

            verifyNoInteractions(jwtService);
        }

        @Test
        @DisplayName("should propagate JwtException when token is invalid")
        void shouldPropagateJwtExceptionWhenDecodeFails() {
            RefreshTokenRequest request = new RefreshTokenRequest();
            request.setRefreshToken("garbage");

            when(refreshTokenJwtDecoder.decode("garbage"))
                    .thenThrow(new org.springframework.security.oauth2.jwt.JwtException("Invalid"));

            assertThatThrownBy(() -> authService.refresh(request))
                    .isInstanceOf(org.springframework.security.oauth2.jwt.JwtException.class);

            verifyNoInteractions(usersRepositoryPort, jwtService);
        }
    }

    // ---------- register() ----------

    @Nested
    @DisplayName("register()")
    class RegisterTests {

        @Test
        @DisplayName("should save new user and return token pair")
        void shouldSaveNewUserAndReturnTokenPair() {
            RegisterRequest request = new RegisterRequest();
            request.setLogin("newuser");
            request.setPassword("secret");
            request.setFullName("New User");

            User newUser = User.builder()
                    .id(UUID.randomUUID())
                    .login("newuser")
                    .build();

            when(usersRepositoryPort.findByLogin("newuser")).thenReturn(Optional.empty());
            when(registerMapper.toDomain(request)).thenReturn(newUser); // <-- toDomain!
            when(passwordEncoder.encode("secret")).thenReturn("$2a$10$encrypted");
            when(usersRepositoryPort.save(newUser)).thenReturn(newUser);
            when(jwtService.createTokenPair(newUser)).thenReturn(expectedResponse);

            LoginResponse actual = authService.register(request);

            assertThat(actual).isSameAs(expectedResponse);
            verify(registerMapper).toDomain(request); // <-- Проверяем toDomain
            verify(usersRepositoryPort).save(newUser);
            verify(jwtService).createTokenPair(newUser);
        }

        @Test
        @DisplayName("should throw when login already occupied")
        void shouldThrowWhenLoginOccupied() {
            RegisterRequest request = new RegisterRequest();
            request.setLogin("admin");

            when(usersRepositoryPort.findByLogin("admin")).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessageContaining("Login is already occupied");

            verify(usersRepositoryPort, never()).save(any());
            verifyNoInteractions(registerMapper, jwtService);
        }
    }

    // ---------- helper ----------

    private static Jwt buildJwt(UUID subject, String tokenType) {
        Instant now = Instant.now();
        return Jwt.withTokenValue("token-value")
                .header("alg", "HS256")
                .subject(subject.toString())
                .claim(JwtService.CLAIM_TYP, tokenType)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .build();
    }
}