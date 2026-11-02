package com.example.ticketsmodule.impl.users.persistence.repository;

import com.example.ticketsmodule.impl.users.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий пользователей
 */
@Repository
public interface UserSpringDataRepository extends JpaRepository<UserJpaEntity, UUID> {
    Optional<UserJpaEntity> findByLogin(String login);
}
