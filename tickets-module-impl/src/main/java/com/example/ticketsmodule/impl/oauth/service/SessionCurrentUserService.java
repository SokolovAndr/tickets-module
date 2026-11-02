package com.example.ticketsmodule.impl.oauth.service;

import com.example.ticketsmodule.domain.user.model.User;
import com.example.ticketsmodule.domain.user.repository.UsersRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Резолвер текущего пользователя для UI (form login + сессии).
 * Используется только в UI-цепочке Spring Security.
 */

@Service
@RequiredArgsConstructor
public class SessionCurrentUserService {

    private final UsersRepositoryPort usersRepositoryPort;

    public User getCurrentUser() {
        return usersRepositoryPort.findByLogin(getCurrentLogin())
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
