package com.example.ticketsmodule.domain.route.model;

import com.example.ticketsmodule.domain.carrier.model.Carrier;
import com.example.ticketsmodule.domain.common.model.CommonModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Route extends CommonModel {
    private String departurePoint;
    private String destinationPoint;
    private Carrier carrier;
    private int durationMinutes;
    private OffsetDateTime departureAt;
    private OffsetDateTime destinationAt;
}
