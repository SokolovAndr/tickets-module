package com.example.ticketsmodule.impl.oauth.service;

import com.example.ticketsmodule.domain.user.model.User;
import com.example.ticketsmodule.domain.user.repository.UsersRepositoryPort;
import com.example.ticketsmodule.impl.users.conversion.RegisterMapper;
import com.example.ticketsmodule.domain.user.model.UserRoleEnum;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import com.example.ticketsmodule.api.model.LoginRequest;
import com.example.ticketsmodule.api.model.LoginResponse;
import com.example.ticketsmodule.api.model.RefreshTokenRequest;
import com.example.ticketsmodule.api.model.RegisterRequest;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сервис аутентификации
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsersRepositoryPort usersRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final @Qualifier("refreshTokenJwtDecoder") JwtDecoder refreshTokenJwtDecoder;
    private final RegisterMapper registerMapper;

    public LoginResponse login(@Valid LoginRequest request) {
        User user = usersRepositoryPort.findByLogin(request.getLogin())
                .filter(u -> passwordEncoder.matches(request.getPassword(), u.getPassword()))
                .orElseThrow(() -> new BadCredentialsException("Invalid login or password"));
        return jwtService.createTokenPair(user);
    }
    public LoginResponse refresh(@Valid RefreshTokenRequest request) {
        Jwt jwt = refreshTokenJwtDecoder.decode(request.getRefreshToken());
        if (!JwtService.TOKEN_TYPE_REFRESH.equals(jwt.getClaimAsString(JwtService.CLAIM_TYP))) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        UUID userId = UUID.fromString(jwt.getSubject());
        User user = usersRepositoryPort.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("User not found"));
        return jwtService.createTokenPair(user);
    }

    public LoginResponse register(@Valid RegisterRequest registerRequest) {

        if (usersRepositoryPort.findByLogin(registerRequest.getLogin()).isPresent()) {
            throw new BadCredentialsException("Login is already occupied");
        }

        final User newUser = registerMapper.toDomain(registerRequest);
        final String passwordEncrypted = passwordEncoder.encode(registerRequest.getPassword());

        newUser.setPassword(passwordEncrypted);
        newUser.setRole(UserRoleEnum.USER);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());

        User savedUser = usersRepositoryPort.save(newUser);
        return jwtService.createTokenPair(savedUser);
    }
}
