package com.example.ticketsmodule.domain.order.exception;

import java.util.UUID;

public class OrderAccessDeniedException extends RuntimeException {
    public OrderAccessDeniedException(UUID orderId, UUID userId) {
        super(String.format("Order %s does not belong to user %s", orderId, userId));
    }
}