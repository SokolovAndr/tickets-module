package com.example.ticketsmodule.web.registration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistrationForm {

    @NotBlank(message = "Логин обязателен")
    @Size(min = 5, max = 20, message = "Логин должен быть от 5 до 20 символов")
    private String login;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 5, max = 20, message = "Пароль должен быть от 5 до 20 символов")
    private String password;

    @NotBlank(message = "Подтверждение пароля обязательно")
    private String confirmPassword;

    @NotBlank(message = "ФИО обязательно")
    private String fullName;
}