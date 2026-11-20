package com.example.ticketsmodule.impl.orders.conversion;

import com.example.ticketsmodule.api.model.CreateOrderResponse;
import com.example.ticketsmodule.domain.order.model.Order;
import com.example.ticketsmodule.domain.order.model.OrderStatusEnum;
import com.example.ticketsmodule.domain.route.model.Route;
import com.example.ticketsmodule.domain.ticket.model.Ticket;
import com.example.ticketsmodule.domain.ticket.model.TicketStatusEnum;
import com.example.ticketsmodule.domain.user.model.User;
import com.example.ticketsmodule.impl.order.conversion.OrderMapper;
import com.example.ticketsmodule.impl.order.conversion.OrderMapperImpl;
import com.example.ticketsmodule.impl.users.conversion.UserMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrderMapper unit tests")
class OrderMapperTest {

    private OrderMapper mapper;

    private UUID orderId;
    private UUID userId;
    private User user;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        OrderMapperImpl impl = new OrderMapperImpl();
        ReflectionTestUtils.setField(impl, "userMapper", new UserMapperImpl());
        mapper = impl;

        orderId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        UUID routeId = UUID.randomUUID();
        userId = UUID.randomUUID();

        Route route = Route.builder().id(routeId).build();
        user = User.builder().id(userId).login("admin").build();
        ticket = Ticket.builder()
                .id(ticketId)
                .route(route)
                .user(user)
                .seatNumber(7)
                .price(BigDecimal.valueOf(120))
                .ticketStatus(TicketStatusEnum.PURCHASED)
                .build();
    }

    @Nested
    @DisplayName("toResponse()")
    class ToResponseTests {

        @Test
        @DisplayName("should map direct order fields")
        void shouldMapDirectFields() {
            OffsetDateTime now = OffsetDateTime.now();
            OffsetDateTime reserved = now.plusMinutes(15);
            OffsetDateTime paid = now.minusHours(1);
            OffsetDateTime canceled = now.minusHours(2);
            OffsetDateTime refunded = now.minusMinutes(30);

            Order order = Order.builder()
                    .id(orderId)
                    .ticket(ticket)
                    .user(user)
                    .status(OrderStatusEnum.PAID)
                    .price(BigDecimal.valueOf(120))
                    .reservedUntil(reserved)
                    .paidAt(paid)
                    .canceledAt(canceled)
                    .refundedAt(refunded)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            CreateOrderResponse response = mapper.toResponse(order);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(orderId);
            assertThat(response.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(120));
            assertThat(response.getReservedUntil()).isEqualTo(reserved);
            assertThat(response.getPaidAt()).isEqualTo(paid);
            assertThat(response.getCanceledAt()).isEqualTo(canceled);
            assertThat(response.getRefundedAt()).isEqualTo(refunded);
            assertThat(response.getCreatedAt()).isEqualTo(now);
            assertThat(response.getUpdatedAt()).isEqualTo(now);
            assertThat(response.getUser()).isNotNull();
            assertThat(response.getUser().getId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("should map ticket.route.id to ticket.routeId")
        void shouldMapTicketRouteId() {
            UUID customRouteId = UUID.randomUUID();
            Route customRoute = Route.builder().id(customRouteId).build();
            ticket.setRoute(customRoute);

            Order order = Order.builder()
                    .id(orderId)
                    .ticket(ticket)
                    .user(user)
                    .price(BigDecimal.ONE)
                    .build();

            CreateOrderResponse response = mapper.toResponse(order);

            assertThat(response.getTicket()).isNotNull();
            assertThat(response.getTicket().getRouteId()).isEqualTo(customRouteId);
        }

        @Test
        @DisplayName("should map ticket.user.id to ticket.purchasedById")
        void shouldMapTicketPurchasedById() {
            UUID customUserId = UUID.randomUUID();
            ticket.setUser(User.builder().id(customUserId).build());

            Order order = Order.builder()
                    .id(orderId)
                    .ticket(ticket)
                    .user(user)
                    .price(BigDecimal.ONE)
                    .build();

            CreateOrderResponse response = mapper.toResponse(order);

            assertThat(response.getTicket()).isNotNull();
            assertThat(response.getTicket().getPurchasedById()).isEqualTo(customUserId);
        }

        @Test
        @DisplayName("should map ticket.seatNumber and price")
        void shouldMapTicketSeatAndPrice() {
            Order order = Order.builder()
                    .id(orderId)
                    .ticket(ticket)
                    .user(user)
                    .price(BigDecimal.ONE)
                    .build();

            CreateOrderResponse response = mapper.toResponse(order);

            assertThat(response.getTicket()).isNotNull();
            assertThat(response.getTicket().getSeatNumber()).isEqualTo(7);
            assertThat(response.getTicket().getPrice()).isEqualByComparingTo(BigDecimal.valueOf(120));
        }

        @Test
        @DisplayName("should return null ticket when order.ticket is null")
        void shouldReturnNullTicketWhenSourceTicketIsNull() {
            Order order = Order.builder()
                    .id(orderId)
                    .ticket(null)
                    .user(user)
                    .price(BigDecimal.ONE)
                    .build();

            CreateOrderResponse response = mapper.toResponse(order);

            assertThat(response).isNotNull();
            assertThat(response.getTicket()).isNull();
        }

        @Test
        @DisplayName("should set purchasedById=null when ticket.user is null")
        void shouldSetPurchasedByIdNullWhenTicketUserIsNull() {
            ticket.setUser(null);

            Order order = Order.builder()
                    .id(orderId)
                    .ticket(ticket)
                    .user(user)
                    .price(BigDecimal.ONE)
                    .build();

            CreateOrderResponse response = mapper.toResponse(order);

            assertThat(response.getTicket().getPurchasedById()).isNull();
        }

        @Test
        @DisplayName("should preserve null timestamps")
        void shouldPreserveNullTimestamps() {
            Order order = Order.builder()
                    .id(orderId)
                    .ticket(ticket)
                    .user(user)
                    .price(BigDecimal.ONE)
                    .reservedUntil(null)
                    .paidAt(null)
                    .canceledAt(null)
                    .refundedAt(null)
                    .createdAt(null)
                    .updatedAt(null)
                    .build();

            CreateOrderResponse response = mapper.toResponse(order);

            assertThat(response.getReservedUntil()).isNull();
            assertThat(response.getPaidAt()).isNull();
            assertThat(response.getCanceledAt()).isNull();
            assertThat(response.getRefundedAt()).isNull();
            assertThat(response.getCreatedAt()).isNull();
            assertThat(response.getUpdatedAt()).isNull();
        }

        @Test
        @DisplayName("should return null when source is null")
        void shouldReturnNullWhenSourceNull() {
            CreateOrderResponse response = mapper.toResponse(null);
            assertThat(response).isNull();
        }
    }
}