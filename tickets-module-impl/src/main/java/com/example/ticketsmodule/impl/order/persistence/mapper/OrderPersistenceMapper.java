package com.example.ticketsmodule.impl.order.persistence.mapper;

import com.example.ticketsmodule.domain.order.model.Order;
import com.example.ticketsmodule.impl.order.persistence.entity.OrderJpaEntity;
import com.example.ticketsmodule.impl.tickets.persistence.mapper.TicketPersistenceMapper;
import com.example.ticketsmodule.impl.users.persistence.mapper.UserPersistenceMapper;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Маппер для перевода Entity в Domain и обратно
 */

@Mapper(componentModel = "spring", uses = {TicketPersistenceMapper.class, UserPersistenceMapper.class})
public interface OrderPersistenceMapper {

    Order toDomain(OrderJpaEntity entity);

    OrderJpaEntity toEntity(Order domain);

    List<Order> toDomainList(List<OrderJpaEntity> entities);
}
