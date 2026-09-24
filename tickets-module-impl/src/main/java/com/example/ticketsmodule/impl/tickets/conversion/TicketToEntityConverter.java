package com.example.ticketsmodule.impl.tickets.conversion;

import com.example.ticketsmodule.impl.routes.domain.RouteEntity;
import com.example.ticketsmodule.impl.tickets.domain.TicketEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.example.ticketsmodule.api.model.CreateTicketRequest;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketToEntityConverter {
    public TicketEntity convert(CreateTicketRequest source, RouteEntity route) {
        return TicketEntity.builder()
                .route(route)
                .seatNumber(source.getSeatNumber())
                .price(source.getPrice())
                .isPurchased(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
