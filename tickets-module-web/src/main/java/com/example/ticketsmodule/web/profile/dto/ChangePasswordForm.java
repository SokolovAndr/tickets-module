package com.example.ticketsmodule.web.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordForm {

    @NotBlank(message = "Введите текущий пароль")
    private String oldPassword;

    @NotBlank(message = "Введите новый пароль")
    @Size(min = 5, max = 20, message = "Пароль должен быть от 5 до 20 символов")
    private String newPassword;

    @NotBlank(message = "Подтвердите новый пароль")
    private String confirmPassword;
}