package com.example.ticketsmodule.domain.carrier.model;

import com.example.ticketsmodule.domain.common.model.CommonModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Carrier extends CommonModel {
    private String name;
    private String phone;
}