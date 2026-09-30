package com.example.ticketsmodule.impl.oauth.service;

import com.example.ticketsmodule.impl.users.domain.UserEntity;
import com.example.ticketsmodule.impl.users.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionCurrentUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SessionCurrentUserService sessionCurrentUserService;

    private UserEntity user;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .id(UUID.randomUUID())
                .login("testuser")
                .build();

        authentication = new UsernamePasswordAuthenticationToken(
                "testuser", "password", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_ShouldReturnUser_WhenAuthenticatedAndUserExists() {
        when(userRepository.findByLogin("testuser")).thenReturn(Optional.of(user));

        UserEntity result = sessionCurrentUserService.getCurrentUser();

        assertEquals(user, result);
    }

    @Test
    void getCurrentUser_ShouldThrow_WhenUserNotFound() {
        when(userRepository.findByLogin("testuser")).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> sessionCurrentUserService.getCurrentUser());

        assertEquals("Current user not found", ex.getMessage());
    }

    @Test
    void getCurrentUser_ShouldThrow_WhenAuthenticationIsNull() {
        SecurityContextHolder.getContext().setAuthentication(null);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> sessionCurrentUserService.getCurrentUser());

        assertEquals("User is not authenticated", ex.getMessage());
    }

    @Test
    void getCurrentUser_ShouldThrow_WhenAuthenticationNotAuthenticated() {
        Authentication unauthenticated = mock(Authentication.class);
        when(unauthenticated.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(unauthenticated);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> sessionCurrentUserService.getCurrentUser());

        assertEquals("User is not authenticated", ex.getMessage());
    }

    @Test
    void getCurrentUserId_ShouldReturnUserId() {
        when(userRepository.findByLogin("testuser")).thenReturn(Optional.of(user));

        UUID id = sessionCurrentUserService.getCurrentUserId();

        assertEquals(user.getId(), id);
    }
}