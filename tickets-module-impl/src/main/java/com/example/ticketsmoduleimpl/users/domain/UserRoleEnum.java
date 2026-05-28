package com.example.ticketsmoduleimpl.users.domain;

import com.example.ticketsmoduleimpl.common.domain.enumeration.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Тип роли USER/ADMIN
 */

@Getter
@RequiredArgsConstructor
public enum UserRoleEnum implements BaseEnum {
    USER(1, "Обычный пользователь"), ADMIN(2, "Администратор");

    private final Integer id;
    private final String description;

    private static final Map<Integer, UserRoleEnum> USER_ROLE_BY_ID_MAP = Arrays.stream(values()).collect(Collectors.toUnmodifiableMap(UserRoleEnum::getId, Function.identity()));

    @NonNull
    public static UserRoleEnum get(@NonNull final Integer id) {
        return Optional.ofNullable(USER_ROLE_BY_ID_MAP.get(id)).orElseThrow(() -> new IllegalArgumentException(String.format("Неизвестный id=%s", id)));
    }
}
