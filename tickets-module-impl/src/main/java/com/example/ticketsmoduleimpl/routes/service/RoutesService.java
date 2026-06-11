package com.example.ticketsmoduleimpl.routes.service;

import com.example.ticketsmoduleimpl.carriers.domain.CarrierEntity;
import com.example.ticketsmoduleimpl.carriers.service.CarriersService;
import com.example.ticketsmoduleimpl.routes.conversion.RouteFromEntityConverter;
import com.example.ticketsmoduleimpl.routes.conversion.RouteToEntityConverter;
import com.example.ticketsmoduleimpl.routes.domain.RouteEntity;
import com.example.ticketsmoduleimpl.routes.repository.RoutesRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tickets.model.CreateCarrierResponse;
import tickets.model.CreateRouteRequest;
import tickets.model.CreateRouteResponse;
import tickets.model.RoutePatchRequest;
import tickets.model.RoutesSearchRequest;

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
        return null;
    }

    @Transactional(readOnly = true)
    public CreateRouteResponse findOne(@NotNull UUID id) {
        return null;
    }

    @Transactional
    public CreateRouteResponse patch(@NotNull UUID id, @Valid RoutePatchRequest routePatchRequest) {
        return null;
    }
}
