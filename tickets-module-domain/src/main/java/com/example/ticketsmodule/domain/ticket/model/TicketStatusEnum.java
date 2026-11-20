package com.example.ticketsmodule.domain.ticket.model;

import com.example.ticketsmodule.domain.common.enumeration.BaseEnum;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Статус билета
 */

@Getter
@RequiredArgsConstructor
public enum TicketStatusEnum implements BaseEnum {

    AVAILABLE(1, "Доступен для покупки"),
    RESERVED(2, "Зарезервирован"),
    PURCHASED(3, "Куплен");

    private final Integer id;
    private final String description;

    private static final Map<Integer, TicketStatusEnum> TICKET_STATUS_BY_ID_MAP = Arrays.stream(values()).collect(Collectors.toUnmodifiableMap(TicketStatusEnum::getId, Function.identity()));

    @NonNull
    public static TicketStatusEnum get(@NonNull final Integer id) {
        return Optional.ofNullable(TICKET_STATUS_BY_ID_MAP.get(id)).orElseThrow(() -> new IllegalArgumentException(String.format("Неизвестный id=%s", id)));
    }
}