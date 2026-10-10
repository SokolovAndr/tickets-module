package com.example.ticketsmodule.web.routes.controller;

import com.example.ticketsmodule.api.model.CreateRouteResponse;
import com.example.ticketsmodule.api.model.CreateTicketResponse;
import com.example.ticketsmodule.api.model.RoutesSearchRequest;
import com.example.ticketsmodule.impl.oauth.service.SessionCurrentUserService;
import com.example.ticketsmodule.impl.routes.service.RoutesService;
import com.example.ticketsmodule.impl.tickets.service.TicketsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class RoutesViewController {

    private static final int PAGE_SIZE = 6;

    private final RoutesService routesService;
    private final TicketsService ticketsService;
    private final SessionCurrentUserService sessionCurrentUserService;

    @GetMapping("/routes")
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String departurePoint,
            @RequestParam(required = false) String destinationPoint,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate destinationDate,
            Model model) {

        RoutesSearchRequest search = new RoutesSearchRequest();
        search.setDeparturePoint(StringUtils.hasText(departurePoint) ? departurePoint : null);
        search.setDestinationPoint(StringUtils.hasText(destinationPoint) ? destinationPoint : null);

        if (departureDate != null) {
            search.setDepartureFrom(departureDate);
            search.setDepartureTo(departureDate);
        }
        if (destinationDate != null) {
            search.setDestinationFrom(destinationDate);
            search.setDestinationTo(destinationDate);
        }

        Pageable pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE,
                Sort.by(Sort.Direction.ASC, "departureAt"));
        Page<CreateRouteResponse> routesPage = routesService.findAll(search, pageable);

        model.addAttribute("page", routesPage);
        model.addAttribute("departurePoint", departurePoint);
        model.addAttribute("destinationPoint", destinationPoint);
        model.addAttribute("departureDate", departureDate);
        model.addAttribute("destinationDate", destinationDate);
        return "routes/list";
    }

    @GetMapping("/routes/{id}")
    public String details(@PathVariable UUID id, Model model) {
        CreateRouteResponse route = routesService.findOne(id);
        List<CreateTicketResponse> seats = ticketsService.findAvailableByRoute(id);
        model.addAttribute("route", route);
        model.addAttribute("seats", seats);
        return "routes/details";
    }

    @PostMapping("/routes/{routeId}/tickets/{ticketId}/buy")
    public String buy(@PathVariable UUID routeId,
                      @PathVariable UUID ticketId,
                      RedirectAttributes ra) {
        UUID userId = sessionCurrentUserService.getCurrentUserId();
        try {
            ticketsService.buyTicket(ticketId, userId);
            ra.addFlashAttribute("successMessage", "Билет успешно куплен");
            return "redirect:/my-tickets";
        } catch (Exception e) {
            log.warn("Failed to buy ticket {}: {}", ticketId, e.getMessage());
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/routes/" + routeId;
        }
    }
}