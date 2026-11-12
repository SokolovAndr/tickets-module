package com.example.ticketsmodule.domain.order.repository;

import com.example.ticketsmodule.domain.common.PageRequest;
import com.example.ticketsmodule.domain.common.PageResult;
import com.example.ticketsmodule.domain.order.model.Order;
import com.example.ticketsmodule.domain.order.model.OrderStatusEnum;
import com.example.ticketsmodule.domain.order.model.OrdersFilter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrdersRepositoryPort {

    Order save(Order order);

    Optional<Order> findById(UUID id);

    Optional<Order> findActiveByTicketId(UUID ticketId);

    List<Order> findByTicketId(UUID ticketId);

    List<Order> findByUserId(UUID userId);

    List<Order> findByUserIdAndStatus(UUID userId, OrderStatusEnum status);

    List<Order> findExpiredWaiting(LocalDateTime now);

    PageResult<Order> findAll(OrdersFilter filter, PageRequest page);

}
