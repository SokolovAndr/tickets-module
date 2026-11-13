package com.example.ticketsmodule.web.routes.controller;

import com.example.ticketsmodule.api.model.CreateOrderRequest;
import com.example.ticketsmodule.api.model.CreateOrderResponse;
import com.example.ticketsmodule.api.model.CreateRouteResponse;
import com.example.ticketsmodule.api.model.CreateTicketResponse;
import com.example.ticketsmodule.api.model.RoutesSearchRequest;
import com.example.ticketsmodule.domain.order.exception.TicketNotAvailableException;
import com.example.ticketsmodule.impl.oauth.service.SessionCurrentUserService;
import com.example.ticketsmodule.impl.order.service.OrdersService;
import com.example.ticketsmodule.impl.routes.service.RoutesService;
import com.example.ticketsmodule.impl.tickets.service.TicketsService;
import com.example.ticketsmodule.domain.common.PageRequest;
import com.example.ticketsmodule.domain.common.PageResult;
import com.example.ticketsmodule.domain.common.SortDirection;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final OrdersService ordersService;
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

        PageRequest domainPage = PageRequest.builder()
                .page(Math.max(page, 0))
                .size(PAGE_SIZE)
                .sortField("departureAt")
                .direction(SortDirection.ASC)
                .build();

        PageResult<CreateRouteResponse> result = routesService.findAll(search, domainPage);

        Pageable springPageable = org.springframework.data.domain.PageRequest.of(
                domainPage.getPage(),
                domainPage.getSize(),
                Sort.by(Sort.Direction.ASC, "departureAt")
        );

        Page<CreateRouteResponse> routesPage = new PageImpl<>(
                result.getContent(),
                springPageable,
                result.getTotalElements()
        );

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

    @PostMapping("/routes/{routeId}/tickets/{ticketId}/reserve")
    public String reserve(@PathVariable UUID routeId,
                          @PathVariable UUID ticketId,
                          RedirectAttributes ra) {
        UUID userId = sessionCurrentUserService.getCurrentUserId();
        try {
            CreateOrderRequest req = new CreateOrderRequest().ticketId(ticketId);
            CreateOrderResponse order = ordersService.create(req, userId);
            ra.addFlashAttribute("successMessage",
                    "Билет забронирован. Оплатите до " +
                            order.getReservedUntil().toLocalTime());
            return "redirect:/my-orders";
        } catch (TicketNotAvailableException e) {
            ra.addFlashAttribute("errorMessage", "Место уже занято");
            return "redirect:/routes/" + routeId;
        }
    }
}