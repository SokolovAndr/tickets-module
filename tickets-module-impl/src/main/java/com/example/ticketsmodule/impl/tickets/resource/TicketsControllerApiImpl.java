package com.example.ticketsmodule.impl.tickets.resource;

import com.example.ticketsmodule.impl.oauth.service.JwtCurrentUserService;
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

    private final TicketsService ticketsService;
    private final JwtCurrentUserService jwtCurrentUserService;

    @Override
    public ResponseEntity<CreateTicketResponse> createTicket(@RequestBody CreateTicketRequest createTicketRequest) {
        CreateTicketResponse result = ticketsService.create(createTicketRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Override
    public ResponseEntity<Page> getAllMyTickets(
            @Valid Pageable pageable,
            @Valid TicketsSearchRequest searchParam) {
        UUID userId = jwtCurrentUserService.getCurrentUserId();
        Page<CreateTicketResponse> result = ticketsService.findAllMyTickets(searchParam, pageable, userId);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Page> getAllTickets(@Valid Pageable pageable, @Valid TicketsSearchRequest searchParam) {
        Page<CreateTicketResponse> result = ticketsService.findAll(searchParam, pageable);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<CreateTicketResponse> getTicket(@NotNull UUID id) {
        CreateTicketResponse result = ticketsService.findOne(id);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Override
    public ResponseEntity<CreateTicketResponse> patchTicket(@NotNull UUID id, @Valid TicketPatchRequest ticketPatchRequest) {
        CreateTicketResponse result = ticketsService.patch(id, ticketPatchRequest);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<CreateTicketResponse> buyTicket(@NotNull UUID id) {
        UUID userId = jwtCurrentUserService.getCurrentUserId();
        CreateTicketResponse result = ticketsService.buyTicket(id, userId);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Override
    public ResponseEntity<CreateTicketResponse> returnTicket(@NotNull UUID id) {
        UUID userId = jwtCurrentUserService.getCurrentUserId();
        CreateTicketResponse result = ticketsService.returnTicket(id, userId);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Override
    public ResponseEntity<ReleaseTicketsBatchResponse> releaseTicketsBatch(@RequestBody ReleaseTicketsBatchRequest request) {
        ReleaseTicketsBatchResponse result = ticketsService.releaseTicketsBatch(request);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
