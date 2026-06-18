package com.example.ticketsmoduleimpl.tickets.service;

import com.example.ticketsmoduleimpl.carriers.domain.CarrierEntity;
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
import org.springframework.util.StringUtils;
import tickets.model.CreateTicketRequest;
import tickets.model.CreateTicketResponse;
import tickets.model.RoutesSearchRequest;
import tickets.model.TicketPatchRequest;
import tickets.model.TicketsSearchRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

        //TODO(): здесь не нужен currentUser
        final UserEntity currentUser = currentUserService.getCurrentUser();
        final RouteEntity route = routesService.findOneById(createTicketRequest.getRouteId());

        final TicketEntity ticketEntity = toEntityConverter.convert(createTicketRequest, route, currentUser);
        assert ticketEntity != null;
        TicketEntity response = ticketRepository.save(ticketEntity);
        return fromEntityConverter.convert(response);
    }

    public Page<CreateTicketResponse> findAll(@Valid TicketsSearchRequest searchParam, @Valid Pageable pageable) {
        Specification<TicketEntity> spec = buildSpecification(searchParam);
        return ticketRepository.findAll(spec, pageable).map(fromEntityConverter::convert);
    }

    private Specification<TicketEntity> buildSpecification(TicketsSearchRequest searchParam) {
        return (root, query, cb) -> {
            final List<Predicate> predicates = new ArrayList<>();

            query.orderBy(cb.desc(root.get("createdAt")));

            if (searchParam != null) {

                //TODO(): добавить фильтрацию по цене

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

    public CreateTicketResponse findOne(@NotNull UUID id) {
        TicketEntity response = ticketRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Ticket with id " + id + " not found"));
        return fromEntityConverter.convert(response);
    }

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
            }
            if (ticketPatchRequest.getPurchasedAt() != null) {
                entity.setPurchasedAt(ticketPatchRequest.getPurchasedAt());
            }

            entity.setUpdatedAt(LocalDateTime.now());

            return ticketRepository.save(entity);
        }).orElseThrow(() -> new EntityNotFoundException(String.format("ticket with id %s not found", ticketPatchRequest)));

        return fromEntityConverter.convert(result);
    }

    public CreateTicketResponse buyTicket(@NotNull UUID id) {
        //TODO(): здесь нужен currentUser

        return null;
    }

    public CreateTicketResponse returnTicket(@NotNull UUID id) {
        //TODO(): здесь нужен currentUser

        return null;
    }
}
