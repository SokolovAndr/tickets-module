package com.example.ticketsmoduleimpl.oauth.service;

import com.example.ticketsmoduleimpl.users.domain.UserEntity;
import com.example.ticketsmoduleimpl.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import tickets.model.LoginRequest;
import tickets.model.LoginResponse;
import tickets.model.RefreshTokenRequest;

import java.util.UUID;

/**
 * Сервис аутентификации
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final @Qualifier("refreshTokenJwtDecoder") JwtDecoder refreshTokenJwtDecoder;
    public LoginResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByLogin(request.getLogin())
                .filter(u -> passwordEncoder.matches(request.getPassword(), u.getPassword()))
                .orElseThrow(() -> new BadCredentialsException("Invalid login or password"));
        return jwtService.createTokenPair(user);
    }
    public LoginResponse refresh(RefreshTokenRequest request) {
        Jwt jwt = refreshTokenJwtDecoder.decode(request.getRefreshToken());
        if (!JwtService.TOKEN_TYPE_REFRESH.equals(jwt.getClaimAsString(JwtService.CLAIM_TYP))) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        UUID userId = UUID.fromString(jwt.getSubject());
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("User not found"));
        return jwtService.createTokenPair(user);
    }
}
