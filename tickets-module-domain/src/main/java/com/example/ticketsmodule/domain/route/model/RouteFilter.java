package com.example.ticketsmodule.domain.route.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteFilter {
    private String query;
    private String departurePoint;
    private String destinationPoint;
    private LocalDateTime departureFrom;
    private LocalDateTime departureTo;
    private LocalDateTime destinationFrom;
    private LocalDateTime destinationTo;
    private LocalDateTime createDateFrom;
    private LocalDateTime createDateTo;
    private Boolean emptySits;
}