package com.example.ticketsmodule.web.controllers;

import com.example.ticketsmodule.api.model.CreateOrderRequest;
import com.example.ticketsmodule.api.model.CreateOrderResponse;
import com.example.ticketsmodule.api.model.CreateRouteResponse;
import com.example.ticketsmodule.api.model.CreateTicketResponse;
import com.example.ticketsmodule.api.model.RoutesSearchRequest;
import com.example.ticketsmodule.domain.common.PageRequest;
import com.example.ticketsmodule.domain.common.PageResult;
import com.example.ticketsmodule.domain.order.exception.TicketNotAvailableException;
import com.example.ticketsmodule.domain.ticket.model.TicketStatusEnum;
import com.example.ticketsmodule.impl.oauth.service.SessionCurrentUserService;
import com.example.ticketsmodule.impl.order.service.OrdersService;
import com.example.ticketsmodule.impl.routes.service.RoutesService;
import com.example.ticketsmodule.impl.tickets.service.TicketsService;
import com.example.ticketsmodule.web.routes.controller.RoutesViewController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoutesViewController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("RoutesViewController MVC tests")
class RoutesViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private RoutesService routesService;
    @MockBean private TicketsService ticketsService;
    @MockBean private OrdersService ordersService;
    @MockBean private SessionCurrentUserService sessionCurrentUserService;

    private UUID routeId;
    private UUID ticketId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        routeId = UUID.randomUUID();
        ticketId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    // ---------- GET /routes ----------

    @Nested
    @DisplayName("GET /routes")
    class ListTests {

        @Test
        @DisplayName("returns list view with page and filter attributes")
        void listShouldReturnListView() throws Exception {
            PageResult<CreateRouteResponse> pageResult = PageResult.<CreateRouteResponse>builder()
                    .content(List.of(new CreateRouteResponse()))
                    .totalElements(1)
                    .totalPages(1)
                    .pageNumber(0)
                    .pageSize(6)
                    .build();

            when(routesService.findAll(any(RoutesSearchRequest.class), any(PageRequest.class)))
                    .thenReturn(pageResult);

            mockMvc.perform(get("/routes")
                            .param("page", "0")
                            .param("departurePoint", "Moscow")
                            .param("destinationPoint", "SPb")
                            .param("departureDate", "2026-10-11")
                            .param("destinationDate", "2026-10-11"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("routes/list"))
                    .andExpect(model().attributeExists("page"))
                    .andExpect(model().attribute("departurePoint", "Moscow"))
                    .andExpect(model().attribute("destinationPoint", "SPb"))
                    .andExpect(model().attribute("departureDate", LocalDate.of(2026, 10, 11)))
                    .andExpect(model().attribute("destinationDate", LocalDate.of(2026, 10, 11)));
        }

        @Test
        @DisplayName("uses PAGE_SIZE=6 and page=0 by default")
        void listShouldUseDefaultPaging() throws Exception {
            PageResult<CreateRouteResponse> empty = PageResult.<CreateRouteResponse>builder()
                    .content(List.of())
                    .totalElements(0)
                    .build();

            when(routesService.findAll(any(RoutesSearchRequest.class), any(PageRequest.class)))
                    .thenReturn(empty);

            mockMvc.perform(get("/routes"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("routes/list"))
                    .andExpect(model().attribute("page", org.hamcrest.Matchers.notNullValue()));
        }

        @Test
        @DisplayName("clamps negative page to zero")
        void listShouldClampNegativePage() throws Exception {
            PageResult<CreateRouteResponse> empty = PageResult.<CreateRouteResponse>builder()
                    .content(List.of())
                    .totalElements(0)
                    .build();

            when(routesService.findAll(any(RoutesSearchRequest.class), any(PageRequest.class)))
                    .thenReturn(empty);

            mockMvc.perform(get("/routes").param("page", "-5"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("routes/list"));
        }
    }

    // ---------- GET /routes/{id} ----------

    @Nested
    @DisplayName("GET /routes/{id}")
    class DetailsTests {

        @Test
        @DisplayName("returns details view with route and seats")
        void detailsShouldReturnDetailsView() throws Exception {
            CreateRouteResponse route = new CreateRouteResponse();
            route.setId(routeId);
            route.setDeparturePoint("Moscow");
            route.setDestinationPoint("SPb");

            CreateTicketResponse seat = new CreateTicketResponse();
            seat.setId(ticketId);
            seat.setRouteId(routeId);
            seat.setSeatNumber(1);

            when(routesService.findOne(routeId)).thenReturn(route);
            when(ticketsService.findAvailableByRoute(routeId)).thenReturn(List.of(seat));

            mockMvc.perform(get("/routes/{id}", routeId))
                    .andExpect(status().isOk())
                    .andExpect(view().name("routes/details"))
                    .andExpect(model().attribute("route", route))
                    .andExpect(model().attributeExists("seats"))
                    .andExpect(model().attribute("seats", hasSize(1)));
        }

        @Test
        @DisplayName("returns details view with empty seats")
        void detailsShouldReturnEmptySeats() throws Exception {
            CreateRouteResponse route = new CreateRouteResponse();
            route.setId(routeId);

            when(routesService.findOne(routeId)).thenReturn(route);
            when(ticketsService.findAvailableByRoute(routeId)).thenReturn(List.of());

            mockMvc.perform(get("/routes/{id}", routeId))
                    .andExpect(status().isOk())
                    .andExpect(view().name("routes/details"))
                    .andExpect(model().attribute("seats", hasSize(0)));
        }
    }

    // ---------- POST /routes/{routeId}/tickets/{ticketId}/reserve ----------

    @Nested
    @DisplayName("POST /routes/{routeId}/tickets/{ticketId}/reserve")
    class ReserveTests {

        @Test
        @DisplayName("redirects to /my-tickets with success flash on success")
        void reserveShouldRedirectToMyTicketsOnSuccess() throws Exception {
            when(sessionCurrentUserService.getCurrentUserId()).thenReturn(userId);
            when(ordersService.create(any(CreateOrderRequest.class), eq(userId)))
                    .thenReturn(new CreateOrderResponse());

            mockMvc.perform(post("/routes/{routeId}/tickets/{ticketId}/reserve", routeId, ticketId))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/my-tickets"))
                    .andExpect(flash().attribute("successMessage", "Билет забронирован на 1 мин"));

            var captor = org.mockito.ArgumentCaptor.forClass(CreateOrderRequest.class);
            verify(ordersService).create(captor.capture(), eq(userId));
            assertThat(captor.getValue().getTicketId()).isEqualTo(ticketId);
        }

        @Test
        @DisplayName("redirects to route page with error flash when ticket is not available")
        void reserveShouldRedirectToRouteOnError() throws Exception {
            when(sessionCurrentUserService.getCurrentUserId()).thenReturn(userId);
            when(ordersService.create(any(CreateOrderRequest.class), eq(userId)))
                    .thenThrow(new TicketNotAvailableException(ticketId, TicketStatusEnum.PURCHASED));

            mockMvc.perform(post("/routes/{routeId}/tickets/{ticketId}/reserve", routeId, ticketId))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/routes/" + routeId))
                    .andExpect(flash().attribute("errorMessage", "Место уже занято"));
        }

        @Test
        @DisplayName("redirects to route page with error flash on any other exception")
        void reserveShouldRedirectToRouteOnAnyError() throws Exception {
            when(sessionCurrentUserService.getCurrentUserId()).thenReturn(userId);
            when(ordersService.create(any(CreateOrderRequest.class), eq(userId)))
                    .thenThrow(new RuntimeException("boom"));

            mockMvc.perform(post("/routes/{routeId}/tickets/{ticketId}/reserve", routeId, ticketId))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/routes/" + routeId))
                    .andExpect(flash().attribute("errorMessage", "Место уже занято"));
        }
    }
}