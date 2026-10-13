package com.example.ticketsmodule.impl.users.conversion;

import com.example.ticketsmodule.api.model.UserResponse;
import com.example.ticketsmodule.impl.users.domain.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse (UserEntity entity);
}
