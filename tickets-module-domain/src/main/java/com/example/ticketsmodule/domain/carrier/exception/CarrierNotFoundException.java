package com.example.ticketsmodule.domain.carrier.exception;

import java.util.UUID;

public class CarrierNotFoundException extends RuntimeException {
    public CarrierNotFoundException(UUID id) {
        super(String.format("Carrier with id %s not found", id));
    }
}