package com.example.ticketsmodule.impl.users.service;

import com.example.ticketsmodule.impl.users.domain.UserEntity;
import com.example.ticketsmodule.impl.users.domain.UserRoleEnum;
import com.example.ticketsmodule.impl.users.repository.UserRepository;
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
    private UserRepository userRepository;

    @InjectMocks
    private AppUserDetailsService appUserDetailsService;

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .login("testuser")
                .password("encoded-password")
                .role(UserRoleEnum.USER)
                .build();

        when(userRepository.findByLogin("testuser")).thenReturn(Optional.of(user));

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
        UserEntity admin = UserEntity.builder()
                .id(UUID.randomUUID())
                .login("admin")
                .password("encoded-password")
                .role(UserRoleEnum.ADMIN)
                .build();

        when(userRepository.findByLogin("admin")).thenReturn(Optional.of(admin));

        UserDetails result = appUserDetailsService.loadUserByUsername("admin");

        assertThat(result.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_ShouldThrow_WhenUserNotFound() {
        when(userRepository.findByLogin("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appUserDetailsService.loadUserByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found: unknown");
    }
}