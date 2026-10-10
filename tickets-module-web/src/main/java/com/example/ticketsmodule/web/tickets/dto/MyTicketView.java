package com.example.ticketsmodule.web.tickets.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class MyTicketView {

    private UUID id;
    private int seatNumber;
    private BigDecimal price;
    private LocalDateTime purchasedAt;

    private String departurePoint;
    private LocalDateTime departureAt;
    private String destinationPoint;
    private LocalDateTime destinationAt;
}