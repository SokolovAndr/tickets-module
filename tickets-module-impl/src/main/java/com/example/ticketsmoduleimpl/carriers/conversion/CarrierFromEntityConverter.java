package com.example.ticketsmoduleimpl.carriers.conversion;

import com.example.ticketsmoduleimpl.carriers.domain.CarrierEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import tickets.model.CreateCarrierResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class CarrierFromEntityConverter implements Converter<CarrierEntity, CreateCarrierResponse> {

    @Override
    public CreateCarrierResponse convert(CarrierEntity source) {
        var response = new CreateCarrierResponse();
        response.setId(source.getId());
        response.setName(source.getName());
        response.setPhone(source.getPhone());
        response.setCreatedAt(source.getCreatedAt());
        response.setUpdatedAt(source.getUpdatedAt());

        return response;
    }
}