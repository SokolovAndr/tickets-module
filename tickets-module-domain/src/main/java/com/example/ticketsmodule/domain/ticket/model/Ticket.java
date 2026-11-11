package com.example.ticketsmodule.domain.ticket.model;

import com.example.ticketsmodule.domain.common.model.CommonModel;
import com.example.ticketsmodule.domain.route.model.Route;
import com.example.ticketsmodule.domain.user.model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Ticket extends CommonModel {
    private Route route;
    private int seatNumber;
    private BigDecimal price;
    private boolean purchased;
    private User user;
    private LocalDateTime purchasedAt;
}
