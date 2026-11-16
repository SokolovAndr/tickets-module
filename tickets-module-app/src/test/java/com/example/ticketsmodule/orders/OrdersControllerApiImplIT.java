package com.example.ticketsmodule.orders;

import com.example.ticketsmodule.api.model.CreateOrderRequest;
import com.example.ticketsmodule.domain.carrier.model.Carrier;
import com.example.ticketsmodule.domain.carrier.repository.CarriersRepositoryPort;
import com.example.ticketsmodule.domain.order.model.Order;
import com.example.ticketsmodule.domain.order.model.OrderStatusEnum;
import com.example.ticketsmodule.domain.order.repository.OrdersRepositoryPort;
import com.example.ticketsmodule.domain.route.model.Route;
import com.example.ticketsmodule.domain.route.repository.RoutesRepositoryPort;
import com.example.ticketsmodule.domain.ticket.model.Ticket;
import com.example.ticketsmodule.domain.ticket.model.TicketStatusEnum;
import com.example.ticketsmodule.domain.ticket.repository.TicketsRepositoryPort;
import com.example.ticketsmodule.domain.user.model.User;
import com.example.ticketsmodule.domain.user.model.UserRoleEnum;
import com.example.ticketsmodule.domain.user.repository.UsersRepositoryPort;
import com.example.ticketsmodule.support.AbstractIntegrationTest;
import com.example.ticketsmodule.support.JwtTestHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционные тесты для OrdersControllerApiImpl.
 * Контракт сверен с api.yaml / types.yaml.
 * Коды ответов соответствуют спецификации:
 *   - 201 — создание заказа
 *   - 409 — билет уже зарезервирован/куплен
 *   - 404 — объект не найден
 *   - 422 — бизнес-правило нарушено (неверный статус/истекло время/после отправления)
 *   - 403 — попытка доступа к чужому заказу
 */
