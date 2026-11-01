package com.example.ticketsmodule.domain.ticket.repository;

import com.example.ticketsmodule.domain.common.PageRequest;
import com.example.ticketsmodule.domain.common.PageResult;
import com.example.ticketsmodule.domain.ticket.model.Ticket;
import com.example.ticketsmodule.domain.ticket.model.TicketFilter;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface TicketsRepositoryPort {
    Ticket save(Ticket ticket);
    List<Ticket> saveAll(List<Ticket> tickets);
    Optional<Ticket> findById(UUID id);
    PageResult<Ticket> findAll(TicketFilter filter, PageRequest page);
    Set<Integer> findSeatNumbersByRouteId(UUID routeId);
    List<Ticket> findPurchasedByUserId(UUID userId);
    List<Ticket> findAvailableByRouteId(UUID routeId);
}