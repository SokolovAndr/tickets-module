package com.example.ticketsmodule.domain.order.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdersFilter {
    private UUID ticketId;
    private UUID userId;
    private BigDecimal price;
    private OrderStatusEnum status;
    private OffsetDateTime createdDateFrom;
    private OffsetDateTime createdDateTo;
    private OffsetDateTime reservedUntilFrom;
    private OffsetDateTime reservedUntilTo;
    private OffsetDateTime paidDateFrom;
    private OffsetDateTime paidDateTo;
    private OffsetDateTime canceledDateFrom;
    private OffsetDateTime canceledDateTo;
    private OffsetDateTime refundedDateFrom;
    private OffsetDateTime refundedDateTo;
}
