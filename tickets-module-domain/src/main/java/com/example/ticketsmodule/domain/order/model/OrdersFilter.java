package com.example.ticketsmodule.domain.order.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdersFilter {
    private UUID ticketId;
    private UUID userId;
    private OrderStatusEnum status;
    private LocalDate createdDateFrom;
    private LocalDate createdDateTo;
    private LocalDate reservedUntilFrom;
    private LocalDate reservedUntilTo;
    private LocalDate paidDateFrom;
    private LocalDate paidDateTo;
    private LocalDate canceledDateFrom;
    private LocalDate canceledDateTo;
    private LocalDate refundedDateFrom;
    private LocalDate refundedDateTo;
}
