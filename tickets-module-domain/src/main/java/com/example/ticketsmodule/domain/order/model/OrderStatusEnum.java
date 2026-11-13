package com.example.ticketsmodule.domain.order.model;

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
 * Статус заказа
 */

@Getter
@RequiredArgsConstructor
public enum OrderStatusEnum implements BaseEnum {

    WAITING(1, "Ожидание оплаты"),
    TIME_OUT(2, "Время на оплату истекло"),
    PAID(3, "Оплачен"),
    CANCELED(4, "Отменен до оплаты"),
    REFUNDED(5, "Возвращен после оплаты");

    private final Integer id;
    private final String description;

    private static final Map<Integer, OrderStatusEnum> ORDER_STATUS_BY_ID_MAP = Arrays.stream(values()).collect(Collectors.toUnmodifiableMap(OrderStatusEnum::getId, Function.identity()));

    @NonNull
    public static OrderStatusEnum get(@NonNull final Integer id) {
        return Optional.ofNullable(ORDER_STATUS_BY_ID_MAP.get(id)).orElseThrow(() -> new IllegalArgumentException(String.format("Неизвестный id=%s", id)));
    }
}
