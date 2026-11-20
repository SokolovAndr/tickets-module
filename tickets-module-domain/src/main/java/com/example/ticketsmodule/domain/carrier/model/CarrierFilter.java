package com.example.ticketsmodule.domain.carrier.model;

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
public class CarrierFilter {
    private String query;
    private String name;
    private String phone;
    private OffsetDateTime createDateFrom;
    private OffsetDateTime createDateTo;
}