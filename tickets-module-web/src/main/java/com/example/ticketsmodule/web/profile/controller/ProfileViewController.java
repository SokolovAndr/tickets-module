package com.example.ticketsmodule.web.profile.controller;

import com.example.ticketsmodule.api.model.ChangePasswordRequest;
import com.example.ticketsmodule.api.model.UserPatchRequest;
import com.example.ticketsmodule.impl.oauth.service.SessionCurrentUserService;
import com.example.ticketsmodule.impl.users.domain.UserEntity;
import com.example.ticketsmodule.impl.users.service.UsersService;
import com.example.ticketsmodule.web.profile.dto.ChangePasswordForm;
import com.example.ticketsmodule.web.profile.dto.EditProfileForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileViewController {

    private final UsersService usersService;
    private final SessionCurrentUserService sessionCurrentUserService;

    @GetMapping
    public String showProfile(Model model) {
        UserEntity user = currentUser();

        if (!model.containsAttribute("profileForm")) {
            EditProfileForm profileForm = new EditProfileForm();
            profileForm.setFullName(user.getFullName());
            model.addAttribute("profileForm", profileForm);
        }
        if (!model.containsAttribute("passwordForm")) {
            model.addAttribute("passwordForm", new ChangePasswordForm());
        }
        model.addAttribute("user", usersService.toResponse(user));
        return "profile";
    }

    @PostMapping("/edit")
    public String editProfile(@Valid @ModelAttribute("profileForm") EditProfileForm form,
                              BindingResult br,
                              RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute(
                    "org.springframework.validation.BindingResult.profileForm", br);
            ra.addFlashAttribute("profileForm", form);
            ra.addFlashAttribute("errorMessage", "Проверьте правильность заполнения полей");
            return "redirect:/profile";
        }

        try {
            // Form → API-DTO: мостик между web-слоем и сервисом
            UserPatchRequest request = new UserPatchRequest();
            request.setFullName(form.getFullName());
            usersService.patchCurrentUser(currentUser(), request);

            ra.addFlashAttribute("successMessage", "Профиль успешно обновлён");
        } catch (Exception e) {
            log.warn("Failed to update profile: {}", e.getMessage());
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/password")
    public String changePassword(@Valid @ModelAttribute("passwordForm") ChangePasswordForm form,
                                 BindingResult br,
                                 RedirectAttributes ra) {
        if (form.getNewPassword() != null
                && !form.getNewPassword().equals(form.getConfirmPassword())) {
            br.rejectValue("confirmPassword", "password.mismatch", "Пароли не совпадают");
        }
        if (br.hasErrors()) {
            ra.addFlashAttribute(
                    "org.springframework.validation.BindingResult.passwordForm", br);
            ra.addFlashAttribute("passwordForm", form);
            ra.addFlashAttribute("errorMessage", "Проверьте правильность заполнения полей");
            return "redirect:/profile";
        }

        try {
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setOldPassword(form.getOldPassword());
            request.setNewPassword(form.getNewPassword());
            usersService.changeCurrentUserPassword(currentUser(), request);
            ra.addFlashAttribute("successMessage", "Пароль успешно изменён");
        } catch (Exception e) {
            log.warn("Failed to change password: {}", e.getMessage());
            ra.addFlashAttribute("errorMessage", e.getMessage());
            ra.addFlashAttribute("passwordForm", form);
        }
        return "redirect:/profile";
    }

    private UserEntity currentUser() {
        return usersService.findOneById(sessionCurrentUserService.getCurrentUserId());
    }
}