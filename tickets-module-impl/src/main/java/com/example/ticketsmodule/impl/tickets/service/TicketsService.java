package com.example.ticketsmodule.impl.tickets.service;

import com.example.ticketsmodule.api.model.CreateTicketRequest;
import com.example.ticketsmodule.api.model.CreateTicketResponse;
import com.example.ticketsmodule.api.model.ReleaseTicketsBatchRequest;
import com.example.ticketsmodule.api.model.ReleaseTicketsBatchResponse;
import com.example.ticketsmodule.api.model.TicketPatchRequest;
import com.example.ticketsmodule.api.model.TicketsSearchRequest;
import com.example.ticketsmodule.domain.common.PageRequest;
import com.example.ticketsmodule.domain.common.PageResult;
import com.example.ticketsmodule.domain.route.model.Route;
import com.example.ticketsmodule.domain.ticket.exception.TicketAlreadyPurchasedException;
import com.example.ticketsmodule.domain.ticket.exception.TicketNotFoundException;
import com.example.ticketsmodule.domain.ticket.model.Ticket;
import com.example.ticketsmodule.domain.ticket.model.TicketFilter;
import com.example.ticketsmodule.domain.ticket.repository.TicketsRepositoryPort;
import com.example.ticketsmodule.domain.user.model.User;
import com.example.ticketsmodule.impl.routes.service.RoutesService;
import com.example.ticketsmodule.impl.tickets.conversion.TicketMapper;
import com.example.ticketsmodule.impl.users.service.UsersService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketsService {

    private final TicketsRepositoryPort ticketRepository;
    private final TicketMapper ticketMapper;
    private final RoutesService routesService;
    private final UsersService usersService;

    private static final String USER_TICKETS_CACHE = "userTickets";

    @Transactional
    public CreateTicketResponse create(CreateTicketRequest createTicketRequest) {
        final Route route = routesService.findOneById(createTicketRequest.getRouteId());
        final Ticket ticket = ticketMapper.toDomain(createTicketRequest);
        ticket.setRoute(route);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());
        return ticketMapper.toResponse(ticketRepository.save(ticket));
    }

    @Transactional(readOnly = true)
    public PageResult<CreateTicketResponse> findAll(@Valid TicketsSearchRequest req, PageRequest page) {
        TicketFilter filter = toFilter(req);
        PageResult<Ticket> result = ticketRepository.findAll(filter, page);

        List<CreateTicketResponse> content = result.getContent().stream().map(ticketMapper::toResponse).toList();

        return PageResult.<CreateTicketResponse>builder()
                .content(content)
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .pageNumber(result.getPageNumber())
                .pageSize(result.getPageSize())
                .build();
    }

    @Transactional(readOnly = true)
    public CreateTicketResponse findOne(@NotNull UUID id) {
        Ticket ticket = ticketRepository.findById(id).orElseThrow(() -> new TicketNotFoundException(id));
        return ticketMapper.toResponse(ticket);
    }

    @Transactional(readOnly = true)
    public Ticket findById(@NotNull UUID id) {
        return ticketRepository.findById(id).orElseThrow(() -> new TicketNotFoundException(id));
    }

    @Transactional
    public CreateTicketResponse patch(@NotNull UUID id, @Valid TicketPatchRequest req) {
        Ticket ticket = ticketRepository.findById(id).orElseThrow(() -> new TicketNotFoundException(id));

        if (req.getRouteId() != null) ticket.setRoute(routesService.findOneById(req.getRouteId()));
        if (req.getSeatNumber() != null) ticket.setSeatNumber(req.getSeatNumber());
        if (req.getPrice() != null) ticket.setPrice(req.getPrice());
        if (req.getIsPurchased() != null) ticket.setPurchased(req.getIsPurchased());

        if (req.getPurchasedById() != null) {
            User user = usersService.findOneById(req.getPurchasedById());
            ticket.setUser(user);
            ticket.setPurchased(true);
            ticket.setPurchasedAt(req.getPurchasedAt() != null ? req.getPurchasedAt() : LocalDateTime.now());
        } else if (req.getPurchasedAt() != null) {
            ticket.setPurchasedAt(req.getPurchasedAt());
        }

        ticket.setUpdatedAt(LocalDateTime.now());
        return ticketMapper.toResponse(ticketRepository.save(ticket));
    }

    @CacheEvict(value = USER_TICKETS_CACHE, key = "#userId")
    @Transactional
    public CreateTicketResponse buyTicket(@NotNull UUID id, @NotNull UUID userId) {
        User currentUser = usersService.findOneById(userId);
        Ticket ticket = ticketRepository.findById(id).orElseThrow(() -> new TicketNotFoundException(id));

        if (ticket.isPurchased()) throw new TicketAlreadyPurchasedException(id);
        if (ticket.getRoute().getDepartureAt().isBefore(LocalDateTime.now())) throw new IllegalStateException("Cannot buy ticket for past departure");

        ticket.setUser(currentUser);
        ticket.setPurchasedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());
        ticket.setPurchased(true);

        return ticketMapper.toResponse(ticketRepository.save(ticket));
    }

    @CacheEvict(value = USER_TICKETS_CACHE, key = "#userId")
    @Transactional
    public CreateTicketResponse returnTicket(@NotNull UUID id, @NotNull UUID userId) {
        User currentUser = usersService.findOneById(userId);
        Ticket ticket = ticketRepository.findById(id).orElseThrow(() -> new TicketNotFoundException(id));

        if (!ticket.isPurchased()) throw new IllegalStateException("Ticket is not purchased");
        if (ticket.getRoute().getDepartureAt().isBefore(LocalDateTime.now())) throw new IllegalStateException("Cannot return ticket after departure");
        if (!currentUser.getId().equals(ticket.getUser().getId())) throw new IllegalStateException("Users are different");

        ticket.setUser(null);
        ticket.setPurchasedAt(null);
        ticket.setPurchased(false);
        ticket.setUpdatedAt(LocalDateTime.now());

        return ticketMapper.toResponse(ticketRepository.save(ticket));
    }

    @Transactional
    public ReleaseTicketsBatchResponse releaseTicketsBatch(@Valid ReleaseTicketsBatchRequest request) {
        final Route route = routesService.findOneById(request.getRouteId());

        if (route.getDepartureAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException(String.format("Невозможно выпустить билеты для маршрута id %s с датой отправки в прошлом", route.getId()));
        }

        Set<Integer> occupiedSeats = ticketRepository.findSeatNumbersByRouteId(route.getId());

        List<Ticket> ticketsToSave = IntStream.rangeClosed(1, request.getSeatCount())
                .filter(seat -> !occupiedSeats.contains(seat))
                .mapToObj(seat -> buildTicket(route, seat, request.getPrice()))
                .toList();

        if (ticketsToSave.isEmpty()) {
            throw new IllegalStateException(String.format("Нет свободных мест для маршрута %s", route.getId()));
        }

        List<Ticket> savedTickets = ticketRepository.saveAll(ticketsToSave);

        return new ReleaseTicketsBatchResponse()
                .ticketsIds(savedTickets.stream().map(Ticket::getId).toList())
                .price(request.getPrice())
                .createdCount(savedTickets.size())
                .routeId(request.getRouteId());
    }

    @Cacheable(value = USER_TICKETS_CACHE, key = "#userId")
    @Transactional(readOnly = true)
    public List<CreateTicketResponse> findAllMyTickets(UUID userId) {
        return ticketRepository.findPurchasedByUserId(userId).stream().map(ticketMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CreateTicketResponse> findAvailableByRoute(UUID routeId) {
        routesService.findOneById(routeId);
        return ticketRepository.findAvailableByRouteId(routeId).stream().map(ticketMapper::toResponse).toList();
    }

    private TicketFilter toFilter(TicketsSearchRequest req) {
        if (req == null) return TicketFilter.builder().build();
        return TicketFilter.builder()
                .query(req.getQuery())
                .routeId(req.getRouteId())
                .seatNumber(req.getSeatNumber())
                .priceFrom(req.getPriceFrom())
                .priceTo(req.getPriceTo())
                .isPurchased(req.getIsPurchased())
                .purchasedById(req.getPurchasedById())
                .createdDateFrom(req.getCreatedDateFrom())
                .createdDateTo(req.getCreatedDateTo())
                .build();
    }

    private Ticket buildTicket(Route route, int seat, BigDecimal price) {
        return Ticket.builder()
                .route(route)
                .seatNumber(seat)
                .price(price)
                .purchased(false)
                .user(null)
                .purchasedAt(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}