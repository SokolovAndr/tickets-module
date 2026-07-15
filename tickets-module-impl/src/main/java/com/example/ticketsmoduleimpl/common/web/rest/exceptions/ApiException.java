package com.example.ticketsmoduleimpl.common.web.rest.exceptions;

public class ApiException extends RuntimeException {

    private final String userMessage;

    public ApiException(String message, Throwable cause, String userMessage) {
        super(message, cause);
        this.userMessage = userMessage;
    }

    public ApiException(String message, String userMessage) {
        super(message);
        this.userMessage = userMessage;
    }

    public String getUserMessage() {
        return userMessage;
    }
}
