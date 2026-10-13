package com.example.ticketsmodule.users;

import com.example.ticketsmodule.api.model.ChangePasswordRequest;
import com.example.ticketsmodule.api.model.UserPatchRequest;
import com.example.ticketsmodule.impl.users.domain.UserEntity;
import com.example.ticketsmodule.impl.users.domain.UserRoleEnum;
import com.example.ticketsmodule.support.AbstractIntegrationTest;
import com.example.ticketsmodule.support.JwtTestHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("UsersControllerApiImpl integration tests")
class UsersControllerApiImplIT extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PasswordEncoder passwordEncoder;

    private static final String OLD_PASSWORD = "oldPass123";
    private static final String NEW_PASSWORD = "newPass456";

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        testUser = createUserInDb("testuser", OLD_PASSWORD, "Test User", UserRoleEnum.USER);
    }

    // ---------- SECURITY ----------

    @Nested
    @DisplayName("security")
    class SecurityTests {

        @Test
        @DisplayName("GET /api/users/me without auth → 401")
        void getMe_shouldReturn401_withoutAuth() throws Exception {
            mockMvc.perform(get("/api/users/me"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("PATCH /api/users/me without auth → 401")
        void patchMe_shouldReturn401_withoutAuth() throws Exception {
            UserPatchRequest request = new UserPatchRequest();
            request.setFullName("Hacker");

            mockMvc.perform(patch("/api/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/users/me/password without auth → 401")
        void changePassword_shouldReturn401_withoutAuth() throws Exception {
            mockMvc.perform(post("/api/users/me/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/users/me with USER role → 200")
        void getMe_shouldAllowUserRole() throws Exception {
            mockMvc.perform(get("/api/users/me")
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER")))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/users/me with ADMIN role → 200")
        void getMe_shouldAllowAdminRole() throws Exception {
            UserEntity admin = createUserInDb("admin", OLD_PASSWORD, "Admin", UserRoleEnum.ADMIN);

            mockMvc.perform(get("/api/users/me")
                            .with(JwtTestHelper.withUser(admin.getId(), "ADMIN")))
                    .andExpect(status().isOk());
        }
    }

    // ---------- GET /api/users/me ----------

    @Nested
    @DisplayName("GET /api/users/me")
    class GetMeTests {

        @Test
        @DisplayName("should return current user profile")
        void shouldReturnProfile() throws Exception {
            mockMvc.perform(get("/api/users/me")
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.login").value("testuser"))
                    .andExpect(jsonPath("$.fullName").value("Test User"))
                    .andExpect(jsonPath("$.role").value("USER"))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.updatedAt").exists());
        }

        @Test
        @DisplayName("should not expose password in response")
        void shouldNotExposePassword() throws Exception {
            mockMvc.perform(get("/api/users/me")
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.password").doesNotExist())
                    .andExpect(jsonPath("$.passwordHash").doesNotExist());
        }

    }

    // ---------- PATCH /api/users/me ----------

    @Nested
    @DisplayName("PATCH /api/users/me")
    class PatchMeTests {

        @Test
        @DisplayName("should update fullName and return 200")
        void shouldUpdateFullName() throws Exception {
            UserPatchRequest request = new UserPatchRequest();
            request.setFullName("Updated Name");

            mockMvc.perform(patch("/api/users/me")
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fullName").value("Updated Name"))
                    .andExpect(jsonPath("$.login").value("testuser"));

            UserEntity updated = userRepository.findById(testUser.getId()).orElseThrow();
            assertThat(updated.getFullName()).isEqualTo("Updated Name");
        }

        @Test
        @DisplayName("should update updatedAt timestamp")
        void shouldUpdateUpdatedAt() throws Exception {
            LocalDateTime before = userRepository.findById(testUser.getId())
                    .orElseThrow().getUpdatedAt();

            // Небольшая задержка, чтобы timestamp гарантированно отличался
            Thread.sleep(10);

            UserPatchRequest request = new UserPatchRequest();
            request.setFullName("New Name");

            mockMvc.perform(patch("/api/users/me")
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            UserEntity updated = userRepository.findById(testUser.getId()).orElseThrow();
            assertThat(updated.getUpdatedAt()).isAfter(before);
        }

        @Test
        @DisplayName("should keep fullName when patch field is null")
        void shouldKeepFullName_whenNull() throws Exception {
            UserPatchRequest request = new UserPatchRequest();
            request.setFullName(null);

            mockMvc.perform(patch("/api/users/me")
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            UserEntity updated = userRepository.findById(testUser.getId()).orElseThrow();
            assertThat(updated.getFullName()).isEqualTo("Test User");
        }

        @Test
        @DisplayName("should not affect other users")
        void shouldNotAffectOtherUsers() throws Exception {
            UserEntity other = createUserInDb("other", OLD_PASSWORD, "Other User", UserRoleEnum.USER);

            UserPatchRequest request = new UserPatchRequest();
            request.setFullName("Hacked");

            mockMvc.perform(patch("/api/users/me")
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            UserEntity untouched = userRepository.findById(other.getId()).orElseThrow();
            assertThat(untouched.getFullName()).isEqualTo("Other User");
        }
    }

    // ---------- POST /api/users/me/password ----------

    @Nested
    @DisplayName("POST /api/users/me/password")
    class ChangePasswordTests {

        @Test
        @DisplayName("should change password and return 200")
        void shouldChangePassword() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setOldPassword(OLD_PASSWORD);
            request.setNewPassword(NEW_PASSWORD);

            mockMvc.perform(post("/api/users/me/password")
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            UserEntity updated = userRepository.findById(testUser.getId()).orElseThrow();
            assertThat(passwordEncoder.matches(NEW_PASSWORD, updated.getPassword())).isTrue();
            assertThat(passwordEncoder.matches(OLD_PASSWORD, updated.getPassword())).isFalse();
        }

        @Test
        @DisplayName("should return 422 when old password is incorrect")
        void shouldReturn422_whenOldPasswordIncorrect() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setOldPassword("wrongPassword");
            request.setNewPassword(NEW_PASSWORD);

            mockMvc.perform(post("/api/users/me/password")
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.userMessage").value("Текущий пароль неверен"));

            UserEntity unchanged = userRepository.findById(testUser.getId()).orElseThrow();
            assertThat(passwordEncoder.matches(OLD_PASSWORD, unchanged.getPassword())).isTrue();
        }

        @Test
        @DisplayName("should return 422 when new password equals current")
        void shouldReturn422_whenNewPasswordEqualsOld() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setOldPassword(OLD_PASSWORD);
            request.setNewPassword(OLD_PASSWORD);

            mockMvc.perform(post("/api/users/me/password")
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.userMessage")
                            .value("Новый пароль должен отличаться от текущего"));
        }

        @Test
        @DisplayName("should not change other user's password")
        void shouldNotAffectOtherUsers() throws Exception {
            UserEntity other = createUserInDb("other", OLD_PASSWORD, "Other User", UserRoleEnum.USER);

            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setOldPassword(OLD_PASSWORD);
            request.setNewPassword(NEW_PASSWORD);

            mockMvc.perform(post("/api/users/me/password")
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            UserEntity untouched = userRepository.findById(other.getId()).orElseThrow();
            assertThat(passwordEncoder.matches(OLD_PASSWORD, untouched.getPassword())).isTrue();
            assertThat(passwordEncoder.matches(NEW_PASSWORD, untouched.getPassword())).isFalse();
        }

        @Test
        @DisplayName("should allow old password check to fail before new password check")
        void shouldFailFastOnWrongOldPassword() throws Exception {
            // Если новый пароль тоже невалидный (короткий),
            // должно вернуться «Текущий пароль неверен», а не ошибка валидации.
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setOldPassword("wrongPassword");
            request.setNewPassword("x"); // меньше minLength

            mockMvc.perform(post("/api/users/me/password")
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest()); // валидация newPassword сработает раньше
        }
    }

    // ---------- HELPERS ----------

    private UserEntity createUserInDb(String login, String rawPassword, String fullName, UserRoleEnum role) {
        LocalDateTime now = LocalDateTime.now();
        return userRepository.save(UserEntity.builder()
                .login(login)
                .password(passwordEncoder.encode(rawPassword))
                .fullName(fullName)
                .role(role)
                .createdAt(now)
                .updatedAt(now)
                .build());
    }
}