package com.example.ticketsmodule.impl.oauth.service;

import com.example.ticketsmodule.impl.users.domain.UserEntity;
import com.example.ticketsmodule.impl.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Резолвер текущего пользователя для UI (form login + сессии).
 * Используется только в UI-цепочке Spring Security.
 */

@Service
@RequiredArgsConstructor
public class SessionCurrentUserService {

    private final UserRepository userRepository;

    public UserEntity getCurrentUser() {
        return userRepository.findByLogin(getCurrentLogin())
                .orElseThrow(() -> new IllegalStateException("Current user not found"));
    }

    public UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }

    protected String getCurrentLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }
        return authentication.getName();
    }
}
