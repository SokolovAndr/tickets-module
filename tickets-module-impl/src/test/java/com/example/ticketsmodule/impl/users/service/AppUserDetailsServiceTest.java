package com.example.ticketsmodule.impl.users.service;

import com.example.ticketsmodule.domain.user.model.User;
import com.example.ticketsmodule.domain.user.model.UserRoleEnum;
import com.example.ticketsmodule.domain.user.repository.UsersRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserDetailsServiceTest {

    @Mock
    private UsersRepositoryPort usersRepositoryPort;

    @InjectMocks
    private AppUserDetailsService appUserDetailsService;

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        User user = User.builder() // <-- Домен
                .id(UUID.randomUUID())
                .login("testuser")
                .password("encoded-password")
                .role(UserRoleEnum.USER)
                .build();

        when(usersRepositoryPort.findByLogin("testuser")).thenReturn(Optional.of(user));

        UserDetails result = appUserDetailsService.loadUserByUsername("testuser");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getPassword()).isEqualTo("encoded-password");
        assertThat(result.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    void loadUserByUsername_ShouldReturnAdminAuthority_WhenUserIsAdmin() {
        User admin = User.builder() // <-- Домен
                .id(UUID.randomUUID())
                .login("admin")
                .password("encoded-password")
                .role(UserRoleEnum.ADMIN)
                .build();

        when(usersRepositoryPort.findByLogin("admin")).thenReturn(Optional.of(admin));

        UserDetails result = appUserDetailsService.loadUserByUsername("admin");

        assertThat(result.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_ShouldThrow_WhenUserNotFound() {
        when(usersRepositoryPort.findByLogin("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appUserDetailsService.loadUserByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found: unknown");
    }
}