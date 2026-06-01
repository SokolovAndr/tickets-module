package com.example.ticketsmoduleimpl.carriers.repository;

import com.example.ticketsmoduleimpl.carriers.domain.CarrierEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Репозиторий компаний перевозчиков
 */
@Repository
public interface CarriersRepository extends JpaRepository<CarrierEntity, UUID>, JpaSpecificationExecutor<CarrierEntity> {
}
