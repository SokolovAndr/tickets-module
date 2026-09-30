package com.example.ticketsmodule.web.registration.controller;

import com.example.ticketsmodule.api.model.RegisterRequest;
import com.example.ticketsmodule.impl.oauth.service.AuthService;
import com.example.ticketsmodule.web.registration.dto.RegistrationForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class RegistrationController {

    private final AuthService authService;
    private static final String REGISTER_PATH = "register";

    @GetMapping("/register")
    public String showForm(Model model) {
        model.addAttribute("form", new RegistrationForm());
        return REGISTER_PATH;
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegistrationForm form,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {

        if (form.getPassword() != null && !form.getPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", "Пароли не совпадают");
        }

        if (bindingResult.hasErrors()) {
            return REGISTER_PATH;
        }

        RegisterRequest request = new RegisterRequest();
        request.setLogin(form.getLogin());
        request.setPassword(form.getPassword());
        request.setFullName(form.getFullName());

        try {
            authService.register(request);
        } catch (BadCredentialsException e) {
            bindingResult.rejectValue("login", "login.taken", e.getMessage());
            return REGISTER_PATH;
        }

        redirectAttributes.addFlashAttribute("successMessage", "Регистрация прошла успешно! Теперь войдите в систему.");
        return "redirect:/login";
    }

}
