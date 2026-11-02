package com.example.ticketsmodule.impl.tickets.persistence.mapper;

import com.example.ticketsmodule.domain.ticket.model.Ticket;
import com.example.ticketsmodule.impl.routes.persistence.mapper.RoutePersistenceMapper;
import com.example.ticketsmodule.impl.tickets.persistence.entity.TicketJpaEntity;
import com.example.ticketsmodule.impl.users.persistence.mapper.UserPersistenceMapper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {RoutePersistenceMapper.class, UserPersistenceMapper.class})
public interface TicketPersistenceMapper {
    Ticket toDomain(TicketJpaEntity entity);
    TicketJpaEntity toEntity(Ticket domain);
    List<Ticket> toDomainList(List<TicketJpaEntity> entities);
    List<TicketJpaEntity> toEntityList(List<Ticket> domains);
}