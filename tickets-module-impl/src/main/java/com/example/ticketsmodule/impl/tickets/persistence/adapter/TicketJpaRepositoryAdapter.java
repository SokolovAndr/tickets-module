package com.example.ticketsmodule.impl.tickets.persistence.adapter;

import com.example.ticketsmodule.domain.common.PageRequest;
import com.example.ticketsmodule.domain.common.PageResult;
import com.example.ticketsmodule.domain.common.SortDirection;
import com.example.ticketsmodule.domain.ticket.model.Ticket;
import com.example.ticketsmodule.domain.ticket.model.TicketFilter;
import com.example.ticketsmodule.domain.ticket.repository.TicketsRepositoryPort;
import com.example.ticketsmodule.impl.tickets.persistence.entity.TicketJpaEntity;
import com.example.ticketsmodule.impl.tickets.persistence.mapper.TicketPersistenceMapper;
import com.example.ticketsmodule.impl.tickets.persistence.repository.TicketSpringDataRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketJpaRepositoryAdapter implements TicketsRepositoryPort {

    private final TicketSpringDataRepository springRepo;
    private final TicketPersistenceMapper mapper;

    @Override
    @Transactional
    public Ticket save(Ticket ticket) {
        return mapper.toDomain(springRepo.save(mapper.toEntity(ticket)));
    }

    @Override
    @Transactional
    public List<Ticket> saveAll(List<Ticket> tickets) {
        return mapper.toDomainList(springRepo.saveAll(mapper.toEntityList(tickets)));
    }

    @Override
    public Optional<Ticket> findById(UUID id) {
        return springRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public PageResult<Ticket> findAll(TicketFilter filter, PageRequest pageReq) {
        Sort sort = buildSort(pageReq);
        var springPageable = org.springframework.data.domain.PageRequest.of(pageReq.getPage(), pageReq.getSize(), sort);
        Page<TicketJpaEntity> springPage = springRepo.findAll(buildSpecification(filter), springPageable);

        return PageResult.<Ticket>builder()
                .content(mapper.toDomainList(springPage.getContent()))
                .totalElements(springPage.getTotalElements())
                .totalPages(springPage.getTotalPages())
                .pageNumber(springPage.getNumber())
                .pageSize(springPage.getSize())
                .build();
    }

    @Override
    public Set<Integer> findSeatNumbersByRouteId(UUID routeId) {
        return springRepo.findSeatNumbersByRouteId(routeId);
    }

    @Override
    public List<Ticket> findPurchasedByUserId(UUID userId) {
        return mapper.toDomainList(springRepo.findPurchasedByUserId(userId));
    }

    @Override
    public List<Ticket> findAvailableByRouteId(UUID routeId) {
        return mapper.toDomainList(springRepo.findAvailableByRouteId(routeId));
    }

    private Sort buildSort(PageRequest pageReq) {
        String field = (pageReq.getSortField() == null || pageReq.getSortField().isBlank()) ? "createdAt" : pageReq.getSortField();
        Sort.Direction dir = (pageReq.getDirection() == SortDirection.ASC) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, field);
    }

    private Specification<TicketJpaEntity> buildSpecification(TicketFilter f) {
        return (root, query, cb) -> {
            if (Long.class != query.getResultType() && long.class != query.getResultType()) {
                root.fetch("route", jakarta.persistence.criteria.JoinType.LEFT).fetch("carrier", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("user", jakarta.persistence.criteria.JoinType.LEFT);
            }

            final List<Predicate> predicates = new ArrayList<>();
            if (f != null) {
                if (f.getRouteId() != null) predicates.add(cb.equal(root.get("route").get("id"), f.getRouteId()));
                if (f.getPriceFrom() != null) predicates.add(cb.greaterThanOrEqualTo(root.get("price"), f.getPriceFrom()));
                if (f.getPriceTo() != null) predicates.add(cb.lessThanOrEqualTo(root.get("price"), f.getPriceTo()));
                if (f.getIsPurchased() != null) predicates.add(cb.equal(root.get("purchased"), f.getIsPurchased()));
                if (f.getSeatNumber() != null) predicates.add(cb.equal(root.get("seatNumber"), f.getSeatNumber()));
                if (f.getCreatedDateFrom() != null) predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), f.getCreatedDateFrom().atStartOfDay()));
                if (f.getCreatedDateTo() != null) predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), f.getCreatedDateTo().atTime(LocalTime.MAX)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}