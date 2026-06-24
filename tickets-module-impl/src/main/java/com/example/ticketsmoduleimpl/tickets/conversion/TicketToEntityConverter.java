package com.example.ticketsmoduleimpl.tickets.conversion;

import com.example.ticketsmoduleimpl.routes.domain.RouteEntity;
import com.example.ticketsmoduleimpl.tickets.domain.TicketEntity;
import com.example.ticketsmoduleimpl.users.domain.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tickets.model.CreateTicketRequest;

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
