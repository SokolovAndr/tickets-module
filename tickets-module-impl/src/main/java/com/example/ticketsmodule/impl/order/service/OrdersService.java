package com.example.ticketsmodule.impl.order.service;

import com.example.ticketsmodule.api.model.CreateOrderRequest;
import com.example.ticketsmodule.api.model.CreateOrderResponse;
import com.example.ticketsmodule.api.model.OrdersSearchRequest;
import com.example.ticketsmodule.domain.common.PageRequest;
import com.example.ticketsmodule.domain.common.PageResult;
import com.example.ticketsmodule.domain.order.exception.OrderAccessDeniedException;
import com.example.ticketsmodule.domain.order.exception.OrderExpiredException;
import com.example.ticketsmodule.domain.order.exception.OrderInvalidStatusException;
import com.example.ticketsmodule.domain.order.exception.OrderNotFoundException;
import com.example.ticketsmodule.domain.order.exception.TicketNotAvailableException;
import com.example.ticketsmodule.domain.order.model.Order;
import com.example.ticketsmodule.domain.order.model.OrderStatusEnum;
import com.example.ticketsmodule.domain.order.model.OrdersFilter;
import com.example.ticketsmodule.domain.order.repository.OrdersRepositoryPort;
import com.example.ticketsmodule.domain.ticket.model.Ticket;
import com.example.ticketsmodule.domain.ticket.model.TicketStatusEnum;
import com.example.ticketsmodule.domain.ticket.repository.TicketsRepositoryPort;
import com.example.ticketsmodule.domain.user.model.User;
import com.example.ticketsmodule.impl.order.conversion.OrderMapper;
import com.example.ticketsmodule.impl.users.service.UsersService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.example.ticketsmodule.domain.ticket.model.TicketStatusEnum.AVAILABLE;

