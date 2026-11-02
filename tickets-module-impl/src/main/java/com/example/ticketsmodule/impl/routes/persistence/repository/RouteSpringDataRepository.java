package com.example.ticketsmodule.impl.routes.persistence.repository;

import com.example.ticketsmodule.impl.routes.persistence.entity.RouteJpaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий маршрутов
 */
@Repository
public interface RouteSpringDataRepository extends JpaRepository<RouteJpaEntity, UUID>, JpaSpecificationExecutor<RouteJpaEntity> {

    @Override
    @EntityGraph(attributePaths = {"carrier"})
    Optional<RouteJpaEntity> findById(UUID id);
}
