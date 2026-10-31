package com.example.ticketsmodule.impl.routes.persistence.adapter;

import com.example.ticketsmodule.domain.common.PageRequest;
import com.example.ticketsmodule.domain.common.PageResult;
import com.example.ticketsmodule.domain.common.SortDirection;
import com.example.ticketsmodule.domain.route.model.Route;
import com.example.ticketsmodule.domain.route.model.RouteFilter;
import com.example.ticketsmodule.domain.route.repository.RoutesRepositoryPort;
import com.example.ticketsmodule.impl.routes.persistence.entity.RouteJpaEntity;
import com.example.ticketsmodule.impl.routes.persistence.mapper.RoutePersistenceMapper;
import com.example.ticketsmodule.impl.routes.persistence.repository.RouteSpringDataRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RouteJpaRepositoryAdapter implements RoutesRepositoryPort {

    private final RouteSpringDataRepository springRepo;
    private final RoutePersistenceMapper mapper;

    @Override
    @Transactional
    public Route save(Route route) {
        RouteJpaEntity saved = springRepo.save(mapper.toEntity(route));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Route> findById(UUID id) {
        return springRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public PageResult<Route> findAll(RouteFilter filter, PageRequest pageReq) {
        Sort sort = buildSort(pageReq);
        var springPageable = org.springframework.data.domain.PageRequest.of(
                pageReq.getPage(),
                pageReq.getSize(),
                sort
        );

        Page<RouteJpaEntity> springPage = springRepo.findAll(buildSpecification(filter), springPageable);

        return PageResult.<Route>builder()
                .content(mapper.toDomainList(springPage.getContent()))
                .totalElements(springPage.getTotalElements())
                .totalPages(springPage.getTotalPages())
                .pageNumber(springPage.getNumber())
                .pageSize(springPage.getSize())
                .build();
    }

    private Sort buildSort(PageRequest pageReq) {
        String field = (pageReq.getSortField() == null || pageReq.getSortField().isBlank())
                ? "createdAt" : pageReq.getSortField();
        Sort.Direction dir = (pageReq.getDirection() == SortDirection.ASC)
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, field);
    }

    private Specification<RouteJpaEntity> buildSpecification(RouteFilter f) {
        return (root, query, cb) -> {
            final List<Predicate> predicates = new ArrayList<>();
            if (f != null) {
                if (StringUtils.hasText(f.getDeparturePoint())) {
                    var pattern = "%" + f.getDeparturePoint().toLowerCase() + "%";
                    predicates.add(cb.like(cb.lower(root.get("departurePoint")), pattern));
                }
                if (StringUtils.hasText(f.getDestinationPoint())) {
                    var pattern = "%" + f.getDestinationPoint().toLowerCase() + "%";
                    predicates.add(cb.like(cb.lower(root.get("destinationPoint")), pattern));
                }
                if (f.getDepartureFrom() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("departureAt"), f.getDepartureFrom()));
                }
                if (f.getDepartureTo() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("departureAt"), f.getDepartureTo()));
                }
                if (f.getDestinationFrom() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("destinationAt"), f.getDestinationFrom()));
                }
                if (f.getDestinationTo() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("destinationAt"), f.getDestinationTo()));
                }
                if (f.getCreateDateFrom() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), f.getCreateDateFrom()));
                }
                if (f.getCreateDateTo() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), f.getCreateDateTo()));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}