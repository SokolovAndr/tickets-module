package com.example.ticketsmodule.impl.tickets.exception;

import com.example.ticketsmodule.impl.common.web.rest.exceptions.ApiException;

import java.util.UUID;

public class TicketAlreadyPurchasedException extends ApiException {

    public TicketAlreadyPurchasedException(UUID ticketId) {
        super(
                "Ticket " + ticketId + " already purchased",  // для лога
                "Билет уже куплен"                              // для клиента
        );
    }
}