package com.example.ticketsmodule.web.controllers;

import com.example.ticketsmodule.api.model.RegisterRequest;
import com.example.ticketsmodule.impl.oauth.service.AuthService;
import com.example.ticketsmodule.web.registration.controller.RegistrationController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * MVC-тесты для {@link RegistrationController}.
 *
 * <p>{@code @WebMvcTest} поднимает только web-слой:
 * DispatcherServlet, контроллеры, Thymeleaf, конвертеры.
 * Security-фильтры отключены через {@code addFilters = false}.
 *
 * <p>{@code AuthService} замокан через {@code @MockBean} — тестируем
 * поведение контроллера, а не бизнес-логику.
 */
@WebMvcTest(RegistrationController.class)
@AutoConfigureMockMvc(addFilters = false)
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("GET /register → отдаёт view 'register' с пустой формой в модели")
    void showForm_ShouldReturnRegisterView() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("form"));
    }

    @Test
    @DisplayName("POST /register с валидными данными → редирект на /login")
    void register_WithValidData_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(post("/register")
                        .param("login", "testuser")
                        .param("fullName", "Test User")
                        .param("password", "secret123")
                        .param("confirmPassword", "secret123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("POST /register, пароли не совпадают → форма с ошибкой в confirmPassword")
    void register_WhenPasswordsDontMatch_ShouldReturnRegisterView() throws Exception {
        mockMvc.perform(post("/register")
                        .param("login", "testuser")
                        .param("fullName", "Test User")
                        .param("password", "secret123")
                        .param("confirmPassword", "different"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("form", "confirmPassword"));
    }

    @Test
    @DisplayName("POST /register, логин занят → форма с ошибкой в login")
    void register_WhenLoginTaken_ShouldReturnRegisterViewWithError() throws Exception {
        doThrow(new BadCredentialsException("Login is already occupied"))
                .when(authService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/register")
                        .param("login", "existing")
                        .param("fullName", "Test")
                        .param("password", "secret123")
                        .param("confirmPassword", "secret123"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("form", "login"));
    }
}