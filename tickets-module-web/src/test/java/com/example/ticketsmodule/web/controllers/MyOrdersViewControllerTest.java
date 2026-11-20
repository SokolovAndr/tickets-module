package com.example.ticketsmodule.web.controllers;

import com.example.ticketsmodule.api.model.CreateOrderResponse;
import com.example.ticketsmodule.api.model.CreateTicketResponse;
import com.example.ticketsmodule.api.model.OrderStatusEnum;
import com.example.ticketsmodule.domain.order.exception.OrderExpiredException;
import com.example.ticketsmodule.domain.route.model.Route;
import com.example.ticketsmodule.domain.ticket.model.Ticket;
import com.example.ticketsmodule.domain.user.model.User;
import com.example.ticketsmodule.impl.oauth.service.SessionCurrentUserService;
import com.example.ticketsmodule.impl.order.service.OrdersService;
import com.example.ticketsmodule.impl.routes.service.RoutesService;
import com.example.ticketsmodule.impl.tickets.pdf.TicketPdfGenerator;
import com.example.ticketsmodule.impl.tickets.service.TicketsService;
import com.example.ticketsmodule.web.orders.controller.MyOrdersViewController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MyOrdersViewController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("MyOrdersViewController MVC tests")
class MyOrdersViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private OrdersService ordersService;
    @MockBean private TicketsService ticketsService;
    @MockBean private RoutesService routesService;
    @MockBean private SessionCurrentUserService sessionCurrentUserService;
    @MockBean private TicketPdfGenerator ticketPdfGenerator;

    private UUID orderId;
    private UUID ticketId;
    private UUID routeId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        ticketId = UUID.randomUUID();
        routeId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    // ---------- GET /my-tickets ----------

    @Nested
    @DisplayName("GET /my-tickets")
    class ListTests {

        @Test
        @DisplayName("returns view with waiting/paid/history/routes grouped by status")
        void shouldReturnViewWithGroupedOrders() throws Exception {
            CreateOrderResponse waiting = order(OrderStatusEnum.WAITING);
            CreateOrderResponse paid = order(OrderStatusEnum.PAID);
            CreateOrderResponse canceled = order(OrderStatusEnum.CANCELED);

            Route route = Route.builder()
                    .id(routeId)
                    .departurePoint("Moscow")
                    .destinationPoint("SPb")
                    .departureAt(OffsetDateTime.now().plusDays(1))
                    .destinationAt(OffsetDateTime.now().plusDays(1).plusHours(2))
                    .build();

            when(sessionCurrentUserService.getCurrentUserId()).thenReturn(userId);
            when(ordersService.findAllMyOrders(userId)).thenReturn(List.of(waiting, paid, canceled));
            when(routesService.findAllByIds(Set.of(routeId))).thenReturn(Map.of(routeId, route));

            mockMvc.perform(get("/my-tickets"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("my-tickets"))
                    .andExpect(model().attributeExists("waiting", "paid", "history", "routes"))
                    .andExpect(model().attribute("waiting", hasSize(1)))
                    .andExpect(model().attribute("paid", hasSize(1)))
                    .andExpect(model().attribute("history", hasSize(1)));
        }

        @Test
        @DisplayName("returns view with empty lists when user has no orders")
        void shouldReturnEmptyListsWhenNoOrders() throws Exception {
            when(sessionCurrentUserService.getCurrentUserId()).thenReturn(userId);
            when(ordersService.findAllMyOrders(userId)).thenReturn(List.of());
            when(routesService.findAllByIds(Set.of())).thenReturn(Map.of());

            mockMvc.perform(get("/my-tickets"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("my-tickets"))
                    .andExpect(model().attribute("waiting", hasSize(0)))
                    .andExpect(model().attribute("paid", hasSize(0)))
                    .andExpect(model().attribute("history", hasSize(0)));
        }
    }

    // ---------- POST /my-tickets/{id}/pay ----------

    @Nested
    @DisplayName("POST /my-tickets/{id}/pay")
    class PayTests {

        @Test
        @DisplayName("redirects with success flash on success")
        void shouldRedirectWithSuccess() throws Exception {
            when(sessionCurrentUserService.getCurrentUserId()).thenReturn(userId);
            when(ordersService.payOrder(orderId, userId)).thenReturn(new CreateOrderResponse());

            mockMvc.perform(post("/my-tickets/{id}/pay", orderId))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/my-tickets"))
                    .andExpect(flash().attribute("successMessage", "Билет оплачен"));
        }

        @Test
        @DisplayName("redirects with expired flash when reservation expired")
        void shouldRedirectWithExpiredMessage() throws Exception {
            when(sessionCurrentUserService.getCurrentUserId()).thenReturn(userId);
            when(ordersService.payOrder(orderId, userId))
                    .thenThrow(new OrderExpiredException(orderId));

            mockMvc.perform(post("/my-tickets/{id}/pay", orderId))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/my-tickets"))
                    .andExpect(flash().attribute("errorMessage", "Время бронирования истекло"));
        }

        @Test
        @DisplayName("redirects with generic error message on any other exception")
        void shouldRedirectWithGenericError() throws Exception {
            when(sessionCurrentUserService.getCurrentUserId()).thenReturn(userId);
            when(ordersService.payOrder(orderId, userId))
                    .thenThrow(new IllegalStateException("Order is not WAITING"));

            mockMvc.perform(post("/my-tickets/{id}/pay", orderId))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/my-tickets"))
                    .andExpect(flash().attribute("errorMessage", "Order is not WAITING"));
        }
    }

    // ---------- POST /my-tickets/{id}/cancel ----------

    @Nested
    @DisplayName("POST /my-tickets/{id}/cancel")
    class CancelTests {

        @Test
        @DisplayName("redirects with success flash on success")
        void shouldRedirectWithSuccess() throws Exception {
            when(sessionCurrentUserService.getCurrentUserId()).thenReturn(userId);
            when(ordersService.cancelOrder(orderId, userId)).thenReturn(new CreateOrderResponse());

            mockMvc.perform(post("/my-tickets/{id}/cancel", orderId))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/my-tickets"))
                    .andExpect(flash().attribute("successMessage", "Заказ отменён"));
        }

        @Test
        @DisplayName("redirects with error flash on failure")
        void shouldRedirectWithError() throws Exception {
            when(sessionCurrentUserService.getCurrentUserId()).thenReturn(userId);
            when(ordersService.cancelOrder(orderId, userId))
                    .thenThrow(new IllegalStateException("Order cannot be canceled"));

            mockMvc.perform(post("/my-tickets/{id}/cancel", orderId))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/my-tickets"))
                    .andExpect(flash().attribute("errorMessage", "Order cannot be canceled"));
        }
    }

    // ---------- POST /my-tickets/{id}/refund ----------

    @Nested
    @DisplayName("POST /my-tickets/{id}/refund")
    class RefundTests {

        @Test
        @DisplayName("redirects with success flash on success")
        void shouldRedirectWithSuccess() throws Exception {
            when(sessionCurrentUserService.getCurrentUserId()).thenReturn(userId);
            when(ordersService.refundOrder(orderId, userId)).thenReturn(new CreateOrderResponse());

            mockMvc.perform(post("/my-tickets/{id}/refund", orderId))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/my-tickets"))
                    .andExpect(flash().attribute("successMessage", "Билет возвращён"));
        }

        @Test
        @DisplayName("redirects with error flash on failure")
        void shouldRedirectWithError() throws Exception {
            when(sessionCurrentUserService.getCurrentUserId()).thenReturn(userId);
            when(ordersService.refundOrder(orderId, userId))
                    .thenThrow(new IllegalStateException("Refund not allowed"));

            mockMvc.perform(post("/my-tickets/{id}/refund", orderId))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/my-tickets"))
                    .andExpect(flash().attribute("errorMessage", "Refund not allowed"));
        }
    }

    // ---------- GET /my-tickets/{id}/pdf ----------

    @Nested
    @DisplayName("GET /my-tickets/{id}/pdf")
    class DownloadPdfTests {

        @Test
        @DisplayName("returns PDF when ticket belongs to current user")
        void shouldReturnPdf() throws Exception {
            User user = new User();
            user.setId(userId);

            Ticket ticket = new Ticket();
            ticket.setId(ticketId);
            ticket.setUser(user);

            byte[] mockPdf = new byte[]{37, 80, 68, 70}; // %PDF

            when(sessionCurrentUserService.getCurrentUserId()).thenReturn(userId);
            when(ticketsService.findById(ticketId)).thenReturn(ticket);
            when(ticketPdfGenerator.generatePdf(ticket)).thenReturn(mockPdf);

            mockMvc.perform(get("/my-tickets/{id}/pdf", ticketId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                    .andExpect(header().string("Content-Disposition",
                            "attachment; filename=\"ticket-" + ticketId + ".pdf\""))
                    .andExpect(content().bytes(mockPdf));
        }

        @Test
        @DisplayName("does not generate PDF when ticket belongs to another user")
        void shouldNotGeneratePdfForAnotherUser() throws Exception {
            User anotherUser = new User();
            anotherUser.setId(UUID.randomUUID());

            Ticket ticket = new Ticket();
            ticket.setId(ticketId);
            ticket.setUser(anotherUser);

            when(sessionCurrentUserService.getCurrentUserId()).thenReturn(userId);
            when(ticketsService.findById(ticketId)).thenReturn(ticket);

            // Главная гарантия: PDF не генерируется.
            // Дополнительно: ответ НЕ является PDF-файлом (content-type != application/pdf
            // и первые байты тела — не "%PDF"). Конкретный HTTP-статус не фиксируем:
            // в @WebMvcTest его может подменить @ControllerAdvice.
            MvcResult result = mockMvc.perform(get("/my-tickets/{id}/pdf", ticketId)).andReturn();

            verifyNoInteractions(ticketPdfGenerator);

            assertThat(result.getResponse().getContentType())
                    .isNotEqualTo(MediaType.APPLICATION_PDF_VALUE);

            byte[] body = result.getResponse().getContentAsByteArray();
            assertThat(new String(body, 0, Math.min(4, body.length)))
                    .isNotEqualTo("%PDF");
        }

        @Test
        @DisplayName("does not generate PDF when ticket has no owner")
        void shouldNotGeneratePdfForUnownedTicket() throws Exception {
            Ticket ticket = new Ticket();
            ticket.setId(ticketId);
            ticket.setUser(null);

            when(sessionCurrentUserService.getCurrentUserId()).thenReturn(userId);
            when(ticketsService.findById(ticketId)).thenReturn(ticket);

            mockMvc.perform(get("/my-tickets/{id}/pdf", ticketId))
                    .andReturn();

            verifyNoInteractions(ticketPdfGenerator);
        }
    }

    // ---------- HELPERS ----------

    private CreateOrderResponse order(OrderStatusEnum status) {
        CreateTicketResponse ticket = new CreateTicketResponse();
        ticket.setId(ticketId);
        ticket.setRouteId(routeId);
        ticket.setSeatNumber(7);
        ticket.setPrice(BigDecimal.TEN);

        CreateOrderResponse order = new CreateOrderResponse();
        order.setId(orderId);
        order.setTicket(ticket);
        order.setStatus(status);
        order.setPrice(BigDecimal.TEN);
        order.setReservedUntil(OffsetDateTime.now().plusMinutes(15));
        return order;
    }
}