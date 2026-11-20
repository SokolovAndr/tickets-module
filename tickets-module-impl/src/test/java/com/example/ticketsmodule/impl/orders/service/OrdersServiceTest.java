package com.example.ticketsmodule.impl.orders.service;

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
import com.example.ticketsmodule.domain.order.repository.OrdersRepositoryPort;
import com.example.ticketsmodule.domain.route.model.Route;
import com.example.ticketsmodule.domain.ticket.exception.TicketNotFoundException;
import com.example.ticketsmodule.domain.ticket.model.Ticket;
import com.example.ticketsmodule.domain.ticket.model.TicketStatusEnum;
import com.example.ticketsmodule.domain.ticket.repository.TicketsRepositoryPort;
import com.example.ticketsmodule.domain.user.model.User;
import com.example.ticketsmodule.impl.order.conversion.OrderMapper;
import com.example.ticketsmodule.impl.order.service.OrdersService;
import com.example.ticketsmodule.impl.users.service.UsersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrdersService unit tests")
class OrdersServiceTest {

    @Mock private OrdersRepositoryPort ordersRepositoryPort;
    @Mock private TicketsRepositoryPort ticketsRepositoryPort;
    @Mock private UsersService usersService;
    @Mock private OrderMapper orderMapper;

    @InjectMocks
    private OrdersService ordersService;

    private UUID orderId;
    private UUID ticketId;
    private UUID routeId;
    private UUID userId;

    private User user;
    private Route route;
    private Ticket ticket;
    private Order order;
    private CreateOrderResponse expectedResponse;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        ticketId = UUID.randomUUID();
        routeId = UUID.randomUUID();
        userId = UUID.randomUUID();

