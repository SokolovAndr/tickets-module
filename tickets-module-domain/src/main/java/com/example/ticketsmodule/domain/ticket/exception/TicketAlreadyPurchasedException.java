package com.example.ticketsmodule.domain.ticket.exception;

import java.util.UUID;

public class TicketAlreadyPurchasedException extends RuntimeException {
    public TicketAlreadyPurchasedException(UUID ticketId) {
        super(String.format("Ticket %s already purchased", ticketId));
    }
}