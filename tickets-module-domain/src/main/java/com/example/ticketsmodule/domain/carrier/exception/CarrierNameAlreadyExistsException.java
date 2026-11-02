package com.example.ticketsmodule.domain.carrier.exception;

public class CarrierNameAlreadyExistsException extends RuntimeException {
    public CarrierNameAlreadyExistsException(String name) {
        super(String.format("Carrier with name '%s' already exists", name));
    }
}