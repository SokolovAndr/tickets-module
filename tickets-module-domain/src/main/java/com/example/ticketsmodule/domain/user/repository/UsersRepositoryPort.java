package com.example.ticketsmodule.domain.user.repository;

import com.example.ticketsmodule.domain.user.model.User;
import java.util.Optional;
import java.util.UUID;

public interface UsersRepositoryPort {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByLogin(String login);
}