package com.example.ticketsmoduleimpl.common.domain.exception;

import lombok.Getter;

@Getter
public class EnumException extends RuntimeException {

    public EnumException(String message, Throwable cause) {
        super(message, cause);
    }

    public EnumException(String message) {
        super(message);
    }
}