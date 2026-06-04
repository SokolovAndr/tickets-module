package com.example.ticketsmoduleimpl.oauth.resource;

import com.example.ticketsmoduleimpl.oauth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tickets.api.OauthControllerApi;
import tickets.model.LoginRequest;
import tickets.model.LoginResponse;
import tickets.model.RefreshTokenRequest;
import tickets.model.RegisterRequest;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OauthControllerApiImpl implements OauthControllerApi {

    private final AuthService authService;

    @Override
    public ResponseEntity<LoginResponse> login(@Valid LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @Override
    public ResponseEntity<LoginResponse> refreshToken(@Valid RefreshTokenRequest refreshTokenRequest) {
        return ResponseEntity.ok(authService.refresh(refreshTokenRequest));
    }

    @Override
    public ResponseEntity<LoginResponse> register(@Valid RegisterRequest registerRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(registerRequest));
    }
}
