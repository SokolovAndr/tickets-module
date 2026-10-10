package com.example.ticketsmodule.web.tickets.controller;

import com.example.ticketsmodule.api.model.CreateTicketResponse;
import com.example.ticketsmodule.impl.oauth.service.SessionCurrentUserService;
import com.example.ticketsmodule.impl.routes.domain.RouteEntity;
import com.example.ticketsmodule.impl.routes.service.RoutesService;
import com.example.ticketsmodule.impl.tickets.service.TicketsService;
import com.example.ticketsmodule.web.tickets.dto.MyTicketView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MyTicketsViewController {

    private final TicketsService ticketsService;
    private final RoutesService routesService;
    private final SessionCurrentUserService sessionCurrentUserService;

    @GetMapping("/my-tickets")
    public String list(Model model) {
        UUID userId = sessionCurrentUserService.getCurrentUserId();

        List<CreateTicketResponse> tickets = ticketsService.findAllMyTickets(userId);

        Set<UUID> routeIds = tickets.stream()
                .map(CreateTicketResponse::getRouteId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, RouteEntity> routes = routesService.findAllByIds(routeIds);

        List<MyTicketView> views = tickets.stream()
                .map(t -> {
                    RouteEntity r = routes.get(t.getRouteId());
                    return MyTicketView.builder()
                            .id(t.getId())
                            .seatNumber(t.getSeatNumber())
                            .price(t.getPrice())
                            .purchasedAt(t.getPurchasedAt())
                            .departurePoint(r != null ? r.getDeparturePoint() : "—")
                            .departureAt(r != null ? r.getDepartureAt() : null)
                            .destinationPoint(r != null ? r.getDestinationPoint() : "—")
                            .destinationAt(r != null ? r.getDestinationAt() : null)
                            .build();
                })
                .toList();

        model.addAttribute("tickets", views);
        return "my-tickets";
    }

    @PostMapping("/my-tickets/{id}/return")
    public String returnTicket(@PathVariable UUID id, RedirectAttributes ra) {
        UUID userId = sessionCurrentUserService.getCurrentUserId();
        try {
            ticketsService.returnTicket(id, userId);
            ra.addFlashAttribute("successMessage", "Билет успешно возвращён");
        } catch (Exception e) {
            log.warn("Failed to return ticket {}: {}", id, e.getMessage());
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/my-tickets";
    }
}