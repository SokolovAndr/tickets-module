package com.example.ticketsmodule.domain.user.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(UUID id) {
        super(String.format("User with id %s not found", id));
    }

    public UserNotFoundException(String login) {
        super(String.format("User with login %s not found", login));
    }
}