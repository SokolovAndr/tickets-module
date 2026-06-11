package com.example.ticketsmoduleimpl.routes.resource;

import com.example.ticketsmoduleimpl.routes.service.RoutesService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tickets.api.RoutesControllerApi;
import tickets.model.CreateCarrierResponse;
import tickets.model.CreateRouteRequest;
import tickets.model.CreateRouteResponse;
import tickets.model.RoutePatchRequest;
import tickets.model.RoutesSearchRequest;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RoutesControllerApiImpl implements RoutesControllerApi {

    private final RoutesService routesService;


    @Override
    public ResponseEntity<CreateRouteResponse> createRoute(@RequestBody CreateRouteRequest createRouteRequest) {
        CreateRouteResponse result = routesService.create(createRouteRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Override
    public ResponseEntity<Page> getAllRoutes(@Valid Pageable pageable, @Valid RoutesSearchRequest searchParam) {
        Page<CreateRouteResponse> result = routesService.findAll(searchParam, pageable);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<CreateRouteResponse> getRoute(@NotNull UUID id) {
        CreateRouteResponse result = routesService.findOne(id);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Override
    public ResponseEntity<CreateRouteResponse> patchRoute(@NotNull UUID id, @Valid RoutePatchRequest routePatchRequest) {
        CreateRouteResponse result = routesService.patch(id, routePatchRequest);
        return ResponseEntity.ok(result);
    }
}
