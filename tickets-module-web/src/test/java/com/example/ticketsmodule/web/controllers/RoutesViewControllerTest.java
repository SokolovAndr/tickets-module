package com.example.ticketsmodule.web.controllers;

import com.example.ticketsmodule.api.model.CreateRouteResponse;
import com.example.ticketsmodule.api.model.CreateTicketResponse;
import com.example.ticketsmodule.api.model.RoutesSearchRequest;
import com.example.ticketsmodule.impl.oauth.service.SessionCurrentUserService;
import com.example.ticketsmodule.impl.routes.service.RoutesService;
import com.example.ticketsmodule.impl.tickets.service.TicketsService;
import com.example.ticketsmodule.web.routes.controller.RoutesViewController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
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

    @MockBean
    private RoutesService routesService;

    @MockBean
    private TicketsService ticketsService;

    @MockBean
    private SessionCurrentUserService sessionCurrentUserService;

    private UUID routeId;
    private UUID ticketId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        routeId = UUID.randomUUID();
        ticketId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("GET /routes returns list view")
    void listShouldReturnListView() throws Exception {
        Page<CreateRouteResponse> page = new PageImpl<>(List.of(new CreateRouteResponse()));
        when(routesService.findAll(any(RoutesSearchRequest.class), any(Pageable.class)))
                .thenReturn(page);

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
    @DisplayName("GET /routes/{id} returns details view")
    void detailsShouldReturnDetailsView() throws Exception {
        CreateRouteResponse route = new CreateRouteResponse();
        when(routesService.findOne(routeId)).thenReturn(route);
        when(ticketsService.findAvailableByRoute(routeId)).thenReturn(List.of());

        mockMvc.perform(get("/routes/{id}", routeId))
                .andExpect(status().isOk())
                .andExpect(view().name("routes/details"))
                .andExpect(model().attribute("route", route))
                .andExpect(model().attributeExists("seats"));
    }

    @Test
    @DisplayName("POST buy redirects to my tickets on success")
    void buyShouldRedirectToMyTickets() throws Exception {
        when(sessionCurrentUserService.getCurrentUserId()).thenReturn(userId);
        when(ticketsService.buyTicket(ticketId, userId)).thenReturn(new CreateTicketResponse());

        mockMvc.perform(post("/routes/{routeId}/tickets/{ticketId}/buy", routeId, ticketId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-tickets"))
                .andExpect(flash().attribute("successMessage", "Билет успешно куплен"));
    }

    @Test
    @DisplayName("POST buy redirects to route on error")
    void buyShouldRedirectToRouteOnError() throws Exception {
        when(sessionCurrentUserService.getCurrentUserId()).thenReturn(userId);
        when(ticketsService.buyTicket(ticketId, userId))
                .thenThrow(new IllegalStateException("No seats"));

        mockMvc.perform(post("/routes/{routeId}/tickets/{ticketId}/buy", routeId, ticketId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/routes/" + routeId))
                .andExpect(flash().attribute("errorMessage", "No seats"));
    }
}