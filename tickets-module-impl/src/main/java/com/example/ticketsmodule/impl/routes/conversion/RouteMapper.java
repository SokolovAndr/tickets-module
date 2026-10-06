package com.example.ticketsmodule.impl.routes.conversion;

import com.example.ticketsmodule.api.model.CreateRouteRequest;
import com.example.ticketsmodule.api.model.CreateRouteResponse;
import com.example.ticketsmodule.impl.carriers.domain.CarrierEntity;
import com.example.ticketsmodule.impl.routes.domain.RouteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RouteMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    RouteEntity toEntity (CreateRouteRequest request, CarrierEntity carrier);

    @Mapping(source = "carrier.id", target = "carrierId")
    CreateRouteResponse toResponse (RouteEntity entity);
}
