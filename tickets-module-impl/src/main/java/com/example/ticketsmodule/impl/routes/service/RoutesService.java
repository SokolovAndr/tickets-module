package com.example.ticketsmodule.impl.routes.service;

import com.example.ticketsmodule.domain.carrier.exception.CarrierNotFoundException;
import com.example.ticketsmodule.domain.carrier.model.Carrier;
import com.example.ticketsmodule.domain.carrier.repository.CarriersRepositoryPort;
import com.example.ticketsmodule.domain.common.PageRequest;
import com.example.ticketsmodule.domain.common.PageResult;
import com.example.ticketsmodule.domain.route.exception.RouteNotFoundException;
import com.example.ticketsmodule.domain.route.model.Route;
import com.example.ticketsmodule.domain.route.model.RouteFilter;
import com.example.ticketsmodule.domain.route.repository.RoutesRepositoryPort;
import com.example.ticketsmodule.impl.routes.conversion.RouteMapper;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.ticketsmodule.api.model.CreateRouteRequest;
import com.example.ticketsmodule.api.model.CreateRouteResponse;
import com.example.ticketsmodule.api.model.RoutePatchRequest;
import com.example.ticketsmodule.api.model.RoutesSearchRequest;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Сервис для работы с маршрутами
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutesService {

    private final RoutesRepositoryPort routesRepositoryPort;
    private final CarriersRepositoryPort carriersRepositoryPort;
    private final RouteMapper routeMapper;

    @Transactional
    public CreateRouteResponse create(CreateRouteRequest createRouteRequest) {
        Carrier carrier = carriersRepositoryPort.findById(createRouteRequest.getCarrierId()).orElseThrow(() -> new CarrierNotFoundException(createRouteRequest.getCarrierId()));

        Route route = routeMapper.toDomain(createRouteRequest);
        route.setCarrier(carrier);
        route.setCreatedAt(LocalDateTime.now());
        route.setUpdatedAt(LocalDateTime.now());
        return routeMapper.toResponse(routesRepositoryPort.save(route));
    }

    @Transactional(readOnly = true)
    public PageResult<CreateRouteResponse> findAll(RoutesSearchRequest req, PageRequest page) {
        RouteFilter filter = toFilter(req);
        PageResult<Route> result = routesRepositoryPort.findAll(filter, page);

        List<CreateRouteResponse> content = result.getContent().stream().map(routeMapper::toResponse).toList();

        return PageResult.<CreateRouteResponse>builder().content(content).totalElements(result.getTotalElements()).totalPages(result.getTotalPages()).pageNumber(result.getPageNumber()).pageSize(result.getPageSize()).build();
    }

    @Transactional(readOnly = true)
    public CreateRouteResponse findOne(@NotNull UUID id) {
        Route route = routesRepositoryPort.findById(id).orElseThrow(() -> new RouteNotFoundException(id));
        return routeMapper.toResponse(route);
    }

    @Transactional(readOnly = true)
    public Route findOneById(UUID id) {
        return routesRepositoryPort.findById(id).orElseThrow(() -> new RouteNotFoundException(id));
    }

    @Transactional
    public CreateRouteResponse patch(@NotNull UUID id, @Valid RoutePatchRequest req) {
        Route route = routesRepositoryPort.findById(id).orElseThrow(() -> new RouteNotFoundException(id));

        if (req.getDeparturePoint() != null) route.setDeparturePoint(req.getDeparturePoint());
        if (req.getDestinationPoint() != null) route.setDestinationPoint(req.getDestinationPoint());
        if (req.getDepartureAt() != null) route.setDepartureAt(req.getDepartureAt());
        if (req.getDestinationAt() != null) route.setDestinationAt(req.getDestinationAt());
        if (req.getDurationMinutes() != null) route.setDurationMinutes(req.getDurationMinutes());

        if (req.getCarrierId() != null) {
            Carrier carrier = carriersRepositoryPort.findById(req.getCarrierId()).orElseThrow(() -> new CarrierNotFoundException(req.getCarrierId()));
            route.setCarrier(carrier);
        }

        route.setUpdatedAt(LocalDateTime.now());
        return routeMapper.toResponse(routesRepositoryPort.save(route));
    }

    private RouteFilter toFilter(RoutesSearchRequest req) {
        if (req == null) return RouteFilter.builder().build();
        return RouteFilter.builder().query(req.getQuery()).departurePoint(req.getDeparturePoint()).destinationPoint(req.getDestinationPoint()).departureFrom(req.getDepartureFrom() != null ? req.getDepartureFrom().atStartOfDay() : null).departureTo(req.getDepartureTo() != null ? req.getDepartureTo().atTime(LocalTime.MAX) : null).destinationFrom(req.getDestinationFrom() != null ? req.getDestinationFrom().atStartOfDay() : null).destinationTo(req.getDestinationTo() != null ? req.getDestinationTo().atTime(LocalTime.MAX) : null).createDateFrom(req.getCreateDateFrom() != null ? req.getCreateDateFrom().atStartOfDay() : null).createDateTo(req.getCreateDateTo() != null ? req.getCreateDateTo().atTime(LocalTime.MAX) : null).emptySits(req.getEmptySits()).build();
    }
}