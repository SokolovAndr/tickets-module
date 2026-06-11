package com.example.ticketsmoduleimpl.routes.conversion;

import com.example.ticketsmoduleimpl.routes.domain.RouteEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import tickets.model.CreateRouteResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouteFromEntityConverter implements Converter<RouteEntity, CreateRouteResponse> {


    @Override
    public CreateRouteResponse convert(RouteEntity source) {
        var response = new CreateRouteResponse();
        response.setId(source.getId());
        response.setDeparturePoint(source.getDeparturePoint());
        response.setDestinationPoint(source.getDestinationPoint());
        response.setCarrierId(source.getCarrier().getId());
        response.setDurationMinutes(source.getDurationMinutes());
        response.setDepartureAt(source.getDepartureAt());
        response.setDestinationAt(source.getDestinationAt());
        response.setCreatedAt(source.getCreatedAt());
        response.setUpdatedAt(source.getUpdatedAt());

        return response;
    }

}
