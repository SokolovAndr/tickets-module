package com.example.ticketsmodule.impl.oauth.resource;

import com.example.ticketsmodule.api.model.LoginRequest;
import com.example.ticketsmodule.api.model.LoginResponse;
import com.example.ticketsmodule.api.model.RefreshTokenRequest;
import com.example.ticketsmodule.api.model.RegisterRequest;
import com.example.ticketsmodule.impl.oauth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OauthControllerApiImpl unit tests")
class OauthControllerApiImplTest {

    @Mock private AuthService authService;

    @InjectMocks
    private OauthControllerApiImpl controller;

    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        loginResponse = new LoginResponse();
        loginResponse.setAccessToken("access-token");
        loginResponse.setRefreshToken("refresh-token");
        loginResponse.setTokenType("Bearer");
    }

    // ---------- login() ----------

    @Nested
    @DisplayName("login()")
    class LoginTests {

        @Test
        @DisplayName("should return 200 OK with login response")
        void shouldReturn200Ok() {
            LoginRequest request = new LoginRequest();
            request.setLogin("admin");
            request.setPassword("secret");
            when(authService.login(request)).thenReturn(loginResponse);

            ResponseEntity<LoginResponse> result = controller.login(request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isSameAs(loginResponse);
            verify(authService).login(request);
        }

        @Test
        @DisplayName("should propagate BadCredentialsException from service")
        void shouldPropagateBadCredentials() {
            LoginRequest request = new LoginRequest();
            when(authService.login(request))
                    .thenThrow(new BadCredentialsException("Invalid login or password"));

            assertThatThrownBy(() -> controller.login(request))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessageContaining("Invalid login or password");
        }
    }

    // ---------- refreshToken() ----------

    @Nested
    @DisplayName("refreshToken()")
    class RefreshTokenTests {

        @Test
        @DisplayName("should return 200 OK with new token pair")
        void shouldReturn200Ok() {
            RefreshTokenRequest request = new RefreshTokenRequest();
            request.setRefreshToken("valid.refresh.token");
            when(authService.refresh(request)).thenReturn(loginResponse);

            ResponseEntity<LoginResponse> result = controller.refreshToken(request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isSameAs(loginResponse);
            verify(authService).refresh(request);
        }

        @Test
        @DisplayName("should propagate BadCredentialsException from service")
        void shouldPropagateBadCredentials() {
            RefreshTokenRequest request = new RefreshTokenRequest();
            when(authService.refresh(request))
                    .thenThrow(new BadCredentialsException("Invalid refresh token"));

            assertThatThrownBy(() -> controller.refreshToken(request))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessageContaining("Invalid refresh token");
        }
    }

    // ---------- register() ----------

    @Nested
    @DisplayName("register()")
    class RegisterTests {

        @Test
        @DisplayName("should return 201 Created with login response")
        void shouldReturn201Created() {
            RegisterRequest request = new RegisterRequest();
            request.setLogin("newuser");
            request.setPassword("secret");
            when(authService.register(request)).thenReturn(loginResponse);

            ResponseEntity<LoginResponse> result = controller.register(request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(result.getBody()).isSameAs(loginResponse);
            verify(authService).register(request);
        }

        @Test
        @DisplayName("should propagate BadCredentialsException when login occupied")
        void shouldPropagateBadCredentials() {
            RegisterRequest request = new RegisterRequest();
            when(authService.register(request))
                    .thenThrow(new BadCredentialsException("Login is already occupied"));

            assertThatThrownBy(() -> controller.register(request))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessageContaining("Login is already occupied");
        }
    }
}