@DisplayName("OrdersControllerApiImpl integration tests")
class OrdersControllerApiImplIT extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private CarriersRepositoryPort carriersRepositoryPort;
    @Autowired private RoutesRepositoryPort routesRepositoryPort;
    @Autowired private TicketsRepositoryPort ticketsRepositoryPort;
    @Autowired private OrdersRepositoryPort ordersRepositoryPort;
    @Autowired private UsersRepositoryPort usersRepositoryPort;

    private UUID routeId;
    private User testUser;
    private User otherUser;
    private final AtomicInteger seatCounter = new AtomicInteger(1);

    @BeforeEach
    void setUp() {
        cleanDatabase();
        seatCounter.set(1);

        routeId = createRouteInDb();
        testUser = createUserInDb("testuser");
        otherUser = createUserInDb("otheruser");
    }

    // ---------- SECURITY ----------

    @Nested
    @DisplayName("security")
    class SecurityTests {

        @Test
        @DisplayName("GET /api/orders/my without auth → 401")
        void shouldReturn401WithoutAuthOnMy() throws Exception {
            mockMvc.perform(get("/api/orders/my"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/orders without auth → 401")
        void shouldReturn401WithoutAuthOnCreate() throws Exception {
            CreateOrderRequest request = new CreateOrderRequest();
            request.setTicketId(UUID.randomUUID());

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/orders without auth → 401")
        void shouldReturn401WithoutAuthOnList() throws Exception {
            mockMvc.perform(get("/api/orders"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ---------- CREATE ----------

    @Nested
    @DisplayName("POST /api/orders")
    class CreateOrderTests {

        @Test
        @DisplayName("should create order, reserve ticket and return 201")
        void shouldCreateOrder() throws Exception {
            Ticket ticket = createTicket(TicketStatusEnum.AVAILABLE, null);

            CreateOrderRequest request = new CreateOrderRequest();
            request.setTicketId(ticket.getId());

            mockMvc.perform(post("/api/orders")
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.status").value("WAITING"))
                    .andExpect(jsonPath("$.price").value(150.00))
                    .andExpect(jsonPath("$.reservedUntil").exists())
                    .andExpect(jsonPath("$.user.id").value(testUser.getId().toString()))
                    .andExpect(jsonPath("$.ticket.id").value(ticket.getId().toString()))
                    .andExpect(jsonPath("$.ticket.routeId").value(routeId.toString()))
                    .andExpect(jsonPath("$.ticket.ticketStatus").value("RESERVED"));

            Ticket reserved = ticketsRepositoryPort.findById(ticket.getId()).orElseThrow();
            assertThat(reserved.getTicketStatus()).isEqualTo(TicketStatusEnum.RESERVED);

            var orders = ordersRepositoryPort.findByUserId(testUser.getId());
            assertThat(orders).hasSize(1);
            assertThat(orders.get(0).getStatus()).isEqualTo(OrderStatusEnum.WAITING);
        }

        @Test
        @DisplayName("should return 409 when ticket already purchased")
        void shouldReturn409WhenTicketNotAvailable() throws Exception {
            Ticket ticket = createTicket(TicketStatusEnum.PURCHASED, otherUser);

            CreateOrderRequest request = new CreateOrderRequest();
            request.setTicketId(ticket.getId());

            mockMvc.perform(post("/api/orders")
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());

            assertThat(ordersRepositoryPort.findByUserId(testUser.getId())).isEmpty();
        }

        @Test
        @DisplayName("should return 409 when ticket already reserved")
        void shouldReturn409WhenTicketReserved() throws Exception {
            Ticket ticket = createTicket(TicketStatusEnum.RESERVED, null);

            CreateOrderRequest request = new CreateOrderRequest();
            request.setTicketId(ticket.getId());

            mockMvc.perform(post("/api/orders")
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("should return 404 when ticket not found")
        void shouldReturn404WhenTicketNotFound() throws Exception {
            CreateOrderRequest request = new CreateOrderRequest();
            request.setTicketId(UUID.randomUUID());

            mockMvc.perform(post("/api/orders")
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    // ---------- PAY ----------

    @Nested
    @DisplayName("POST /api/orders/{id}/pay")
    class PayOrderTests {

        @Test
        @DisplayName("should pay WAITING order, mark ticket as PURCHASED")
        void shouldPayOrder() throws Exception {
            Ticket ticket = createTicket(TicketStatusEnum.RESERVED, null);
            Order order = createOrder(testUser, ticket, OrderStatusEnum.WAITING,
                    LocalDateTime.now().plusMinutes(15));

            mockMvc.perform(post("/api/orders/{id}/pay", order.getId())
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("PAID"))
                    .andExpect(jsonPath("$.paidAt").exists());

            Order paid = ordersRepositoryPort.findById(order.getId()).orElseThrow();
            assertThat(paid.getStatus()).isEqualTo(OrderStatusEnum.PAID);
            assertThat(paid.getPaidAt()).isNotNull();

            Ticket updated = ticketsRepositoryPort.findById(ticket.getId()).orElseThrow();
            assertThat(updated.getTicketStatus()).isEqualTo(TicketStatusEnum.PURCHASED);
            assertThat(updated.getUser()).isNotNull();
            assertThat(updated.getUser().getId()).isEqualTo(testUser.getId());
            assertThat(updated.getPurchasedAt()).isNotNull();
        }

        @Test
        @DisplayName("should return 422 when status is not WAITING")
        void shouldReturn422WhenWrongStatus() throws Exception {
            Ticket ticket = createTicket(TicketStatusEnum.PURCHASED, testUser);
            Order order = createOrder(testUser, ticket, OrderStatusEnum.PAID,
                    LocalDateTime.now().plusMinutes(15));

            mockMvc.perform(post("/api/orders/{id}/pay", order.getId())
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER")))
                    .andExpect(status().isUnprocessableEntity());

            Order unchanged = ordersRepositoryPort.findById(order.getId()).orElseThrow();
            assertThat(unchanged.getStatus()).isEqualTo(OrderStatusEnum.PAID);
        }

        @Test
        @DisplayName("should return 422 when reservation expired")
        void shouldReturn422WhenExpired() throws Exception {
            Ticket ticket = createTicket(TicketStatusEnum.RESERVED, null);
            Order order = createOrder(testUser, ticket, OrderStatusEnum.WAITING,
                    LocalDateTime.now().minusMinutes(5));

            mockMvc.perform(post("/api/orders/{id}/pay", order.getId())
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER")))
                    .andExpect(status().isUnprocessableEntity());

            Order unchanged = ordersRepositoryPort.findById(order.getId()).orElseThrow();
            assertThat(unchanged.getStatus()).isEqualTo(OrderStatusEnum.WAITING);
        }

        @Test
        @DisplayName("should return 403 for another user's order")
        void shouldReturn403ForAnotherUser() throws Exception {
            Ticket ticket = createTicket(TicketStatusEnum.RESERVED, null);
            Order order = createOrder(otherUser, ticket, OrderStatusEnum.WAITING,
                    LocalDateTime.now().plusMinutes(15));

            mockMvc.perform(post("/api/orders/{id}/pay", order.getId())
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER")))
                    .andExpect(status().isForbidden());

            assertThat(ordersRepositoryPort.findById(order.getId()).orElseThrow().getStatus())
                    .isEqualTo(OrderStatusEnum.WAITING);
        }

        @Test
        @DisplayName("should return 404 when order not found")
        void shouldReturn404WhenNotFound() throws Exception {
            mockMvc.perform(post("/api/orders/{id}/pay", UUID.randomUUID())
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER")))
                    .andExpect(status().isNotFound());
        }
    }

    // ---------- CANCEL ----------

    @Nested
    @DisplayName("POST /api/orders/{id}/cancel")
    class CancelOrderTests {

        @Test
        @DisplayName("should cancel WAITING order and release ticket")
        void shouldCancelOrder() throws Exception {
            Ticket ticket = createTicket(TicketStatusEnum.RESERVED, null);
            Order order = createOrder(testUser, ticket, OrderStatusEnum.WAITING,
                    LocalDateTime.now().plusMinutes(15));

            mockMvc.perform(post("/api/orders/{id}/cancel", order.getId())
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CANCELED"))
                    .andExpect(jsonPath("$.canceledAt").exists());

            Order canceled = ordersRepositoryPort.findById(order.getId()).orElseThrow();
            assertThat(canceled.getStatus()).isEqualTo(OrderStatusEnum.CANCELED);
            assertThat(canceled.getCanceledAt()).isNotNull();

            Ticket released = ticketsRepositoryPort.findById(ticket.getId()).orElseThrow();
            assertThat(released.getTicketStatus()).isEqualTo(TicketStatusEnum.AVAILABLE);
            assertThat(released.getUser()).isNull();
            assertThat(released.getPurchasedAt()).isNull();
        }

        @Test
        @DisplayName("should return 422 when status is PAID (spec suggests /refund)")
        void shouldReturn422WhenPaid() throws Exception {
            Ticket ticket = createTicket(TicketStatusEnum.PURCHASED, testUser);
            Order order = createOrder(testUser, ticket, OrderStatusEnum.PAID,
                    LocalDateTime.now().plusMinutes(15));

            mockMvc.perform(post("/api/orders/{id}/cancel", order.getId())
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER")))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("should return 403 for another user's order")
        void shouldReturn403ForAnotherUser() throws Exception {
            Ticket ticket = createTicket(TicketStatusEnum.RESERVED, null);
            Order order = createOrder(otherUser, ticket, OrderStatusEnum.WAITING,
                    LocalDateTime.now().plusMinutes(15));

            mockMvc.perform(post("/api/orders/{id}/cancel", order.getId())
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER")))
                    .andExpect(status().isForbidden());

            assertThat(ordersRepositoryPort.findById(order.getId()).orElseThrow().getStatus())
                    .isEqualTo(OrderStatusEnum.WAITING);
        }

        @Test
        @DisplayName("should return 404 when order not found")
        void shouldReturn404WhenNotFound() throws Exception {
            mockMvc.perform(post("/api/orders/{id}/cancel", UUID.randomUUID())
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER")))
                    .andExpect(status().isNotFound());
        }
    }

    // ---------- REFUND ----------

    @Nested
    @DisplayName("POST /api/orders/{id}/refund")
    class RefundOrderTests {

        @Test
        @DisplayName("should refund PAID order and release ticket")
        void shouldRefundOrder() throws Exception {
            Ticket ticket = createTicket(TicketStatusEnum.PURCHASED, testUser);
            Order order = createOrder(testUser, ticket, OrderStatusEnum.PAID,
                    LocalDateTime.now().plusMinutes(15));
            order.setPaidAt(LocalDateTime.now().minusHours(1));
            ordersRepositoryPort.save(order);

            mockMvc.perform(post("/api/orders/{id}/refund", order.getId())
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("REFUNDED"))
                    .andExpect(jsonPath("$.refundedAt").exists());

            Order refunded = ordersRepositoryPort.findById(order.getId()).orElseThrow();
            assertThat(refunded.getStatus()).isEqualTo(OrderStatusEnum.REFUNDED);
            assertThat(refunded.getRefundedAt()).isNotNull();

            Ticket released = ticketsRepositoryPort.findById(ticket.getId()).orElseThrow();
            assertThat(released.getTicketStatus()).isEqualTo(TicketStatusEnum.AVAILABLE);
            assertThat(released.getUser()).isNull();
        }

        @Test
        @DisplayName("should return 422 when status is not PAID")
        void shouldReturn422WhenWrongStatus() throws Exception {
            Ticket ticket = createTicket(TicketStatusEnum.RESERVED, null);
            Order order = createOrder(testUser, ticket, OrderStatusEnum.WAITING,
                    LocalDateTime.now().plusMinutes(15));

            mockMvc.perform(post("/api/orders/{id}/refund", order.getId())
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER")))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("should return 422 when route departure is in the past")
        void shouldReturn422AfterDeparture() throws Exception {
            Route route = routesRepositoryPort.findById(routeId).orElseThrow();
            route.setDepartureAt(LocalDateTime.now().minusDays(1));
            routesRepositoryPort.save(route);

            Ticket ticket = createTicket(TicketStatusEnum.PURCHASED, testUser);
            Order order = createOrder(testUser, ticket, OrderStatusEnum.PAID,
                    LocalDateTime.now().plusMinutes(15));
            order.setPaidAt(LocalDateTime.now().minusHours(1));
            ordersRepositoryPort.save(order);

            mockMvc.perform(post("/api/orders/{id}/refund", order.getId())
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER")))
                    .andExpect(status().isUnprocessableEntity());

            assertThat(ordersRepositoryPort.findById(order.getId()).orElseThrow().getStatus())
                    .isEqualTo(OrderStatusEnum.PAID);
        }

        @Test
        @DisplayName("should return 403 for another user's order")
        void shouldReturn403ForAnotherUser() throws Exception {
            Ticket ticket = createTicket(TicketStatusEnum.PURCHASED, otherUser);
            Order order = createOrder(otherUser, ticket, OrderStatusEnum.PAID,
                    LocalDateTime.now().plusMinutes(15));

            mockMvc.perform(post("/api/orders/{id}/refund", order.getId())
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER")))
                    .andExpect(status().isForbidden());
        }
    }

    // ---------- READ: MY ----------

    @Nested
    @DisplayName("GET /api/orders/my")
    class ListMyOrdersTests {

        @Test
        @DisplayName("should return only current user's orders")
        void shouldReturnOnlyOwnOrders() throws Exception {
            Ticket t1 = createTicket(TicketStatusEnum.RESERVED, null);
            Ticket t2 = createTicket(TicketStatusEnum.RESERVED, null);

            createOrder(testUser, t1, OrderStatusEnum.WAITING, LocalDateTime.now().plusMinutes(15));
            createOrder(otherUser, t2, OrderStatusEnum.WAITING, LocalDateTime.now().plusMinutes(15));

            mockMvc.perform(get("/api/orders/my")
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].ticket.id").value(t1.getId().toString()));
        }

        @Test
        @DisplayName("should return empty list when user has no orders")
        void shouldReturnEmpty() throws Exception {
            mockMvc.perform(get("/api/orders/my")
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    // ---------- READ: BY ID ----------

    @Nested
    @DisplayName("GET /api/orders/{id}")
    class GetOrderTests {

        @Test
        @DisplayName("should return 200 for owner")
        void shouldReturnForOwner() throws Exception {
            Ticket ticket = createTicket(TicketStatusEnum.RESERVED, null);
            Order order = createOrder(testUser, ticket, OrderStatusEnum.WAITING,
                    LocalDateTime.now().plusMinutes(15));

            mockMvc.perform(get("/api/orders/{id}", order.getId())
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(order.getId().toString()))
                    .andExpect(jsonPath("$.status").value("WAITING"));
        }

        @Test
        @DisplayName("should return 403 for another user's order")
        void shouldReturn403ForAnotherUser() throws Exception {
            Ticket ticket = createTicket(TicketStatusEnum.RESERVED, null);
            Order order = createOrder(otherUser, ticket, OrderStatusEnum.WAITING,
                    LocalDateTime.now().plusMinutes(15));

            mockMvc.perform(get("/api/orders/{id}", order.getId())
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 404 when not found")
        void shouldReturn404WhenNotFound() throws Exception {
            mockMvc.perform(get("/api/orders/{id}", UUID.randomUUID())
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER")))
                    .andExpect(status().isNotFound());
        }
    }

    // ---------- READ: ALL (admin) ----------

    @Nested
    @DisplayName("GET /api/orders")
    class ListAllOrdersTests {

        @Test
        @DisplayName("should return paginated list for admin")
        void shouldReturnListForAdmin() throws Exception {
            Ticket t1 = createTicket(TicketStatusEnum.RESERVED, null);
            Ticket t2 = createTicket(TicketStatusEnum.RESERVED, null);

            createOrder(testUser, t1, OrderStatusEnum.WAITING, LocalDateTime.now().plusMinutes(15));
            createOrder(otherUser, t2, OrderStatusEnum.WAITING, LocalDateTime.now().plusMinutes(15));

            mockMvc.perform(get("/api/orders")
                            .with(JwtTestHelper.withRole("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.totalElements").value(2));
        }

        @Test
        @DisplayName("should return empty page when no orders")
        void shouldReturnEmptyPage() throws Exception {
            mockMvc.perform(get("/api/orders")
                            .with(JwtTestHelper.withRole("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements").value(0));
        }
    }

    // ---------- HELPERS ----------

    private UUID createRouteInDb() {
        Carrier carrier = carriersRepositoryPort.save(
                Carrier.builder()
                        .name("Acme-" + UUID.randomUUID())
                        .phone("+7-999")
                        .build());

        Route route = routesRepositoryPort.save(
                Route.builder()
                        .departurePoint("Moscow")
                        .destinationPoint("SPb")
                        .carrier(carrier)
                        .durationMinutes(120)
                        .departureAt(LocalDateTime.now().plusDays(1))
                        .destinationAt(LocalDateTime.now().plusDays(1).plusHours(2))
                        .build());

        return route.getId();
    }

    private User createUserInDb(String login) {
        return usersRepositoryPort.save(User.builder()
                .login(login)
                .password(passwordEncoder.encode("secret123"))
                .fullName("Test User")
                .role(UserRoleEnum.USER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
    }

    private Ticket createTicket(TicketStatusEnum status, User user) {
        Route route = routesRepositoryPort.findById(routeId).orElseThrow();
        return ticketsRepositoryPort.save(Ticket.builder()
                .route(route)
                .seatNumber(seatCounter.getAndIncrement())
                .price(new BigDecimal("150.00"))
                .ticketStatus(status)
                .user(user)
                .purchasedAt(status == TicketStatusEnum.PURCHASED ? LocalDateTime.now() : null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
    }

    private Order createOrder(User user, Ticket ticket, OrderStatusEnum status,
                              LocalDateTime reservedUntil) {
        return ordersRepositoryPort.save(Order.builder()
                .user(user)
                .ticket(ticket)
                .price(ticket.getPrice())
                .status(status)
                .reservedUntil(reservedUntil)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
    }
}