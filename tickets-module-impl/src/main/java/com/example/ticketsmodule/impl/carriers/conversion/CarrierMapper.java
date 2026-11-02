package com.example.ticketsmodule.impl.carriers.conversion;

import com.example.ticketsmodule.api.model.CreateCarrierRequest;
import com.example.ticketsmodule.api.model.CreateCarrierResponse;
import com.example.ticketsmodule.domain.carrier.model.Carrier;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CarrierMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Carrier toDomain (CreateCarrierRequest request);

    CreateCarrierResponse toResponse (Carrier entity);
}
