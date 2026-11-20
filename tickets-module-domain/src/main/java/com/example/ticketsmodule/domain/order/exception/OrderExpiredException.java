package com.example.ticketsmodule.domain.order.exception;

import java.util.UUID;

public class OrderExpiredException extends RuntimeException {
    public OrderExpiredException(UUID orderId) {
        super(String.format("Order %s reservation has expired", orderId));
    }
}