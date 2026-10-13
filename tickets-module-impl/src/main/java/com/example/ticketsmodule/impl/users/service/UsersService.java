package com.example.ticketsmodule.impl.users.service;

import com.example.ticketsmodule.api.model.ChangePasswordRequest;
import com.example.ticketsmodule.api.model.UserPatchRequest;
import com.example.ticketsmodule.api.model.UserResponse;
import com.example.ticketsmodule.impl.common.web.rest.exceptions.ApiException;
import com.example.ticketsmodule.impl.users.conversion.UserMapper;
import com.example.ticketsmodule.impl.users.domain.UserEntity;
import com.example.ticketsmodule.impl.users.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsersService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserEntity findOneById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User with id " + id + " not found"));
    }

    @Transactional
    public void changeCurrentUserPassword(UserEntity user, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new ApiException("Current password incorrect", "Текущий пароль неверен");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new ApiException("New password equals current", "Новый пароль должен отличаться от текущего");
        }
        final String passwordEncrypted = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(passwordEncrypted);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserResponse toResponse(UserEntity user) {
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse patchCurrentUser(UserEntity user, UserPatchRequest request) {
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return userMapper.toResponse(user);
    }
}
