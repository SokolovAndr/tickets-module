package com.example.ticketsmodule.impl.users.service;

import com.example.ticketsmodule.api.model.ChangePasswordRequest;
import com.example.ticketsmodule.api.model.UserPatchRequest;
import com.example.ticketsmodule.api.model.UserResponse;
import com.example.ticketsmodule.domain.user.exception.UserNotFoundException;
import com.example.ticketsmodule.domain.user.model.User;
import com.example.ticketsmodule.domain.user.repository.UsersRepositoryPort;
import com.example.ticketsmodule.impl.common.web.rest.exceptions.ApiException;
import com.example.ticketsmodule.impl.users.conversion.UserMapper;
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
    private final UsersRepositoryPort usersRepositoryPort;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public User findOneById(UUID id) {
        return usersRepositoryPort.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional
    public void changeCurrentUserPassword(User user, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new ApiException("Current password incorrect", "Текущий пароль неверен");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new ApiException("New password equals current", "Новый пароль должен отличаться от текущего");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        usersRepositoryPort.save(user);
    }

    @Transactional(readOnly = true)
    public UserResponse toResponse(User user) {
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse patchCurrentUser(User user, UserPatchRequest request) {
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        user.setUpdatedAt(LocalDateTime.now());
        return userMapper.toResponse(usersRepositoryPort.save(user));
    }
}