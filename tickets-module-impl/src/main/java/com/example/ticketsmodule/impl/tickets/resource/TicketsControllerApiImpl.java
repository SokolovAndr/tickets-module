package com.example.ticketsmodule.impl.tickets.resource;

import com.example.ticketsmodule.impl.tickets.service.TicketsService;
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
import com.example.ticketsmodule.api.controller.TicketsControllerApi;
import com.example.ticketsmodule.api.model.CreateTicketRequest;
import com.example.ticketsmodule.api.model.CreateTicketResponse;
import com.example.ticketsmodule.api.model.ReleaseTicketsBatchRequest;
import com.example.ticketsmodule.api.model.ReleaseTicketsBatchResponse;
import com.example.ticketsmodule.api.model.TicketPatchRequest;
import com.example.ticketsmodule.api.model.TicketsSearchRequest;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TicketsControllerApiImpl implements TicketsControllerApi {

    private final TicketsService tickersService;

    @Override
    public ResponseEntity<CreateTicketResponse> createTicket(@RequestBody CreateTicketRequest createTicketRequest) {
        CreateTicketResponse result = tickersService.create(createTicketRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Override
    public ResponseEntity<Page> getAllTickets(@Valid Pageable pageable, @Valid TicketsSearchRequest searchParam) {
        Page<CreateTicketResponse> result = tickersService.findAll(searchParam, pageable);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<CreateTicketResponse> getTicket(@NotNull UUID id) {
        CreateTicketResponse result = tickersService.findOne(id);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Override
    public ResponseEntity<CreateTicketResponse> patchTicket(@NotNull UUID id, @Valid TicketPatchRequest ticketPatchRequest) {
        CreateTicketResponse result = tickersService.patch(id, ticketPatchRequest);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<CreateTicketResponse> buyTicket(@NotNull UUID id) {
        CreateTicketResponse result = tickersService.buyTicket(id);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Override
    public ResponseEntity<CreateTicketResponse> returnTicket(@NotNull UUID id) {
        CreateTicketResponse result = tickersService.returnTicket(id);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Override
    public ResponseEntity<ReleaseTicketsBatchResponse> releaseTicketsBatch(@RequestBody ReleaseTicketsBatchRequest request) {
        ReleaseTicketsBatchResponse result = tickersService.releaseTicketsBatch(request);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
