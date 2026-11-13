package com.example.ticketsmodule.impl.order.persistence.repository;

import com.example.ticketsmodule.domain.order.model.OrderStatusEnum;
import com.example.ticketsmodule.impl.order.persistence.entity.OrderJpaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderSpringDataRepository extends JpaRepository<OrderJpaEntity, UUID>, JpaSpecificationExecutor<OrderJpaEntity> {

    @Override
    @EntityGraph(attributePaths = {"ticket", "ticket.route", "user"})
    Optional<OrderJpaEntity> findById(UUID id);

    @Query("select o from OrderJpaEntity o where o.ticket.id = :ticketId and o.status in :activeStatuses")
    @EntityGraph(attributePaths = {"ticket", "ticket.route", "user"})
    Optional<OrderJpaEntity> findActiveByTicketId(@Param("ticketId") UUID ticketId, @Param("activeStatuses") List<OrderStatusEnum> activeStatuses);

    @Query("select o from OrderJpaEntity o where o.ticket.id = :ticketId")
    @EntityGraph(attributePaths = {"ticket", "ticket.route", "user"})
    List<OrderJpaEntity> findByTicketId(@Param("ticketId") UUID ticketId);

    @Query("select o from OrderJpaEntity o where o.user.id = :userId")
    @EntityGraph(attributePaths = {"ticket", "ticket.route", "user"})
    List<OrderJpaEntity> findByUserId(@Param("userId") UUID userId);

    @Query("select o from OrderJpaEntity o where o.user.id = :userId and o.status = :status")
    @EntityGraph(attributePaths = {"ticket", "ticket.route", "user"})
    List<OrderJpaEntity> findByUserIdAndStatus(
            @Param("userId") UUID userId,
            @Param("status") OrderStatusEnum status);

    @Query("select o from OrderJpaEntity o where o.status = :status and o.reservedUntil < :now")
    @EntityGraph(attributePaths = {"ticket", "ticket.route"})
    List<OrderJpaEntity> findExpiredWaiting(
            @Param("status") OrderStatusEnum status,
            @Param("now") LocalDateTime now);
}
