package com.example.ticketsmodule.domain.route.repository;

import com.example.ticketsmodule.domain.common.PageRequest;
import com.example.ticketsmodule.domain.common.PageResult;
import com.example.ticketsmodule.domain.route.model.Route;
import com.example.ticketsmodule.domain.route.model.RouteFilter;

import java.util.Optional;
import java.util.UUID;

public interface RoutesRepositoryPort {
    Route save(Route route);
    Optional<Route> findById(UUID id);
    PageResult<Route> findAll(RouteFilter filter, PageRequest page);
}
