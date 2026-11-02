package com.example.ticketsmodule.impl.carriers.persistence.repository;

import com.example.ticketsmodule.impl.carriers.persistence.entity.CarrierJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Репозиторий компаний перевозчиков
 */
@Repository
public interface CarrierSpringDataRepository extends JpaRepository<CarrierJpaEntity, UUID>, JpaSpecificationExecutor<CarrierJpaEntity> {

    boolean existsByName(String name);

}
