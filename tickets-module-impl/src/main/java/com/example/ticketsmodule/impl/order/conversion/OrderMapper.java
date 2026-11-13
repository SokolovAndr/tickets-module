package com.example.ticketsmodule.impl.order.conversion;

import com.example.ticketsmodule.api.model.CreateOrderResponse;
import com.example.ticketsmodule.domain.order.model.Order;
import com.example.ticketsmodule.impl.tickets.conversion.TicketMapper;
import com.example.ticketsmodule.impl.users.conversion.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {TicketMapper.class, UserMapper.class})
public interface OrderMapper {

    @Mapping(source = "ticket.route.id", target = "ticket.routeId")
    @Mapping(source = "ticket.user.id", target = "ticket.purchasedById")
    CreateOrderResponse toResponse(Order entity);
}
