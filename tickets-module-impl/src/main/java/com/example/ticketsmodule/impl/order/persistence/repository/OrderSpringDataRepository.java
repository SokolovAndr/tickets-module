package com.example.ticketsmodule.impl.order.persistence.repository;

import com.example.ticketsmodule.domain.order.model.OrderStatusEnum;
import com.example.ticketsmodule.impl.order.persistence.entity.OrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderSpringDataRepository extends JpaRepository<OrderJpaEntity, UUID>, JpaSpecificationExecutor<OrderJpaEntity> {

    @Query("select order from OrderJpaEntity order where order.ticket.id = :ticketId and order.status in :activeStatuses")
    Optional<OrderJpaEntity> findActiveByTicketId(UUID ticketId, @Param("activeStatuses") List<OrderStatusEnum> activeStatuses);

    @Query("select order from OrderJpaEntity order where order.ticket.id = :ticketId")
    List<OrderJpaEntity> findByTicketId(UUID ticketId);

    @Query("select order from OrderJpaEntity order where order.user.id = :userId")
    List<OrderJpaEntity> findByUserId(UUID userId);

    @Query("select order from OrderJpaEntity order where order.user.id = :userId and order.status = :status")
    List<OrderJpaEntity> findByUserIdAndStatus(UUID userId, OrderStatusEnum status);
}
