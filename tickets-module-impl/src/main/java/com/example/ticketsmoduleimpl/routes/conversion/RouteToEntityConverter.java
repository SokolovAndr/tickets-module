package com.example.ticketsmoduleimpl.routes.conversion;

import com.example.ticketsmoduleimpl.carriers.domain.CarrierEntity;
import com.example.ticketsmoduleimpl.routes.domain.RouteEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tickets.model.CreateRouteRequest;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouteToEntityConverter  {

    public RouteEntity convert(CreateRouteRequest source, CarrierEntity carrier) {
        return RouteEntity.builder()
                .departurePoint(source.getDeparturePoint())
                .destinationPoint(source.getDestinationPoint())
                .carrier(carrier)
                .durationMinutes(source.getDurationMinutes())
                .departureAt(source.getDepartureAt())
                .destinationAt(source.getDestinationAt())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
