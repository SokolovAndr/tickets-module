package com.example.ticketsmodule.impl.carriers.resource;

import com.example.ticketsmodule.api.controller.CarriersControllerApi;
import com.example.ticketsmodule.api.model.CarrierPatchRequest;
import com.example.ticketsmodule.api.model.CarriersSearchRequest;
import com.example.ticketsmodule.api.model.CreateCarrierRequest;
import com.example.ticketsmodule.api.model.CreateCarrierResponse;
import com.example.ticketsmodule.domain.common.PageRequest;
import com.example.ticketsmodule.domain.common.PageResult;
import com.example.ticketsmodule.impl.carriers.service.CarriersService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CarriersControllerApiImpl implements CarriersControllerApi {
    private final CarriersService carriersService;

    @Override
    public ResponseEntity<CreateCarrierResponse> createCarrier(@RequestBody CreateCarrierRequest createCarrierRequest) {
        CreateCarrierResponse result = carriersService.create(createCarrierRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Override
    public ResponseEntity<Page> getAllCarriers(@Valid Pageable pageable, @Valid CarriersSearchRequest searchParam) {
        PageRequest domainPage = PageRequest.builder()
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .build();

        PageResult<CreateCarrierResponse> result = carriersService.findAll(searchParam, domainPage);

        Page<CreateCarrierResponse> springPage = new PageImpl<>(
                result.getContent(),
                pageable,
                result.getTotalElements()
        );

        return ResponseEntity.ok(springPage);
    }

    @Override
    public ResponseEntity<CreateCarrierResponse> getCarrier(@NotNull UUID id) {
        CreateCarrierResponse result = carriersService.findOne(id);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Override
    public ResponseEntity<CreateCarrierResponse> patchCarrier(@NotNull UUID id, @Valid CarrierPatchRequest carrierPatchRequest) {
        CreateCarrierResponse result = carriersService.patch(id, carrierPatchRequest);
        return ResponseEntity.ok(result);
    }
}