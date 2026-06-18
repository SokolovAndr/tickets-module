package com.example.ticketsmoduleimpl.oauth.service;

import com.example.ticketsmoduleimpl.users.domain.UserEntity;
import com.example.ticketsmoduleimpl.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrentUserService {
    private final UserRepository userRepository;

    public UserEntity getCurrentUser() {
        UUID userId = getCurrentUserId();
        return userRepository.findById(userId).orElseThrow(()
                -> new IllegalStateException("Current user not found in database"));
    }

    public UUID getCurrentUserId() {
        Jwt jwt = getCurrentJwt();
        String subject = jwt.getSubject();
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID in JWT subject: {}", subject);
            throw new IllegalStateException("Invalid user ID in token", e);
        }
    }

    public Jwt getCurrentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return jwt;
        }

        throw new IllegalStateException("Authentication is not JWT based");
    }
}
