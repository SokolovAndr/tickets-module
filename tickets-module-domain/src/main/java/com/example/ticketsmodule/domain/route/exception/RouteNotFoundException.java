package com.example.ticketsmodule.domain.route.exception;

import java.util.UUID;

public class RouteNotFoundException extends RuntimeException {
    public RouteNotFoundException(UUID id) {
        super(String.format("Route with id %s not found", id));
    }
}