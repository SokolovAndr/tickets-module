package com.example.ticketsmoduleimpl.tickets.repository;

import com.example.ticketsmoduleimpl.tickets.domain.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Репозиторий билетов
 */
@Repository
public interface TicketRepository extends JpaRepository<TicketEntity, UUID>, JpaSpecificationExecutor<TicketEntity> {

    @Query("SELECT t.seatNumber FROM TicketEntity t WHERE t.route.id = :routeId")
    Set<Integer> findSeatNumbersByRouteId(@Param("routeId") UUID routeId);

    List<TicketEntity> findByRouteId(UUID routeId);

}
