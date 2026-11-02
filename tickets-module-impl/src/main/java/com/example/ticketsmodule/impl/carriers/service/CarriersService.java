package com.example.ticketsmodule.impl.carriers.service;

import com.example.ticketsmodule.domain.carrier.exception.CarrierNameAlreadyExistsException;
import com.example.ticketsmodule.domain.carrier.exception.CarrierNotFoundException;
import com.example.ticketsmodule.domain.carrier.model.Carrier;
import com.example.ticketsmodule.domain.carrier.model.CarrierFilter;
import com.example.ticketsmodule.domain.carrier.repository.CarriersRepositoryPort;
import com.example.ticketsmodule.domain.common.PageRequest;
import com.example.ticketsmodule.domain.common.PageResult;
import com.example.ticketsmodule.impl.carriers.conversion.CarrierMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.ticketsmodule.api.model.CarrierPatchRequest;
import com.example.ticketsmodule.api.model.CarriersSearchRequest;
import com.example.ticketsmodule.api.model.CreateCarrierRequest;
import com.example.ticketsmodule.api.model.CreateCarrierResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Сервис для работы с компаниями перевозчиками
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class CarriersService {

    private final CarriersRepositoryPort carriersRepositoryPort;
    private final CarrierMapper carrierMapper;

    @Transactional
    public CreateCarrierResponse create(CreateCarrierRequest createCarrierRequest) {
        if (carriersRepositoryPort.existsByName(createCarrierRequest.getName())) {
            throw new CarrierNameAlreadyExistsException(createCarrierRequest.getName());
        }
        final Carrier carrier = carrierMapper.toDomain(createCarrierRequest);
        if (carrier == null) {
            throw new IllegalStateException("Failed to convert carrier entity");
        }
        carrier.setCreatedAt(LocalDateTime.now());
        carrier.setUpdatedAt(LocalDateTime.now());
        return carrierMapper.toResponse(carriersRepositoryPort.save(carrier));
    }

    @Transactional(readOnly = true)
    public CreateCarrierResponse findOne(UUID id) {
        Carrier carrier = carriersRepositoryPort.findById(id).orElseThrow(() -> new CarrierNotFoundException(id));
        return carrierMapper.toResponse(carrier);
    }

    @Transactional(readOnly = true)
    public Carrier findOneById(UUID id) {
        return carriersRepositoryPort.findById(id).orElseThrow(() ->new CarrierNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public PageResult<CreateCarrierResponse> findAll(CarriersSearchRequest req, PageRequest page) {
        CarrierFilter filter = toFilter(req);
        PageResult<Carrier> result = carriersRepositoryPort.findAll(filter, page);

        List<CreateCarrierResponse> dtos = result.getContent().stream()
                .map(carrierMapper::toResponse)
                .toList();

        return PageResult.<CreateCarrierResponse>builder()
                .content(dtos)
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .pageNumber(result.getPageNumber())
                .pageSize(result.getPageSize())
                .build();
    }

    @Transactional
    public CreateCarrierResponse patch(@NotNull UUID id, @Valid CarrierPatchRequest carrierPatchRequest) {

        var result = carriersRepositoryPort.findById(id).map(carrier -> {
            if (carrierPatchRequest.getName() != null) {
                carrier.setName(carrierPatchRequest.getName());
            }
            if (carrierPatchRequest.getPhone() != null) {
                carrier.setPhone(carrierPatchRequest.getPhone());
            }
            carrier.setUpdatedAt(LocalDateTime.now());

            return carriersRepositoryPort.save(carrier);
        }).orElseThrow(() -> new CarrierNotFoundException(id));

        return carrierMapper.toResponse(result);
    }

    private CarrierFilter toFilter(CarriersSearchRequest req) {
        if (req == null) return CarrierFilter.builder().build();
        return CarrierFilter.builder()
                .query(req.getQuery())
                .name(req.getName())
                .phone(req.getPhone())
                .createDateFrom(req.getCreateDateFrom() != null ? req.getCreateDateFrom().atStartOfDay() : null)
                .createDateTo(req.getCreateDateTo() != null ? req.getCreateDateTo().atTime(23, 59, 59) : null)
                .build();
    }
}
