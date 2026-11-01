package com.example.ticketsmodule.domain.common.exception;

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