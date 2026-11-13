package com.example.ticketsmodule.domain.ticket.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketFilter {
    private String query;
    private UUID routeId;
    private Integer seatNumber;
    private BigDecimal priceFrom;
    private BigDecimal priceTo;
    private TicketStatusEnum ticketStatus;
    private UUID purchasedById;
    private LocalDate purchasedDateFrom;
    private LocalDate purchasedDateTo;
    private LocalDate createdDateFrom;
    private LocalDate createdDateTo;
}