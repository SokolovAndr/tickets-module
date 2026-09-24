package com.example.ticketsmodule.impl.oauth.resource;

import com.example.ticketsmodule.impl.oauth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import com.example.ticketsmodule.api.controller.OauthControllerApi;
import com.example.ticketsmodule.api.model.LoginRequest;
import com.example.ticketsmodule.api.model.LoginResponse;
import com.example.ticketsmodule.api.model.RefreshTokenRequest;
import com.example.ticketsmodule.api.model.RegisterRequest;

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
