package com.example.ticketsmodule.domain.route.model;

import com.example.ticketsmodule.domain.carrier.model.Carrier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route {
    private UUID id;
    private String departurePoint;
    private String destinationPoint;
    private Carrier carrier;
    private int durationMinutes;
    private LocalDateTime departureAt;
    private LocalDateTime destinationAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
