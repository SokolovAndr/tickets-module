package com.example.ticketsmodule.impl.users.persistence.mapper;

import com.example.ticketsmodule.domain.user.model.User;
import com.example.ticketsmodule.impl.users.persistence.entity.UserJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserPersistenceMapper {
    User toDomain(UserJpaEntity entity);
    UserJpaEntity toEntity(User domain);
}