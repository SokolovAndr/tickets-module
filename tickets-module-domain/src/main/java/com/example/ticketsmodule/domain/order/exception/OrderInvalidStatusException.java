package com.example.ticketsmodule.domain.order.exception;

import com.example.ticketsmodule.domain.order.model.OrderStatusEnum;

import java.util.UUID;

public class OrderInvalidStatusException extends RuntimeException {
    public OrderInvalidStatusException(UUID orderId, OrderStatusEnum current, String action) {
        super(String.format("Order %s cannot be %s in status %s", orderId, action, current));
    }
}