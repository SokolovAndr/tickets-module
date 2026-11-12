package com.example.ticketsmodule.impl.tickets.conversion;

import com.example.ticketsmodule.domain.ticket.model.TicketStatusEnum;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TicketStatusMapper {
    TicketStatusEnum toDomain(com.example.ticketsmodule.api.model.TicketStatusEnum request);

    com.example.ticketsmodule.api.model.TicketStatusEnum toResponse(TicketStatusEnum status);

}
