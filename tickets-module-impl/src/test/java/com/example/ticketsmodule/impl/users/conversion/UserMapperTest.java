package com.example.ticketsmodule.impl.users.conversion;

import com.example.ticketsmodule.api.model.UserResponse;
import com.example.ticketsmodule.domain.user.model.User;
import com.example.ticketsmodule.domain.user.model.UserRoleEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserMapper unit tests")
class UserMapperTest {

    private UserMapper mapper;

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final LocalDateTime CREATED = LocalDateTime.of(2026, 1, 1, 10, 0);
    private static final LocalDateTime UPDATED = LocalDateTime.of(2026, 6, 1, 15, 30);

    @BeforeEach
    void setUp() {
        mapper = new UserMapperImpl();
    }

    @Nested
    @DisplayName("field mapping")
    class FieldMappingTests {

        @Test
        @DisplayName("should map login")
        void shouldMapLogin() {
            User entity = baseUser();
            entity.setLogin("ivan_petrov"); // Используем сеттер вместо билдера

            UserResponse response = mapper.toResponse(entity);

            assertThat(response.getLogin()).isEqualTo("ivan_petrov");
        }

        @Test
        @DisplayName("should map all fields together")
        void shouldMapAllFields() {
            User entity = baseUser();
            entity.setLogin("admin");
            entity.setFullName("Admin Adminov");
            entity.setRole(UserRoleEnum.ADMIN);

            UserResponse response = mapper.toResponse(entity);

            assertThat(response.getLogin()).isEqualTo("admin");
            assertThat(response.getFullName()).isEqualTo("Admin Adminov");
            assertThat(response.getRole()).isEqualTo(UserResponse.RoleEnum.ADMIN);
            assertThat(response.getCreatedAt()).isEqualTo(CREATED);
            assertThat(response.getUpdatedAt()).isEqualTo(UPDATED);
        }
    }

    @Nested
    @DisplayName("edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should return null when source is null")
        void shouldReturnNull_whenSourceNull() {
            UserResponse response = mapper.toResponse(null);

            assertThat(response).isNull();
        }

        @Test
        @DisplayName("should preserve null strings")
        void shouldPreserveNullStrings() {
            User entity = baseUser();
            entity.setLogin(null);
            entity.setFullName(null);

            UserResponse response = mapper.toResponse(entity);

            assertThat(response).isNotNull();
            assertThat(response.getLogin()).isNull();
            assertThat(response.getFullName()).isNull();
        }
    }

    private User baseUser() {
        return User.builder()
                .id(USER_ID)
                .login("testuser")
                .password("hash")
                .fullName("Test User")
                .role(UserRoleEnum.USER)
                .createdAt(CREATED)
                .updatedAt(UPDATED)
                .build();
    }
}