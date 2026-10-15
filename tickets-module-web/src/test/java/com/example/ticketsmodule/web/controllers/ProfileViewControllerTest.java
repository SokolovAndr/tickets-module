package com.example.ticketsmodule.web.controllers;

import com.example.ticketsmodule.api.model.ChangePasswordRequest;
import com.example.ticketsmodule.api.model.UserPatchRequest;
import com.example.ticketsmodule.api.model.UserResponse;
import com.example.ticketsmodule.impl.oauth.service.SessionCurrentUserService;
import com.example.ticketsmodule.impl.users.domain.UserEntity;
import com.example.ticketsmodule.impl.users.domain.UserRoleEnum;
import com.example.ticketsmodule.impl.users.service.UsersService;
import com.example.ticketsmodule.web.profile.controller.ProfileViewController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MVC-тесты для {@link ProfileViewController}.
 *
 * <p>{@code @WebMvcTest} поднимает только web-слой: DispatcherServlet,
 * контроллер, Thymeleaf, конвертеры. Security-фильтры отключены через
 * {@code addFilters = false}.
 *
 * <p>{@code UsersService} и {@code SessionCurrentUserService} замоканы —
 * тестируем поведение контроллера, а не бизнес-логику.
 */
@WebMvcTest(ProfileViewController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ProfileViewController MVC tests")
class ProfileViewControllerTest {

    private static final String BR_PROFILE_FORM =
            "org.springframework.validation.BindingResult.profileForm";
    private static final String BR_PASSWORD_FORM =
            "org.springframework.validation.BindingResult.passwordForm";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsersService usersService;

    @MockBean
    private SessionCurrentUserService sessionCurrentUserService;

    private UUID userId;
    private UserEntity user;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        user = UserEntity.builder()
                .id(userId)
                .login("testuser")
                .password("encodedPassword")
                .fullName("Иванов Иван Иванович")
                .role(UserRoleEnum.USER)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();

        userResponse = new UserResponse();

        when(sessionCurrentUserService.getCurrentUserId()).thenReturn(userId);
        when(usersService.findOneById(userId)).thenReturn(user);
        when(usersService.toResponse(any(UserEntity.class))).thenReturn(userResponse);
    }

    // ==================================================================
    //  GET /profile
    // ==================================================================

    @Test
    @DisplayName("GET /profile returns view with user, profileForm and passwordForm")
    void showProfile_ShouldReturnProfileView() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user", "profileForm", "passwordForm"))
                .andExpect(model().attribute("user", userResponse));

        verify(usersService).findOneById(userId);
        verify(usersService).toResponse(user);
    }

    @Test
    @DisplayName("GET /profile prefills profileForm with current fullName")
    void showProfile_ShouldPrefillProfileForm() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(model().attribute("profileForm",
                        hasProperty("fullName", is("Иванов Иван Иванович"))));
    }

    @Test
    @DisplayName("GET /profile does not overwrite profileForm from flash")
    void showProfile_ShouldNotOverwriteFlashForm() throws Exception {
        UserPatchRequest flashForm = new UserPatchRequest();
        flashForm.setFullName("Петров Пётр Петрович");

        mockMvc.perform(get("/profile").flashAttr("profileForm", flashForm))
                .andExpect(model().attribute("profileForm",
                        hasProperty("fullName", is("Петров Пётр Петрович"))));
    }

    // ==================================================================
    //  POST /profile/edit
    // ==================================================================

    @Test
    @DisplayName("POST /profile/edit success redirects with successMessage")
    void editProfile_ShouldRedirectOnSuccess() throws Exception {
        mockMvc.perform(post("/profile/edit")
                        .param("fullName", "Сидоров Сидор Сидорович"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("successMessage", "Профиль успешно обновлён"));

        ArgumentCaptor<UserPatchRequest> captor =
                ArgumentCaptor.forClass(UserPatchRequest.class);
        verify(usersService).patchCurrentUser(eq(user), captor.capture());
        assertThat(captor.getValue().getFullName()).isEqualTo("Сидоров Сидор Сидорович");
    }

    @Test
    @DisplayName("POST /profile/edit with blank fullName returns to profile with validation error")
    void editProfile_WhenFullNameBlank_ShouldReturnWithErrors() throws Exception {
        mockMvc.perform(post("/profile/edit").param("fullName", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attributeExists(BR_PROFILE_FORM))
                .andExpect(flash().attributeExists("profileForm"));

        verify(usersService, never()).patchCurrentUser(any(), any());
    }

    @Test
    @DisplayName("POST /profile/edit with whitespace fullName returns to profile with validation error")
    void editProfile_WhenFullNameWhitespace_ShouldReturnWithErrors() throws Exception {
        mockMvc.perform(post("/profile/edit").param("fullName", "   "))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists(BR_PROFILE_FORM));

        verify(usersService, never()).patchCurrentUser(any(), any());
    }

    @Test
    @DisplayName("POST /profile/edit redirects with errorMessage when service throws")
    void editProfile_WhenServiceThrows_ShouldRedirectWithError() throws Exception {
        doThrow(new IllegalStateException("DB down"))
                .when(usersService).patchCurrentUser(any(), any());

        mockMvc.perform(post("/profile/edit").param("fullName", "Петров"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("errorMessage", "DB down"));
    }

    // ==================================================================
    //  POST /profile/password
    // ==================================================================

    @Test
    @DisplayName("POST /profile/password success redirects with successMessage")
    void changePassword_ShouldRedirectOnSuccess() throws Exception {
        mockMvc.perform(post("/profile/password")
                        .param("oldPassword", "oldPass")
                        .param("newPassword", "newPass")
                        .param("confirmPassword", "newPass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("successMessage", "Пароль успешно изменён"));

        ArgumentCaptor<ChangePasswordRequest> captor =
                ArgumentCaptor.forClass(ChangePasswordRequest.class);
        verify(usersService).changeCurrentUserPassword(eq(user), captor.capture());
        assertThat(captor.getValue().getOldPassword()).isEqualTo("oldPass");
        assertThat(captor.getValue().getNewPassword()).isEqualTo("newPass");
    }

    @Test
    @DisplayName("POST /profile/password when passwords don't match returns to profile with field error")
    void changePassword_WhenPasswordsDontMatch_ShouldReturnWithErrors() throws Exception {
        mockMvc.perform(post("/profile/password")
                        .param("oldPassword", "oldPass")
                        .param("newPassword", "newPass")
                        .param("confirmPassword", "differentPass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attributeExists(BR_PASSWORD_FORM))
                .andExpect(flash().attributeExists("passwordForm"));

        verify(usersService, never()).changeCurrentUserPassword(any(), any());
    }

    @Test
    @DisplayName("POST /profile/password with blank fields returns to profile with validation error")
    void changePassword_WhenFieldsBlank_ShouldReturnWithErrors() throws Exception {
        mockMvc.perform(post("/profile/password")
                        .param("oldPassword", "")
                        .param("newPassword", "")
                        .param("confirmPassword", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists(BR_PASSWORD_FORM));

        verify(usersService, never()).changeCurrentUserPassword(any(), any());
    }

    @Test
    @DisplayName("POST /profile/password with too short new password returns to profile with validation error")
    void changePassword_WhenNewPasswordTooShort_ShouldReturnWithErrors() throws Exception {
        mockMvc.perform(post("/profile/password")
                        .param("oldPassword", "oldPass")
                        .param("newPassword", "123")
                        .param("confirmPassword", "123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists(BR_PASSWORD_FORM));

        verify(usersService, never()).changeCurrentUserPassword(any(), any());
    }

    @Test
    @DisplayName("POST /profile/password redirects with errorMessage when service throws")
    void changePassword_WhenServiceThrows_ShouldRedirectWithError() throws Exception {
        doThrow(new IllegalStateException("Current password incorrect"))
                .when(usersService).changeCurrentUserPassword(any(), any());

        mockMvc.perform(post("/profile/password")
                        .param("oldPassword", "wrongOld")
                        .param("newPassword", "newPass")
                        .param("confirmPassword", "newPass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("errorMessage", "Current password incorrect"))
                .andExpect(flash().attributeExists("passwordForm"));
    }
}