/**
 * Сервис для работы с заказами
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class OrdersService {

    private final OrdersRepositoryPort ordersRepositoryPort;
    private final TicketsRepositoryPort ticketsRepositoryPort;
    private final UsersService usersService;
    private final OrderMapper orderMapper;

    private static final String USER_TICKETS_CACHE = "userTickets";

    @Transactional
    public CreateOrderResponse create(CreateOrderRequest req, @NotNull UUID userId) {

        final User user = usersService.findOneById(userId);

        final Ticket ticket = ticketsRepositoryPort.findById(req.getTicketId())
                .orElseThrow(() -> new TicketNotAvailableException(req.getTicketId(), null));

        if (ticket.getTicketStatus() != AVAILABLE) {
            throw new TicketNotAvailableException(ticket.getId(), ticket.getTicketStatus());
        }

        ticket.setTicketStatus(TicketStatusEnum.RESERVED);
        ticketsRepositoryPort.save(ticket);

        final LocalDateTime now = LocalDateTime.now();

        final Order order = new Order();

        order.setUser(user);
        order.setTicket(ticket);
        order.setPrice(ticket.getPrice());
        order.setStatus(OrderStatusEnum.WAITING);
        order.setCreatedAt(now);
        order.setReservedUntil(now.plusMinutes(15));
        order.setUpdatedAt(now);

        return orderMapper.toResponse(ordersRepositoryPort.save(order));
    }

    @Transactional
    @CacheEvict(value = USER_TICKETS_CACHE, key = "#userId")
    public CreateOrderResponse payOrder(@NotNull UUID id,  @NotNull UUID userId) {

        final Order order = loadAndCheckOwner(id, userId);

        if (order.getStatus() != OrderStatusEnum.WAITING) {
            throw new OrderInvalidStatusException(id, order.getStatus(), "PAID");
        }

        final LocalDateTime now = LocalDateTime.now();
        if (!now.isBefore(order.getReservedUntil())) {
            throw new OrderExpiredException(id);
        }

        final Ticket ticket = order.getTicket();
        ticket.setTicketStatus(TicketStatusEnum.PURCHASED);
        ticket.setUser(order.getUser());
        ticket.setPurchasedAt(now);
        ticketsRepositoryPort.save(ticket);

        order.setTicket(ticket);
        order.setStatus(OrderStatusEnum.PAID);
        order.setUpdatedAt(now);
        order.setPaidAt(now);

        return orderMapper.toResponse(ordersRepositoryPort.save(order));
    }

    @Transactional
    public CreateOrderResponse cancelOrder(@NotNull UUID id, @NotNull UUID userId) {

        final Order order = loadAndCheckOwner(id, userId);

        if (order.getStatus() != OrderStatusEnum.WAITING) {
            throw new OrderInvalidStatusException(id, order.getStatus(), OrderStatusEnum.CANCELED.getDescription());
        }

        final Ticket ticket = order.getTicket();
        ticket.setTicketStatus(AVAILABLE);
        ticket.setUser(null);
        ticket.setPurchasedAt(null);
        ticketsRepositoryPort.save(ticket);

        final LocalDateTime now = LocalDateTime.now();

        order.setTicket(ticket);
        order.setStatus(OrderStatusEnum.CANCELED);
        order.setUpdatedAt(now);
        order.setCanceledAt(now);

        return orderMapper.toResponse(ordersRepositoryPort.save(order));
    }

    @Transactional
    @CacheEvict(value = USER_TICKETS_CACHE, key = "#userId")
    public CreateOrderResponse refundOrder(@NotNull UUID id, @NotNull UUID userId) {

        final Order order = loadAndCheckOwner(id, userId);

        if (order.getStatus() != OrderStatusEnum.PAID) {
            throw new OrderInvalidStatusException(id, order.getStatus(), OrderStatusEnum.REFUNDED.getDescription());
        }

        if (order.getTicket().getRoute().getDepartureAt().isBefore(LocalDateTime.now())) {
            throw new OrderInvalidStatusException(id, order.getStatus(), "REFUNDED_AFTER_DEPARTURE");
        }

        final Ticket ticket = order.getTicket();
        ticket.setTicketStatus(AVAILABLE);
        ticket.setUser(null);
        ticket.setPurchasedAt(null);
        ticketsRepositoryPort.save(ticket);

        final LocalDateTime now = LocalDateTime.now();

        order.setTicket(ticket);
        order.setStatus(OrderStatusEnum.REFUNDED);
        order.setUpdatedAt(now);
        order.setRefundedAt(now);

        return orderMapper.toResponse(ordersRepositoryPort.save(order));
    }

    @Transactional(readOnly = true)
    public Order findById(UUID id) {
       return ordersRepositoryPort.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<CreateOrderResponse> findAllMyOrders(UUID userId) {
        return ordersRepositoryPort.findByUserId(userId).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CreateOrderResponse findResponseById(UUID id, UUID userId) {
        return orderMapper.toResponse(loadAndCheckOwner(id, userId));
    }

    @Transactional(readOnly = true)
    public PageResult<CreateOrderResponse> findAll(OrdersSearchRequest req, PageRequest page) {
        OrdersFilter filter = toFilter(req);
        PageResult<Order> result = ordersRepositoryPort.findAll(filter, page);
        return PageResult.<CreateOrderResponse>builder()
                .content(result.getContent().stream().map(orderMapper::toResponse).toList())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .pageNumber(result.getPageNumber())
                .pageSize(result.getPageSize())
                .build();
    }

    @Transactional
    public void timeOutExpired() {
        final LocalDateTime now = LocalDateTime.now();
        final List<Order> expired = ordersRepositoryPort.findExpiredWaiting(now);
        for (Order order : expired) {
            order.setStatus(OrderStatusEnum.TIME_OUT);
            order.setUpdatedAt(now);
            final Ticket ticket = order.getTicket();
            ticket.setTicketStatus(AVAILABLE);
            ticket.setUser(null);
            ticket.setPurchasedAt(null);
            ticketsRepositoryPort.save(ticket);
            ordersRepositoryPort.save(order);
        }
        if (!expired.isEmpty()) {
            log.info("Timed out {} expired orders", expired.size());
        }
    }

    private Order loadAndCheckOwner(UUID id, UUID userId) {
        final Order order = findById(id);
        if (!userId.equals(order.getUser().getId())) {
            throw new OrderAccessDeniedException(id, userId);
        }
        return order;
    }

    private OrdersFilter toFilter(OrdersSearchRequest req) {
        if (req == null) return OrdersFilter.builder().build();
        return OrdersFilter.builder()
                .ticketId(req.getTicketId())
                .userId(req.getUserId())
                .status(req.getStatus() == null ? null
                        : OrderStatusEnum.valueOf(req.getStatus().getValue()))
                .createdDateFrom(req.getCreatedDateFrom())
                .createdDateTo(req.getCreatedDateTo())
                .reservedUntilFrom(req.getReservedUntilFrom())
                .reservedUntilTo(req.getReservedUntilTo())
                .paidDateFrom(req.getPaidDateFrom())
                .paidDateTo(req.getPaidDateTo())
                .canceledDateFrom(req.getCanceledDateFrom())
                .canceledDateTo(req.getCanceledDateTo())
                .refundedDateFrom(req.getRefundedDateFrom())
                .refundedDateTo(req.getRefundedDateTo())
                .build();
    }
}
