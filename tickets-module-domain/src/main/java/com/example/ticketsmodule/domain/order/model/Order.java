package com.example.ticketsmodule.domain.order.model;

import com.example.ticketsmodule.domain.common.model.CommonModel;
import com.example.ticketsmodule.domain.ticket.model.Ticket;
import com.example.ticketsmodule.domain.user.model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Order extends CommonModel {
    private Ticket ticket;
    private User user;
    private OrderStatusEnum status;
    private BigDecimal price;

    private OffsetDateTime reservedUntil;
    private OffsetDateTime paidAt;
    private OffsetDateTime canceledAt;
    private OffsetDateTime refundedAt;

}
