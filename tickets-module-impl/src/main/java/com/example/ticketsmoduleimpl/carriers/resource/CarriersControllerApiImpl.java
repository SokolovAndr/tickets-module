package com.example.ticketsmoduleimpl.carriers.resource;

import com.example.ticketsmoduleimpl.carriers.service.CarriersService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tickets.api.CarriersControllerApi;
import tickets.model.CarrierPatchRequest;
import tickets.model.CarriersSearchRequest;
import tickets.model.CreateCarrierRequest;
import tickets.model.CreateCarrierResponse;

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
        Page<CreateCarrierResponse> result = carriersService.findAll(searchParam, pageable);
        return ResponseEntity.ok(result);
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
