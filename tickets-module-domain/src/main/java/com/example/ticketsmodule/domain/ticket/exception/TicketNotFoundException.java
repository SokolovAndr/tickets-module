package com.example.ticketsmodule.domain.ticket.exception;

import java.util.UUID;

public class TicketNotFoundException extends RuntimeException {
    public TicketNotFoundException(UUID id) {
        super(String.format("Ticket with id %s not found", id));
    }
}
