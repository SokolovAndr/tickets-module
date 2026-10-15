package com.example.ticketsmodule.impl.users.conversion;

import com.example.ticketsmodule.api.model.UserResponse;
import com.example.ticketsmodule.impl.users.domain.UserEntity;
import com.example.ticketsmodule.impl.users.domain.UserRoleEnum;
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

    // ---------- маппинг полей ----------

    @Nested
    @DisplayName("field mapping")
    class FieldMappingTests {

        @Test
        @DisplayName("should map login")
        void shouldMapLogin() {
            UserEntity entity = baseUser().login("ivan_petrov").build();

            UserResponse response = mapper.toResponse(entity);

            assertThat(response.getLogin()).isEqualTo("ivan_petrov");
        }

        @Test
        @DisplayName("should map fullName")
        void shouldMapFullName() {
            UserEntity entity = baseUser().fullName("Иван Петров").build();

            UserResponse response = mapper.toResponse(entity);

            assertThat(response.getFullName()).isEqualTo("Иван Петров");
        }

        @Test
        @DisplayName("should map createdAt")
        void shouldMapCreatedAt() {
            UserEntity entity = baseUser().createdAt(CREATED).build();

            UserResponse response = mapper.toResponse(entity);

            assertThat(response.getCreatedAt()).isEqualTo(CREATED);
        }

        @Test
        @DisplayName("should map updatedAt")
        void shouldMapUpdatedAt() {
            UserEntity entity = baseUser().updatedAt(UPDATED).build();

            UserResponse response = mapper.toResponse(entity);

            assertThat(response.getUpdatedAt()).isEqualTo(UPDATED);
        }

        @Test
        @DisplayName("should map all fields together")
        void shouldMapAllFields() {
            UserEntity entity = baseUser()
                    .login("admin")
                    .fullName("Admin Adminov")
                    .role(UserRoleEnum.ADMIN)
                    .createdAt(CREATED)
                    .updatedAt(UPDATED)
                    .build();

            UserResponse response = mapper.toResponse(entity);

            assertThat(response.getLogin()).isEqualTo("admin");
            assertThat(response.getFullName()).isEqualTo("Admin Adminov");
            assertThat(response.getRole()).isEqualTo(UserResponse.RoleEnum.ADMIN);
            assertThat(response.getCreatedAt()).isEqualTo(CREATED);
            assertThat(response.getUpdatedAt()).isEqualTo(UPDATED);
        }
    }

    // ---------- маппинг role enum → String ----------

    @Nested
    @DisplayName("role enum mapping")
    class RoleMappingTests {

        @Test
        @DisplayName("should map USER enum to \"USER\" string")
        void shouldMapUserRole() {
            UserEntity entity = baseUser().role(UserRoleEnum.USER).build();

            UserResponse response = mapper.toResponse(entity);

            assertThat(response.getRole()).isEqualTo(UserResponse.RoleEnum.USER);
        }

        @Test
        @DisplayName("should map ADMIN enum to \"ADMIN\" string")
        void shouldMapAdminRole() {
            UserEntity entity = baseUser().role(UserRoleEnum.ADMIN).build();

            UserResponse response = mapper.toResponse(entity);

            assertThat(response.getRole()).isEqualTo(UserResponse.RoleEnum.ADMIN);
        }

        @Test
        @DisplayName("should return null role when entity role is null")
        void shouldReturnNullRole_whenEntityRoleNull() {
            UserEntity entity = baseUser().role(null).build();

            UserResponse response = mapper.toResponse(entity);

            assertThat(response.getRole()).isNull();
        }
    }

    // ---------- игнорируемые поля ----------

    @Nested
    @DisplayName("ignored fields")
    class IgnoredFieldsTests {

        @Test
        @DisplayName("should not expose password (target has no such field)")
        void shouldNotExposePassword() {
            UserEntity entity = baseUser().password("$2a$10$secretHash").build();

            UserResponse response = mapper.toResponse(entity);

            // В UserResponse нет поля password — проверяем через reflection,
            // что его там нет (защита от случайного добавления в DTO).
            assertThat(UserResponse.class.getDeclaredFields())
                    .extracting(f -> f.getName())
                    .doesNotContain("password");
        }

        @Test
        @DisplayName("should not expose id (target has no such field)")
        void shouldNotExposeId() {
            UserEntity entity = baseUser().id(UUID.randomUUID()).build();

            UserResponse response = mapper.toResponse(entity);

            assertThat(UserResponse.class.getDeclaredFields())
                    .extracting(f -> f.getName())
                    .doesNotContain("id");
        }
    }

    // ---------- edge cases ----------

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
            UserEntity entity = baseUser()
                    .login(null)
                    .fullName(null)
                    .build();

            UserResponse response = mapper.toResponse(entity);

            assertThat(response).isNotNull();
            assertThat(response.getLogin()).isNull();
            assertThat(response.getFullName()).isNull();
        }

        @Test
        @DisplayName("should preserve null dates")
        void shouldPreserveNullDates() {
            UserEntity entity = baseUser()
                    .createdAt(null)
                    .updatedAt(null)
                    .build();

            UserResponse response = mapper.toResponse(entity);

            assertThat(response.getCreatedAt()).isNull();
            assertThat(response.getUpdatedAt()).isNull();
        }

        @Test
        @DisplayName("should handle empty strings")
        void shouldHandleEmptyStrings() {
            UserEntity entity = baseUser()
                    .login("")
                    .fullName("")
                    .build();

            UserResponse response = mapper.toResponse(entity);

            assertThat(response.getLogin()).isEmpty();
            assertThat(response.getFullName()).isEmpty();
        }

        @Test
        @DisplayName("should preserve long fullName (255 chars)")
        void shouldPreserveLongFullName() {
            String longName = "A".repeat(255);
            UserEntity entity = baseUser().fullName(longName).build();

            UserResponse response = mapper.toResponse(entity);

            assertThat(response.getFullName()).hasSize(255);
        }

        @Test
        @DisplayName("should preserve cyrillic characters")
        void shouldPreserveCyrillic() {
            UserEntity entity = baseUser()
                    .fullName("Иванов Иван Иванович")
                    .build();

            UserResponse response = mapper.toResponse(entity);

            assertThat(response.getFullName()).isEqualTo("Иванов Иван Иванович");
        }
    }

    // ---------- полный сценарий ----------

    @Nested
    @DisplayName("full scenario")
    class FullScenarioTests {

        @Test
        @DisplayName("should map UserEntity to UserResponse completely")
        void shouldMapComplete() {
            UserEntity entity = UserEntity.builder()
                    .id(USER_ID)
                    .login("testuser")
                    .password("$2a$10$secretHash")
                    .fullName("Test User")
                    .role(UserRoleEnum.USER)
                    .createdAt(CREATED)
                    .updatedAt(UPDATED)
                    .build();

            UserResponse response = mapper.toResponse(entity);

            assertThat(response).isNotNull();
            assertThat(response.getLogin()).isEqualTo("testuser");
            assertThat(response.getFullName()).isEqualTo("Test User");
            assertThat(response.getRole()).isEqualTo(UserResponse.RoleEnum.USER);
            assertThat(response.getCreatedAt()).isEqualTo(CREATED);
            assertThat(response.getUpdatedAt()).isEqualTo(UPDATED);
        }
    }

    // ---------- helper ----------

    private UserEntity.UserEntityBuilder baseUser() {
        return UserEntity.builder()
                .id(USER_ID)
                .login("testuser")
                .password("hash")
                .fullName("Test User")
                .role(UserRoleEnum.USER)
                .createdAt(CREATED)
                .updatedAt(UPDATED);
    }
}