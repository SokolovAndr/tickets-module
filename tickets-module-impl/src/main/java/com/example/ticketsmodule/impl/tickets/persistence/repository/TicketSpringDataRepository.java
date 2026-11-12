package com.example.ticketsmodule.impl.tickets.persistence.repository;

import com.example.ticketsmodule.impl.tickets.persistence.entity.TicketJpaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Репозиторий билетов
 */
@Repository
public interface TicketSpringDataRepository extends JpaRepository<TicketJpaEntity, UUID>, JpaSpecificationExecutor<TicketJpaEntity> {

    @Override
    @EntityGraph(attributePaths = {"route", "route.carrier", "user"})
    Optional<TicketJpaEntity> findById(UUID id);

    @Query("SELECT t.seatNumber FROM TicketJpaEntity t WHERE t.route.id = :routeId")
    Set<Integer> findSeatNumbersByRouteId(@Param("routeId") UUID routeId);

    List<TicketJpaEntity> findByRouteId(UUID routeId);

    @Query("SELECT t FROM TicketJpaEntity t WHERE t.user.id = :userId AND t.ticketStatus = 'PURCHASED' ORDER BY t.purchasedAt DESC")
    @EntityGraph(attributePaths = {"route", "route.carrier", "user"})
    List<TicketJpaEntity> findPurchasedByUserId(@Param("userId") UUID userId);

    @Query("SELECT t FROM TicketJpaEntity t WHERE t.route.id = :routeId AND t.ticketStatus = 'AVAILABLE' ORDER BY t.seatNumber ASC")
    @EntityGraph(attributePaths = {"route", "route.carrier"})
    List<TicketJpaEntity> findAvailableByRouteId(@Param("routeId") UUID routeId);
}
