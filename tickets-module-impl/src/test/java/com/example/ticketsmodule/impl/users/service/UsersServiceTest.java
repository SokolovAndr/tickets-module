package com.example.ticketsmodule.impl.users.service;

import com.example.ticketsmodule.api.model.ChangePasswordRequest;
import com.example.ticketsmodule.api.model.UserPatchRequest;
import com.example.ticketsmodule.api.model.UserResponse;
import com.example.ticketsmodule.impl.common.web.rest.exceptions.ApiException;
import com.example.ticketsmodule.impl.users.conversion.UserMapper;
import com.example.ticketsmodule.impl.users.domain.UserEntity;
import com.example.ticketsmodule.impl.users.domain.UserRoleEnum;
import com.example.ticketsmodule.impl.users.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsersService unit tests")
class UsersServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UsersService usersService;

    private UUID userId;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        user = UserEntity.builder()
                .id(userId)
                .login("admin")
                .password("hash-old")
                .fullName("Admin Adminov")
                .role(UserRoleEnum.USER)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    // ================= findOneById =================

    @Nested
    @DisplayName("findOneById()")
    class FindOneByIdTests {

        @Test
        @DisplayName("should return user when found")
        void shouldReturnUser_whenFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            UserEntity actual = usersService.findOneById(userId);

            assertThat(actual).isSameAs(user);
            assertThat(actual.getId()).isEqualTo(userId);
            assertThat(actual.getLogin()).isEqualTo("admin");
            verify(userRepository).findById(userId);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when user not found")
        void shouldThrow_whenNotFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> usersService.findOneById(userId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("User with id")
                    .hasMessageContaining(userId.toString())
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

    // ================= changeCurrentUserPassword =================

    @Nested
    @DisplayName("changeCurrentUserPassword()")
    class ChangeCurrentUserPasswordTests {

        private ChangePasswordRequest request;

        @BeforeEach
        void setUp() {
            request = new ChangePasswordRequest();
            request.setOldPassword("oldPass");
            request.setNewPassword("newPass");
        }

        @Test
        @DisplayName("should throw ApiException when old password incorrect")
        void shouldThrow_whenOldPasswordIncorrect() {
            when(passwordEncoder.matches("oldPass", "hash-old")).thenReturn(false);

            assertThatThrownBy(() -> usersService.changeCurrentUserPassword(user, request))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Current password incorrect")
                    .extracting("userMessage")
                    .isEqualTo("Текущий пароль неверен");

            verify(userRepository, never()).save(any());
            verify(passwordEncoder, never()).encode(any());
        }

        @Test
        @DisplayName("should throw ApiException when new password equals current")
        void shouldThrow_whenNewPasswordEqualsCurrent() {
            when(passwordEncoder.matches("oldPass", "hash-old")).thenReturn(true);
            when(passwordEncoder.matches("newPass", "hash-old")).thenReturn(true);

            assertThatThrownBy(() -> usersService.changeCurrentUserPassword(user, request))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("New password equals current")
                    .extracting("userMessage")
                    .isEqualTo("Новый пароль должен отличаться от текущего");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should update password, updatedAt and save user on success")
        void shouldUpdatePassword_whenValid() {
            when(passwordEncoder.matches("oldPass", "hash-old")).thenReturn(true);
            when(passwordEncoder.matches("newPass", "hash-old")).thenReturn(false);
            when(passwordEncoder.encode("newPass")).thenReturn("hash-new");

            usersService.changeCurrentUserPassword(user, request);

            assertThat(user.getPassword()).isEqualTo("hash-new");
            assertThat(user.getUpdatedAt()).isAfter(LocalDateTime.now().minusMinutes(1));
            verify(userRepository).save(user);
            verify(passwordEncoder).encode("newPass");
        }

        @Test
        @DisplayName("should not touch old password hash on failure")
        void shouldNotMutateUser_onFailure() {
            when(passwordEncoder.matches("oldPass", "hash-old")).thenReturn(false);

            assertThatThrownBy(() -> usersService.changeCurrentUserPassword(user, request))
                    .isInstanceOf(ApiException.class);

            assertThat(user.getPassword()).isEqualTo("hash-old");
        }
    }

    // ================= patchCurrentUser =================

    @Nested
    @DisplayName("patchCurrentUser()")
    class PatchCurrentUserTests {

        @Test
        @DisplayName("should update fullName and updatedAt when provided")
        void shouldUpdateFullName_whenProvided() {
            UserPatchRequest request = new UserPatchRequest();
            request.setFullName("New FullName");
            UserResponse expected = new UserResponse();
            when(userMapper.toResponse(user)).thenReturn(expected);

            UserResponse actual = usersService.patchCurrentUser(user, request);

            assertThat(user.getFullName()).isEqualTo("New FullName");
            assertThat(user.getUpdatedAt()).isAfter(LocalDateTime.now().minusMinutes(1));
            assertThat(actual).isSameAs(expected);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("should keep fullName when patch field is null")
        void shouldKeepFullName_whenNull() {
            UserPatchRequest request = new UserPatchRequest();
            request.setFullName(null);
            UserResponse expected = new UserResponse();
            when(userMapper.toResponse(user)).thenReturn(expected);

            usersService.patchCurrentUser(user, request);

            assertThat(user.getFullName()).isEqualTo("Admin Adminov");
            verify(userRepository).save(user);
        }
    }

    // ================= toResponse =================

    @Nested
    @DisplayName("toResponse()")
    class ToResponseTests {

        @Test
        @DisplayName("should delegate to UserMapper")
        void shouldDelegateToMapper() {
            UserResponse expected = new UserResponse();
            expected.setLogin("admin");
            when(userMapper.toResponse(user)).thenReturn(expected);

            UserResponse actual = usersService.toResponse(user);

            assertThat(actual).isSameAs(expected);
            verify(userMapper).toResponse(user);
        }
    }

}