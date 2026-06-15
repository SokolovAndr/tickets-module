package com.example.ticketsmoduleimpl.routes.service;

import com.example.ticketsmoduleimpl.carriers.domain.CarrierEntity;
import com.example.ticketsmoduleimpl.carriers.service.CarriersService;
import com.example.ticketsmoduleimpl.routes.conversion.RouteFromEntityConverter;
import com.example.ticketsmoduleimpl.routes.conversion.RouteToEntityConverter;
import com.example.ticketsmoduleimpl.routes.domain.RouteEntity;
import com.example.ticketsmoduleimpl.routes.repository.RoutesRepository;
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
import tickets.model.CreateRouteRequest;
import tickets.model.CreateRouteResponse;
import tickets.model.RoutePatchRequest;
import tickets.model.RoutesSearchRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Сервис для работы с маршрутами
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutesService {

    private final RoutesRepository routesRepository;
    private final RouteFromEntityConverter fromEntityConverter;
    private final RouteToEntityConverter toEntityConverter;
    private final CarriersService carriersService;



    @Transactional
    public CreateRouteResponse create(CreateRouteRequest createRouteRequest) {

        final CarrierEntity carrier = carriersService.findOneById(createRouteRequest.getCarrierId());

        final RouteEntity routeEntity = toEntityConverter.convert(createRouteRequest, carrier);
        assert routeEntity != null;
        RouteEntity response = routesRepository.save(routeEntity);
        return fromEntityConverter.convert(response);
    }

    @Transactional(readOnly = true)
    public Page<CreateRouteResponse> findAll(@Valid RoutesSearchRequest searchParam, @Valid Pageable pageable) {
        Specification<RouteEntity> spec = buildSpecification(searchParam);
        return routesRepository.findAll(spec, pageable).map(fromEntityConverter::convert);
    }

    private Specification<RouteEntity> buildSpecification(RoutesSearchRequest searchParam) {
        return (root, query, cb) -> {
            final List<Predicate> predicates = new ArrayList<>();

            query.orderBy(cb.desc(root.get("createdAt")));

            if (searchParam != null) {
                if (StringUtils.hasText(searchParam.getDeparturePoint())) {
                    var pattern = "%" + searchParam.getDeparturePoint().toLowerCase() + "%";
                    predicates.add(cb.like(cb.lower(root.get("departurePoint")), pattern));
                }

                if (StringUtils.hasText(searchParam.getDestinationPoint())) {
                    var pattern = "%" + searchParam.getDestinationPoint().toLowerCase() + "%";
                    predicates.add(cb.like(cb.lower(root.get("destinationPoint")), pattern));
                }

                if (searchParam.getDepartureFrom() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("departureAt"), searchParam.getDepartureFrom()));
                }

                if (searchParam.getDepartureTo() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("departureAt"), searchParam.getDepartureTo()));
                }

                if (searchParam.getDestinationFrom() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("destinationAt"), searchParam.getDestinationFrom()));
                }

                if (searchParam.getDestinationTo() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("destinationAt"), searchParam.getDestinationTo()));
                }

                if (searchParam.getCreateDateFrom() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), searchParam.getCreateDateFrom()));
                }

                if (searchParam.getCreateDateTo() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), searchParam.getCreateDateTo()));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional(readOnly = true)
    public CreateRouteResponse findOne(@NotNull UUID id) {
        RouteEntity response = routesRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Route with id " + id + " not found"));
        return fromEntityConverter.convert(response);
    }

    @Transactional(readOnly = true)
    public RouteEntity findOneById(UUID id) {
        return routesRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Route with id " + id + " not found"));
    }

    @Transactional
    public CreateRouteResponse patch(@NotNull UUID id, @Valid RoutePatchRequest routePatchRequest) {
        var result = routesRepository.findById(id).map(entity -> {
            if (routePatchRequest.getDeparturePoint() != null) {
                entity.setDeparturePoint(routePatchRequest.getDeparturePoint());
            }
            if (routePatchRequest.getDestinationPoint() != null) {
                entity.setDestinationPoint(routePatchRequest.getDestinationPoint());
            }
            if (routePatchRequest.getDepartureAt() != null) {
                entity.setDepartureAt(routePatchRequest.getDepartureAt());
            }
            if (routePatchRequest.getDestinationAt() != null) {
                entity.setDestinationAt(routePatchRequest.getDestinationAt());
            }
            if (routePatchRequest.getCarrierId() != null) {
                final CarrierEntity carrier = carriersService.findOneById(routePatchRequest.getCarrierId());
                entity.setCarrier(carrier);
            }
            if (routePatchRequest.getDurationMinutes() != null) {
                entity.setDurationMinutes(routePatchRequest.getDurationMinutes());
            }
            entity.setUpdatedAt(LocalDateTime.now());

            return routesRepository.save(entity);
        }).orElseThrow(() -> new EntityNotFoundException(String.format("Route with id %s not found", routePatchRequest)));

        return fromEntityConverter.convert(result);
    }
}
