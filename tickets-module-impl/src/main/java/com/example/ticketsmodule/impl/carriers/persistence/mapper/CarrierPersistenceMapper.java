package com.example.ticketsmodule.impl.carriers.persistence.mapper;

import com.example.ticketsmodule.domain.carrier.model.Carrier;
import com.example.ticketsmodule.impl.carriers.persistence.entity.CarrierJpaEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Маппер для перевода Entity в Domain и обратно
 */

@Mapper(componentModel = "spring")
public interface CarrierPersistenceMapper {

    Carrier toDomain(CarrierJpaEntity entity);

    CarrierJpaEntity toEntity(Carrier domain);

    List<Carrier> toDomainList(List<CarrierJpaEntity> entities);
}