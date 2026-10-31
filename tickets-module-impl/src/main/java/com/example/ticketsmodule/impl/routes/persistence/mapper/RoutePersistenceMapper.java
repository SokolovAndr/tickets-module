package com.example.ticketsmodule.impl.routes.persistence.mapper;

import com.example.ticketsmodule.domain.route.model.Route;
import com.example.ticketsmodule.impl.carriers.persistence.mapper.CarrierPersistenceMapper;
import com.example.ticketsmodule.impl.routes.persistence.entity.RouteJpaEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Маппер для перевода Entity в Domain и обратно
 */

@Mapper(componentModel = "spring", uses = {CarrierPersistenceMapper.class})
public interface RoutePersistenceMapper {

    Route toDomain(RouteJpaEntity entity);

    RouteJpaEntity toEntity(Route domain);

    List<Route> toDomainList(List<RouteJpaEntity> entities);
}
