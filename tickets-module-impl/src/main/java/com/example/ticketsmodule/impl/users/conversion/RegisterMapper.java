package com.example.ticketsmodule.impl.users.conversion;

import com.example.ticketsmodule.api.model.RegisterRequest;
import com.example.ticketsmodule.impl.users.domain.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RegisterMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserEntity toEntity (RegisterRequest source);

}
