package com.example.ticketsmodule.impl.order.service;

import com.example.ticketsmodule.api.model.CreateCarrierRequest;
import com.example.ticketsmodule.api.model.CreateCarrierResponse;
import com.example.ticketsmodule.api.model.CreateOrderRequest;
import com.example.ticketsmodule.api.model.CreateOrderResponse;
import com.example.ticketsmodule.domain.carrier.exception.CarrierNameAlreadyExistsException;
import com.example.ticketsmodule.domain.carrier.model.Carrier;
import com.example.ticketsmodule.domain.order.exception.OrderNotFoundException;
import com.example.ticketsmodule.domain.order.model.Order;
import com.example.ticketsmodule.domain.order.model.OrderStatusEnum;
import com.example.ticketsmodule.domain.order.model.OrdersFilter;
import com.example.ticketsmodule.domain.order.repository.OrdersRepositoryPort;
import com.example.ticketsmodule.domain.ticket.exception.TicketNotFoundException;
import com.example.ticketsmodule.domain.ticket.model.Ticket;
import com.example.ticketsmodule.domain.ticket.model.TicketStatusEnum;
import com.example.ticketsmodule.domain.ticket.repository.TicketsRepositoryPort;
import com.example.ticketsmodule.domain.user.model.User;
import com.example.ticketsmodule.impl.order.conversion.OrderMapper;
import com.example.ticketsmodule.impl.tickets.service.TicketsService;
import com.example.ticketsmodule.impl.users.service.UsersService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сервис для работы с заказами
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class OrdersService {

    private final OrdersRepositoryPort ordersRepositoryPort;
    private final TicketsRepositoryPort ticketsRepositoryPort;
    private final TicketsService ticketsService;
    private final UsersService usersService;
    private final OrderMapper orderMapper;

    @Transactional
    public CreateOrderResponse create(@Valid CreateOrderRequest createOrderRequest, @NotNull UUID userId) {

        final Order order = orderMapper.toDomain(createOrderRequest);
        if (order == null) {
            throw new IllegalStateException("Failed to convert order entity");
        }

        final User user = usersService.findOneById(userId);

        final UUID ticketId = createOrderRequest.getTicketId();
        final Ticket ticket = ticketsService.findById(ticketId);
        ticket.setTicketStatus(TicketStatusEnum.RESERVED);
        ticket.setUser(user);
        final Ticket savedTicket = ticketsRepositoryPort.save(ticket);

        final LocalDateTime now = LocalDateTime.now();

        order.setUser(user);
        order.setTicket(savedTicket);
        order.setPrice(savedTicket.getPrice());
        order.setStatus(OrderStatusEnum.WAITING);
        order.setCreatedAt(now);
        order.setReservedUntil(now.plusMinutes(15));
        order.setUpdatedAt(now);

        return orderMapper.toResponse(ordersRepositoryPort.save(order));
    }

    @Transactional
    public CreateOrderResponse cancelOrder(@NotNull UUID id, @NotNull UUID userId) {

        final Order order = this.findById(id);

        if (userId != order.getUser().getId()) {
            return null;
        }

        final Ticket ticket = ticketsService.findById(order.getTicket().getId());
        ticket.setTicketStatus(TicketStatusEnum.AVAILABLE);
        ticket.setUser(null);
        ticket.setPurchasedAt(null);
        final Ticket savedTicket = ticketsRepositoryPort.save(ticket);

        final LocalDateTime now = LocalDateTime.now();

        order.setTicket(savedTicket);
        order.setStatus(OrderStatusEnum.CANCELED);
        order.setUpdatedAt(now);
        order.setCanceledAt(now);

        return orderMapper.toResponse(ordersRepositoryPort.save(order));
    }

    @Transactional
    public CreateOrderResponse refundOrder(@NotNull UUID id, @NotNull UUID userId) {

        final Order order = this.findById(id);

        if (userId != order.getUser().getId()) {
            return null;
        }

        final Ticket ticket = ticketsService.findById(order.getTicket().getId());
        ticket.setTicketStatus(TicketStatusEnum.AVAILABLE);
        ticket.setUser(null);
        ticket.setPurchasedAt(null);
        final Ticket savedTicket = ticketsRepositoryPort.save(ticket);

        final LocalDateTime now = LocalDateTime.now();

        order.setTicket(savedTicket);
        order.setStatus(OrderStatusEnum.REFUNDED);
        order.setUpdatedAt(now);
        order.setRefundedAt(now);

        return orderMapper.toResponse(ordersRepositoryPort.save(order));
    }

    @Transactional
    public CreateOrderResponse payOrder(@NotNull UUID id,  @NotNull UUID userId) {

        final LocalDateTime now = LocalDateTime.now();

        final Order order = this.findById(id);

        if (userId != order.getUser().getId()) {
            return null;
        }

        final Ticket ticket = ticketsService.findById(order.getTicket().getId());
        ticket.setTicketStatus(TicketStatusEnum.PURCHASED);
        ticket.setPurchasedAt(now);
        final Ticket savedTicket = ticketsRepositoryPort.save(ticket);

        order.setTicket(savedTicket);
        order.setStatus(OrderStatusEnum.PAID);
        order.setUpdatedAt(now);
        order.setPaidAt(now);

        return orderMapper.toResponse(ordersRepositoryPort.save(order));
    }

    @Transactional(readOnly = true)
    public Order findById(UUID id) {
       return ordersRepositoryPort.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Order findAllMyTickets(UUID userId) {

        return null;
    }

    @Transactional(readOnly = true)
    public CreateOrderResponse findResponseById(UUID id) {
        return orderMapper.toResponse(this.findById(id));
    }
}
