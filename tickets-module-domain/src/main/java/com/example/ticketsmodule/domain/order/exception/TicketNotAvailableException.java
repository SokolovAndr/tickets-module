package com.example.ticketsmodule.domain.order.exception;

import com.example.ticketsmodule.domain.ticket.model.TicketStatusEnum;

import java.util.UUID;

public class TicketNotAvailableException extends RuntimeException {
    public TicketNotAvailableException(UUID ticketId, TicketStatusEnum status) {
        super(String.format("Ticket %s is not available, status = %s", ticketId, status));
    }
}
