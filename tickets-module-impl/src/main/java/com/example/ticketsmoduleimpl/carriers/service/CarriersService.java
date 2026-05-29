package com.example.ticketsmoduleimpl.carriers.service;

import com.example.ticketsmoduleimpl.carriers.conversion.CarrierFromEntityConverter;
import com.example.ticketsmoduleimpl.carriers.conversion.CarrierToEntityConverter;
import com.example.ticketsmoduleimpl.carriers.domain.CarrierEntity;
import com.example.ticketsmoduleimpl.carriers.repository.CarriersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tickets_module.model.CreateCarrierRequest;
import tickets_module.model.CreateCarrierResponse;

/**
 * Сервис для работы с компаниями перевозчиками
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class CarriersService {

    private final CarriersRepository carriersRepository;
    private final CarrierToEntityConverter toEntityConverter;
    private final CarrierFromEntityConverter fromEntityConverter;

    @Transactional
    public CreateCarrierResponse create(CreateCarrierRequest createCarrierRequest) {
        final CarrierEntity carrierEntity = toEntityConverter.convert(createCarrierRequest);
        assert carrierEntity != null;
        CarrierEntity response = carriersRepository.save(carrierEntity);
        return fromEntityConverter.convert(response);
    }
}
