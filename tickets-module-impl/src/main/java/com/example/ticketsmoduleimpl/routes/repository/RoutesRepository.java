package com.example.ticketsmoduleimpl.routes.repository;

import com.example.ticketsmoduleimpl.routes.domain.RouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Репозиторий маршрутов
 */
@Repository
public interface RoutesRepository extends JpaRepository<RouteEntity, UUID>, JpaSpecificationExecutor<RouteEntity> {
}
