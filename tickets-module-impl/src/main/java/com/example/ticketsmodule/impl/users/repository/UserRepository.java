package com.example.ticketsmodule.impl.users.repository;

import com.example.ticketsmodule.impl.users.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий пользователей
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByLogin(String login);
}
