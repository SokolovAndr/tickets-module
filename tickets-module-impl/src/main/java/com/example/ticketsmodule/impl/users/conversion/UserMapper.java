package com.example.ticketsmodule.impl.users.conversion;

import com.example.ticketsmodule.api.model.UserResponse;
import com.example.ticketsmodule.domain.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserResponse toResponse(User user);
}