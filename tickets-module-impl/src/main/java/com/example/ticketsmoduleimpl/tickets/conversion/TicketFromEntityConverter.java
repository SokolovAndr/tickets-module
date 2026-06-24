package com.example.ticketsmoduleimpl.tickets.conversion;

import com.example.ticketsmoduleimpl.tickets.domain.TicketEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import tickets.model.CreateTicketResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketFromEntityConverter implements Converter<TicketEntity, CreateTicketResponse> {

    @Override
    public CreateTicketResponse convert(TicketEntity source) {
        var response = new CreateTicketResponse();

        response.setId(source.getId());
        response.setRouteId(source.getRoute().getId());
        response.setSeatNumber(source.getSeatNumber());
        response.setPrice(source.getPrice());
        response.setIsPurchased(source.isPurchased());
        response.setPurchasedById(source.getUser() != null ? source.getUser().getId() : null);
        response.setPurchasedAt(source.getPurchasedAt());
        response.setCreatedAt(source.getCreatedAt());
        response.setUpdatedAt(source.getUpdatedAt());

        return response;
    }
}
