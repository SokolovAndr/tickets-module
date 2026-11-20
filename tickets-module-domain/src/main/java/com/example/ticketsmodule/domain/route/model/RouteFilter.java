package com.example.ticketsmodule.domain.route.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteFilter {
    private String query;
    private String departurePoint;
    private String destinationPoint;
    private OffsetDateTime departureFrom;
    private OffsetDateTime departureTo;
    private OffsetDateTime destinationFrom;
    private OffsetDateTime destinationTo;
    private OffsetDateTime createDateFrom;
    private OffsetDateTime createDateTo;
    private Boolean emptySits;
}