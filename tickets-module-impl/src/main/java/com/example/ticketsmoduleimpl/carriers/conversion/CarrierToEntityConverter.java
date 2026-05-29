package com.example.ticketsmoduleimpl.carriers.conversion;

import com.example.ticketsmoduleimpl.carriers.domain.CarrierEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import tickets.model.CreateCarrierRequest;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class CarrierToEntityConverter implements Converter<CreateCarrierRequest, CarrierEntity> {

    @Override
    public CarrierEntity convert(CreateCarrierRequest source) {
        return CarrierEntity.builder().
                name(source.getName())
                .phone(source.getPhone())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now()).build();
    }
}
