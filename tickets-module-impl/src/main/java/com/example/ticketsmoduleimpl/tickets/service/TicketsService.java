package com.example.ticketsmoduleimpl.tickets.service;

import com.example.ticketsmoduleimpl.oauth.service.CurrentUserService;
import com.example.ticketsmoduleimpl.routes.domain.RouteEntity;
import com.example.ticketsmoduleimpl.routes.service.RoutesService;
import com.example.ticketsmoduleimpl.tickets.conversion.TicketFromEntityConverter;
import com.example.ticketsmoduleimpl.tickets.conversion.TicketToEntityConverter;
import com.example.ticketsmoduleimpl.tickets.domain.TicketEntity;
import com.example.ticketsmoduleimpl.tickets.repository.TicketRepository;
import com.example.ticketsmoduleimpl.users.domain.UserEntity;
import com.example.ticketsmoduleimpl.users.service.UsersService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tickets.model.CreateTicketRequest;
import tickets.model.CreateTicketResponse;
import tickets.model.ReleaseTicketsBatchRequest;
import tickets.model.ReleaseTicketsBatchResponse;
import tickets.model.TicketPatchRequest;
import tickets.model.TicketsSearchRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Сервис для работы с билетами
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketsService {

    private final TicketRepository ticketRepository;
    private final TicketFromEntityConverter fromEntityConverter;
    private final TicketToEntityConverter toEntityConverter;
    private final RoutesService routesService;
    private final CurrentUserService currentUserService;
    private final UsersService usersService;

    @Transactional
    public CreateTicketResponse create(CreateTicketRequest createTicketRequest) {
        final RouteEntity route = routesService.findOneById(createTicketRequest.getRouteId());
        final TicketEntity ticketEntity = toEntityConverter.convert(createTicketRequest, route);
        if (ticketEntity == null) {
            throw new IllegalStateException("Failed to convert ticket entity");
        }
        TicketEntity response = ticketRepository.save(ticketEntity);
        return fromEntityConverter.convert(response);
    }

    @Transactional(readOnly = true)
    public Page<CreateTicketResponse> findAll(@Valid TicketsSearchRequest searchParam, @Valid Pageable pageable) {
        Specification<TicketEntity> spec = buildSpecification(searchParam);
        return ticketRepository.findAll(spec, pageable).map(fromEntityConverter::convert);
    }

    private Specification<TicketEntity> buildSpecification(TicketsSearchRequest searchParam) {
        return (root, query, cb) -> {
            final List<Predicate> predicates = new ArrayList<>();

            query.orderBy(cb.desc(root.get("createdAt")));

            if (searchParam != null) {

                if (searchParam.getRouteId() != null) {
                    predicates.add(cb.equal(root.get("route").get("id"), searchParam.getRouteId()));
                }

                if (searchParam.getPriceFrom() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("price"), searchParam.getPriceFrom()));
                }

                if (searchParam.getPriceTo() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("price"), searchParam.getPriceTo()));
                }

                if (searchParam.getIsPurchased() != null) {
                    predicates.add(cb.equal(root.get("isPurchased"), searchParam.getIsPurchased()));
                }

                if (searchParam.getCreatedDateFrom() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), searchParam.getCreatedDateFrom()));
                }

                if (searchParam.getCreatedDateTo() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), searchParam.getCreatedDateTo()));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional(readOnly = true)
    public CreateTicketResponse findOne(@NotNull UUID id) {
        TicketEntity response = ticketRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(String.format("Ticket with id %s not found", id)));
        return fromEntityConverter.convert(response);
    }

    @Transactional
    public CreateTicketResponse patch(@NotNull UUID id, @Valid TicketPatchRequest ticketPatchRequest) {

        var result = ticketRepository.findById(id).map(entity -> {

            if (ticketPatchRequest.getRouteId() != null) {
                final RouteEntity route = routesService.findOneById(ticketPatchRequest.getRouteId());
                entity.setRoute(route);
            }
            if (ticketPatchRequest.getSeatNumber() != null) {
                entity.setSeatNumber(ticketPatchRequest.getSeatNumber());
            }
            if (ticketPatchRequest.getPrice() != null) {
                entity.setPrice(ticketPatchRequest.getPrice());
            }
            if (ticketPatchRequest.getIsPurchased() != null) {
                entity.setPurchased(ticketPatchRequest.getIsPurchased());
            }
            if (ticketPatchRequest.getPurchasedById() != null) {
                final UserEntity user = usersService.findOneById(ticketPatchRequest.getPurchasedById());
                entity.setUser(user);
                entity.setPurchased(true);
                entity.setPurchasedAt(ticketPatchRequest.getPurchasedAt() != null
                        ? ticketPatchRequest.getPurchasedAt()
                        : LocalDateTime.now());
            } else if (ticketPatchRequest.getPurchasedAt() != null) {
                entity.setPurchasedAt(ticketPatchRequest.getPurchasedAt());
            }

            entity.setUpdatedAt(LocalDateTime.now());

            return ticketRepository.save(entity);
        }).orElseThrow(() -> new EntityNotFoundException(String.format("Ticket with id %s not found", id)));

        return fromEntityConverter.convert(result);
    }

    @Transactional
    public CreateTicketResponse buyTicket(@NotNull UUID id) {
        final UserEntity currentUser = currentUserService.getCurrentUser();
        final TicketEntity ticket = ticketRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(String.format("ticket with id %s not found", id)));

        if (ticket.isPurchased()) {
            throw new IllegalStateException("Ticket already purchased");
        }

        if (ticket.getRoute().getDepartureAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot buy ticket for past departure");
        }

        ticket.setUser(currentUser);
        ticket.setPurchasedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());
        ticket.setPurchased(true);

        final TicketEntity savedTicket = ticketRepository.save(ticket);
        return fromEntityConverter.convert(savedTicket);
    }

    @Transactional
    public CreateTicketResponse returnTicket(@NotNull UUID id) {
        final UserEntity currentUser = currentUserService.getCurrentUser();
        final TicketEntity ticket = ticketRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(String.format("ticket with id %s not found", id)));

        if (!ticket.isPurchased()) {
            throw new IllegalStateException("Ticket is not purchased");
        }

        if (ticket.getRoute().getDepartureAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot return ticket after departure");
        }

        if (!currentUser.getId().equals(ticket.getUser().getId())) {
            throw new IllegalStateException("Users are different");
        }

        ticket.setUser(null);
        ticket.setPurchasedAt(null);
        ticket.setPurchased(false);
        ticket.setUpdatedAt(LocalDateTime.now());

        final TicketEntity savedTicket = ticketRepository.save(ticket);
        return fromEntityConverter.convert(savedTicket);
    }

    @Transactional
    public ReleaseTicketsBatchResponse releaseTicketsBatch(@Valid ReleaseTicketsBatchRequest request) {

        log.info("Запрос на выпуск билетов: маршрут={}, кол-во мест={}, цена={}",
                request.getRouteId(), request.getSeatCount(), request.getPrice());

        final RouteEntity route = routesService.findOneById(request.getRouteId());

        if (route.getDepartureAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException(
                    String.format("Невозможно выпустить билеты для маршрута id %s с датой отправки в прошлом", route.getId()));
        }

        Set<Integer> occupiedSeats = ticketRepository.findSeatNumbersByRouteId(route.getId());

        List<TicketEntity> ticketsToSave = IntStream.rangeClosed(1, request.getSeatCount())
                .filter(seat -> !occupiedSeats.contains(seat))
                .mapToObj(seat -> TicketEntity.builder()
                        .route(route)
                        .seatNumber(seat)
                        .price(request.getPrice())
                        .isPurchased(false)
                        .user(null)
                        .purchasedAt(null)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build())
                .toList();

        if (ticketsToSave.isEmpty()) {
            throw new IllegalStateException(
                    String.format("Нет свободных мест для маршрута %s. Все %d мест/а заняты",
                            route.getId(), request.getSeatCount()));
        }

        List<TicketEntity> savedTickets = ticketRepository.saveAll(ticketsToSave);

        log.info("Released {} of {} requested tickets for route {}",
                savedTickets.size(), request.getSeatCount(), route.getId());

        return new ReleaseTicketsBatchResponse()
                .ticketsIds(savedTickets.stream()
                        .map(TicketEntity::getId)
                        .toList())
                .price(request.getPrice())
                .createdCount(savedTickets.size())
                .routeId(request.getRouteId());
    }

}
