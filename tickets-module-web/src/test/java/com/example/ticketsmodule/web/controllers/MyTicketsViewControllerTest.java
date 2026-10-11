package com.example.ticketsmodule.web.controllers;

import com.example.ticketsmodule.api.model.CreateTicketResponse;
import com.example.ticketsmodule.impl.oauth.service.SessionCurrentUserService;
import com.example.ticketsmodule.impl.routes.domain.RouteEntity;
import com.example.ticketsmodule.impl.routes.service.RoutesService;
import com.example.ticketsmodule.impl.tickets.service.TicketsService;
import com.example.ticketsmodule.web.tickets.controller.MyTicketsViewController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MyTicketsViewController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("MyTicketsViewController MVC tests")
class MyTicketsViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketsService ticketsService;

    @MockBean
    private RoutesService routesService;

    @MockBean
    private SessionCurrentUserService sessionCurrentUserService;

    private UUID ticketId;
    private UUID routeId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        ticketId = UUID.randomUUID();
        routeId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("GET /my-tickets returns view with tickets")
    void listShouldReturnView() throws Exception {
        CreateTicketResponse ticket = new CreateTicketResponse();
        ticket.setId(ticketId);
        ticket.setRouteId(routeId);
        ticket.setSeatNumber(7);
        ticket.setPrice(BigDecimal.TEN);
        ticket.setPurchasedAt(LocalDateTime.now());

        RouteEntity route = new RouteEntity();
        route.setId(routeId);
        route.setDeparturePoint("Moscow");
        route.setDestinationPoint("SPb");
        route.setDepartureAt(LocalDateTime.now().plusDays(1));
        route.setDestinationAt(LocalDateTime.now().plusDays(1).plusHours(2));

        when(sessionCurrentUserService.getCurrentUserId()).thenReturn(userId);
        when(ticketsService.findAllMyTickets(userId)).thenReturn(List.of(ticket));
        when(routesService.findAllByIds(Set.of(routeId))).thenReturn(Map.of(routeId, route));

        mockMvc.perform(get("/my-tickets"))
                .andExpect(status().isOk())
                .andExpect(view().name("my-tickets"))
                .andExpect(model().attributeExists("tickets"));
    }

    @Test
    @DisplayName("POST return redirects to my tickets on success")
    void returnShouldRedirectOnSuccess() throws Exception {
        when(sessionCurrentUserService.getCurrentUserId()).thenReturn(userId);
        when(ticketsService.returnTicket(ticketId, userId)).thenReturn(new CreateTicketResponse());

        mockMvc.perform(post("/my-tickets/{id}/return", ticketId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-tickets"))
                .andExpect(flash().attribute("successMessage", "Билет успешно возвращён"));
    }

    @Test
    @DisplayName("POST return redirects to my tickets on error")
    void returnShouldRedirectOnError() throws Exception {
        when(sessionCurrentUserService.getCurrentUserId()).thenReturn(userId);
        when(ticketsService.returnTicket(ticketId, userId))
                .thenThrow(new IllegalStateException("Cannot return"));

        mockMvc.perform(post("/my-tickets/{id}/return", ticketId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-tickets"))
                .andExpect(flash().attribute("errorMessage", "Cannot return"));
    }
}