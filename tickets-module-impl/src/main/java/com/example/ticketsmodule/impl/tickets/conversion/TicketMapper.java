package com.example.ticketsmodule.impl.tickets.conversion;

import com.example.ticketsmodule.api.model.CreateTicketRequest;
import com.example.ticketsmodule.api.model.CreateTicketResponse;
import com.example.ticketsmodule.domain.ticket.model.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TicketMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "purchased", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "purchasedAt", ignore = true)
    @Mapping(target = "route", ignore = true)
    Ticket toDomain(CreateTicketRequest request);

    @Mapping(source = "route.id", target = "routeId")
    @Mapping(source = "user.id", target = "purchasedById")
    @Mapping(source = "purchased", target = "isPurchased")
    CreateTicketResponse toResponse(Ticket ticket);
}