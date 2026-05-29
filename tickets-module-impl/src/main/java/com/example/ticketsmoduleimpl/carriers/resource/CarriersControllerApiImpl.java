package com.example.ticketsmoduleimpl.carriers.resource;

import com.example.ticketsmoduleimpl.carriers.service.CarriersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tickets.api.CarriersControllerApi;
import tickets.model.CreateCarrierRequest;
import tickets.model.CreateCarrierResponse;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CarriersControllerApiImpl implements CarriersControllerApi {
    private final CarriersService carriersService;

    @Override
    public ResponseEntity<CreateCarrierResponse> createCarrier(@RequestBody CreateCarrierRequest createCarrierRequest) {
        log.debug("createCarrier response: {}", createCarrierRequest);

        CreateCarrierResponse result = carriersService.create(createCarrierRequest);
        log.debug("createCarrier result: {}", result);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