        user = User.builder().id(userId).login("admin").build();
        route = Route.builder()
                .id(routeId)
                .departureAt(OffsetDateTime.now().plusDays(1))
                .build();
        ticket = Ticket.builder()
                .id(ticketId)
                .route(route)
                .seatNumber(1)
                .price(BigDecimal.valueOf(100))
                .ticketStatus(TicketStatusEnum.AVAILABLE)
                .build();
        order = Order.builder()
                .id(orderId)
                .user(user)
                .ticket(ticket)
                .status(OrderStatusEnum.WAITING)
                .price(BigDecimal.valueOf(100))
                .reservedUntil(OffsetDateTime.now().plusMinutes(15))
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        expectedResponse = new CreateOrderResponse();
    }

    // ---------- create ----------

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("should reserve ticket and return response")
        void shouldReserveTicketAndReturnResponse() {
            CreateOrderRequest request = new CreateOrderRequest();
            request.setTicketId(ticketId);

            when(usersService.findOneById(userId)).thenReturn(user);
            when(ticketsRepositoryPort.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(ticketsRepositoryPort.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
            when(ordersRepositoryPort.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
            when(orderMapper.toResponse(any(Order.class))).thenReturn(expectedResponse);

            CreateOrderResponse result = ordersService.create(request, userId);

            assertThat(result).isSameAs(expectedResponse);
            assertThat(ticket.getTicketStatus()).isEqualTo(TicketStatusEnum.RESERVED);
            verify(ticketsRepositoryPort).save(ticket);
            verify(ordersRepositoryPort).save(any(Order.class));
        }

        @Test
        @DisplayName("should throw when ticket not found")
        void shouldThrowWhenTicketNotFound() {
            CreateOrderRequest request = new CreateOrderRequest();
            request.setTicketId(ticketId);

            when(usersService.findOneById(userId)).thenReturn(user);
            when(ticketsRepositoryPort.findById(ticketId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ordersService.create(request, userId))
                    .isInstanceOf(TicketNotFoundException.class);

            verify(ticketsRepositoryPort, never()).save(any());
            verify(ordersRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("should throw when ticket is not AVAILABLE")
        void shouldThrowWhenTicketNotAvailable() {
            ticket.setTicketStatus(TicketStatusEnum.PURCHASED);
            CreateOrderRequest request = new CreateOrderRequest();
            request.setTicketId(ticketId);

            when(usersService.findOneById(userId)).thenReturn(user);
            when(ticketsRepositoryPort.findById(ticketId)).thenReturn(Optional.of(ticket));

            assertThatThrownBy(() -> ordersService.create(request, userId))
                    .isInstanceOf(TicketNotAvailableException.class);

            verify(ticketsRepositoryPort, never()).save(any());
            verify(ordersRepositoryPort, never()).save(any());
        }
    }

    // ---------- payOrder ----------

    @Nested
    @DisplayName("payOrder()")
    class PayOrderTests {

        @Test
        @DisplayName("should mark ticket as PURCHASED and order as PAID")
        void shouldPaySuccessfully() {
            when(ordersRepositoryPort.findById(orderId)).thenReturn(Optional.of(order));
            when(ticketsRepositoryPort.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
            when(ordersRepositoryPort.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
            when(orderMapper.toResponse(any(Order.class))).thenReturn(expectedResponse);

            CreateOrderResponse result = ordersService.payOrder(orderId, userId);

            assertThat(result).isSameAs(expectedResponse);
            assertThat(order.getStatus()).isEqualTo(OrderStatusEnum.PAID);
            assertThat(order.getPaidAt()).isNotNull();
            assertThat(ticket.getTicketStatus()).isEqualTo(TicketStatusEnum.PURCHASED);
            assertThat(ticket.getUser()).isEqualTo(user);
            assertThat(ticket.getPurchasedAt()).isNotNull();
        }

        @Test
        @DisplayName("should throw OrderAccessDeniedException for another user")
        void shouldThrowWhenNotOwner() {
            when(ordersRepositoryPort.findById(orderId)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> ordersService.payOrder(orderId, UUID.randomUUID()))
                    .isInstanceOf(OrderAccessDeniedException.class);

            verify(ordersRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("should throw OrderInvalidStatusException when status is not WAITING")
        void shouldThrowWhenWrongStatus() {
            order.setStatus(OrderStatusEnum.PAID);
            when(ordersRepositoryPort.findById(orderId)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> ordersService.payOrder(orderId, userId))
                    .isInstanceOf(OrderInvalidStatusException.class);

            verify(ordersRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("should throw OrderExpiredException when reservation expired")
        void shouldThrowWhenExpired() {
            order.setReservedUntil(OffsetDateTime.now().minusMinutes(1));
            when(ordersRepositoryPort.findById(orderId)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> ordersService.payOrder(orderId, userId))
                    .isInstanceOf(OrderExpiredException.class);

            verify(ordersRepositoryPort, never()).save(any());
        }
    }

    // ---------- cancelOrder ----------

    @Nested
    @DisplayName("cancelOrder()")
    class CancelOrderTests {

        @Test
        @DisplayName("should release ticket and set CANCELED status")
        void shouldCancelSuccessfully() {
            when(ordersRepositoryPort.findById(orderId)).thenReturn(Optional.of(order));
            when(ticketsRepositoryPort.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
            when(ordersRepositoryPort.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
            when(orderMapper.toResponse(any(Order.class))).thenReturn(expectedResponse);

            CreateOrderResponse result = ordersService.cancelOrder(orderId, userId);

            assertThat(result).isSameAs(expectedResponse);
            assertThat(order.getStatus()).isEqualTo(OrderStatusEnum.CANCELED);
            assertThat(order.getCanceledAt()).isNotNull();
            assertThat(ticket.getTicketStatus()).isEqualTo(TicketStatusEnum.AVAILABLE);
            assertThat(ticket.getUser()).isNull();
        }

        @Test
        @DisplayName("should throw OrderAccessDeniedException for another user")
        void shouldThrowWhenNotOwner() {
            when(ordersRepositoryPort.findById(orderId)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> ordersService.cancelOrder(orderId, UUID.randomUUID()))
                    .isInstanceOf(OrderAccessDeniedException.class);

            verify(ordersRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("should throw OrderInvalidStatusException for non-WAITING status")
        void shouldThrowWhenWrongStatus() {
            order.setStatus(OrderStatusEnum.CANCELED);
            when(ordersRepositoryPort.findById(orderId)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> ordersService.cancelOrder(orderId, userId))
                    .isInstanceOf(OrderInvalidStatusException.class);

            verify(ordersRepositoryPort, never()).save(any());
        }
    }

    // ---------- refundOrder ----------

    @Nested
    @DisplayName("refundOrder()")
    class RefundOrderTests {

        @Test
        @DisplayName("should release ticket and set REFUNDED status")
        void shouldRefundSuccessfully() {
            order.setStatus(OrderStatusEnum.PAID);
            order.setPaidAt(OffsetDateTime.now().minusHours(1));

            when(ordersRepositoryPort.findById(orderId)).thenReturn(Optional.of(order));
            when(ticketsRepositoryPort.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
            when(ordersRepositoryPort.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
            when(orderMapper.toResponse(any(Order.class))).thenReturn(expectedResponse);

            CreateOrderResponse result = ordersService.refundOrder(orderId, userId);

            assertThat(result).isSameAs(expectedResponse);
            assertThat(order.getStatus()).isEqualTo(OrderStatusEnum.REFUNDED);
            assertThat(order.getRefundedAt()).isNotNull();
            assertThat(ticket.getTicketStatus()).isEqualTo(TicketStatusEnum.AVAILABLE);
            assertThat(ticket.getUser()).isNull();
        }

        @Test
        @DisplayName("should throw OrderAccessDeniedException for another user")
        void shouldThrowWhenNotOwner() {
            when(ordersRepositoryPort.findById(orderId)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> ordersService.refundOrder(orderId, UUID.randomUUID()))
                    .isInstanceOf(OrderAccessDeniedException.class);
        }

        @Test
        @DisplayName("should throw OrderInvalidStatusException when status is not PAID")
        void shouldThrowWhenWrongStatus() {
            order.setStatus(OrderStatusEnum.WAITING);
            when(ordersRepositoryPort.findById(orderId)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> ordersService.refundOrder(orderId, userId))
                    .isInstanceOf(OrderInvalidStatusException.class);

            verify(ordersRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("should throw when route departure is in the past")
        void shouldThrowWhenAfterDeparture() {
            order.setStatus(OrderStatusEnum.PAID);
            route.setDepartureAt(OffsetDateTime.now().minusDays(1));

            when(ordersRepositoryPort.findById(orderId)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> ordersService.refundOrder(orderId, userId))
                    .isInstanceOf(OrderInvalidStatusException.class);

            verify(ordersRepositoryPort, never()).save(any());
        }
    }

    // ---------- findById / findResponseById / findAllMyOrders / findAll ----------

    @Nested
    @DisplayName("findById() / findResponseById()")
    class FindTests {

        @Test
        @DisplayName("findById should return order")
        void findByIdShouldReturn() {
            when(ordersRepositoryPort.findById(orderId)).thenReturn(Optional.of(order));

            assertThat(ordersService.findById(orderId)).isSameAs(order);
        }

        @Test
        @DisplayName("findById should throw when not found")
        void findByIdShouldThrowWhenNotFound() {
            when(ordersRepositoryPort.findById(orderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ordersService.findById(orderId))
                    .isInstanceOf(OrderNotFoundException.class);
        }

        @Test
        @DisplayName("findResponseById should return mapped response")
        void findResponseByIdShouldReturn() {
            when(ordersRepositoryPort.findById(orderId)).thenReturn(Optional.of(order));
            when(orderMapper.toResponse(order)).thenReturn(expectedResponse);

            assertThat(ordersService.findResponseById(orderId, userId)).isSameAs(expectedResponse);
        }

        @Test
        @DisplayName("findResponseById should throw AccessDenied for another user")
        void findResponseByIdShouldThrowForAnotherUser() {
            when(ordersRepositoryPort.findById(orderId)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> ordersService.findResponseById(orderId, UUID.randomUUID()))
                    .isInstanceOf(OrderAccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("findAllMyOrders()")
    class FindAllMyOrdersTests {

        @Test
        @DisplayName("should return mapped list")
        void shouldReturnMappedList() {
            when(ordersRepositoryPort.findByUserId(userId)).thenReturn(List.of(order));
            when(orderMapper.toResponse(order)).thenReturn(expectedResponse);

            List<CreateOrderResponse> result = ordersService.findAllMyOrders(userId);

            assertThat(result).containsExactly(expectedResponse);
            verify(ordersRepositoryPort).findByUserId(userId);
        }

        @Test
        @DisplayName("should return empty list when nothing found")
        void shouldReturnEmptyList() {
            when(ordersRepositoryPort.findByUserId(userId)).thenReturn(List.of());

            assertThat(ordersService.findAllMyOrders(userId)).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAll()")
    class FindAllTests {

        @Test
        @DisplayName("should map page")
        void shouldMapPage() {
            OrdersSearchRequest search = new OrdersSearchRequest();
            PageRequest pageReq = PageRequest.builder().page(0).size(10).build();

            PageResult<Order> domainPage = PageResult.<Order>builder()
                    .content(List.of(order))
                    .totalElements(1)
                    .totalPages(1)
                    .pageNumber(0)
                    .pageSize(10)
                    .build();

            when(ordersRepositoryPort.findAll(any(), eq(pageReq))).thenReturn(domainPage);
            when(orderMapper.toResponse(order)).thenReturn(expectedResponse);

            PageResult<CreateOrderResponse> result = ordersService.findAll(search, pageReq);

            assertThat(result.getContent()).containsExactly(expectedResponse);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("should handle null search request")
        void shouldHandleNullSearch() {
            PageRequest pageReq = PageRequest.builder().page(0).size(10).build();
            PageResult<Order> emptyPage = PageResult.<Order>builder()
                    .content(List.of())
                    .totalElements(0)
                    .build();

            when(ordersRepositoryPort.findAll(any(), eq(pageReq))).thenReturn(emptyPage);

            PageResult<CreateOrderResponse> result = ordersService.findAll(null, pageReq);

            assertThat(result.getContent()).isEmpty();
        }
    }

    // ---------- timeOutExpired ----------

    @Nested
    @DisplayName("timeOutExpired()")
    class TimeOutExpiredTests {

        @Test
        @DisplayName("should time out expired orders and release tickets")
        void shouldTimeOutExpiredOrders() {
            Order expired = Order.builder()
                    .id(UUID.randomUUID())
                    .user(user)
                    .ticket(ticket)
                    .status(OrderStatusEnum.WAITING)
                    .reservedUntil(OffsetDateTime.now().minusMinutes(5))
                    .build();

            when(ordersRepositoryPort.findExpiredWaiting(any(OffsetDateTime.class)))
                    .thenReturn(List.of(expired));
            when(ticketsRepositoryPort.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
            when(ordersRepositoryPort.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            ordersService.timeOutExpired();

            assertThat(expired.getStatus()).isEqualTo(OrderStatusEnum.TIME_OUT);
            assertThat(expired.getUpdatedAt()).isNotNull();
            assertThat(ticket.getTicketStatus()).isEqualTo(TicketStatusEnum.AVAILABLE);
            assertThat(ticket.getUser()).isNull();
            verify(ordersRepositoryPort).save(expired);
            verify(ticketsRepositoryPort).save(ticket);
        }

        @Test
        @DisplayName("should do nothing when no expired orders")
        void shouldDoNothingWhenNoneExpired() {
            when(ordersRepositoryPort.findExpiredWaiting(any(OffsetDateTime.class)))
                    .thenReturn(List.of());

            ordersService.timeOutExpired();

            verify(ordersRepositoryPort, never()).save(any());
            verify(ticketsRepositoryPort, never()).save(any());
        }
    }
}