package com.example.ticketsmodule.impl.users.service;

import com.example.ticketsmodule.impl.users.domain.UserEntity;
import com.example.ticketsmodule.impl.users.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsersService unit tests")
class UsersServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UsersService usersService;

    private UUID userId;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        user = new UserEntity();
        user.setId(userId);
        user.setLogin("admin");
        user.setFullName("Admin Adminov");
    }

    @Test
    @DisplayName("findOneById() should return user when found")
    void findOneById_shouldReturnUser_whenFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserEntity actual = usersService.findOneById(userId);

        assertThat(actual).isSameAs(user);
        assertThat(actual.getId()).isEqualTo(userId);
        assertThat(actual.getLogin()).isEqualTo("admin");
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("findOneById() should throw EntityNotFoundException when user not found")
    void findOneById_shouldThrow_whenNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usersService.findOneById(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(userId.toString())
                .hasMessageContaining("User with id")
                .hasMessageContaining("not found");

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("findOneById() should throw when id is null")
    void findOneById_shouldThrow_whenIdIsNull() {
        // findById(null) у Spring Data JPA бросает IllegalArgumentException
        when(userRepository.findById(null))
                .thenThrow(new IllegalArgumentException("The given id must not be null"));

        assertThatThrownBy(() -> usersService.findOneById(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}