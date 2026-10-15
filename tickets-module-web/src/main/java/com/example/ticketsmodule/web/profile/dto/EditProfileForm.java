package com.example.ticketsmodule.web.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EditProfileForm {

    @NotBlank(message = "ФИО обязательно")
    @Size(max = 255, message = "ФИО не должно превышать 255 символов")
    private String fullName;
}