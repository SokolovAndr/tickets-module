package com.example.ticketsmodule.impl.tickets.conversion;

import com.example.ticketsmodule.api.model.CreateTicketRequest;
import com.example.ticketsmodule.api.model.CreateTicketResponse;
import com.example.ticketsmodule.impl.routes.persistence.entity.RouteJpaEntity;
import com.example.ticketsmodule.impl.tickets.domain.TicketEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isPurchased", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "purchasedAt", ignore = true)
    TicketEntity toEntity (CreateTicketRequest request, RouteJpaEntity route);

    @Mapping(source = "route.id", target = "routeId")
    @Mapping(source = "user.id", target = "purchasedById")
    @Mapping(source = "purchased", target = "isPurchased")
    CreateTicketResponse toResponse (TicketEntity entity);
}
