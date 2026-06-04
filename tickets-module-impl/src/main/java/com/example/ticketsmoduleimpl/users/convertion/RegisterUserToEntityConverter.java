package com.example.ticketsmoduleimpl.users.convertion;

import com.example.ticketsmoduleimpl.users.domain.UserEntity;
import com.example.ticketsmoduleimpl.users.domain.UserRoleEnum;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tickets.model.RegisterRequest;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterUserToEntityConverter implements Converter<RegisterRequest, UserEntity> {

    private final PasswordEncoder passwordEncoder;

    @Override
    @NonNull
    public UserEntity convert(RegisterRequest source) {

        final String passwordEncrypted = passwordEncoder.encode(source.getPassword());

        return UserEntity.builder()
                .login(source.getLogin())
                .password(passwordEncrypted)
                .fullName(source.getFullName())
                .role(UserRoleEnum.USER)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
    }

}
