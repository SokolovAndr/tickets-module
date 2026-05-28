package com.example.ticketsmoduleimpl.users.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.jspecify.annotations.NonNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сущность пользователя – субъект, осуществляющий покупку билетов
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
@Entity
@Table(name = "users", comment = "Сущность пользователя – субъект, осуществляющий покупку билетов")
public class UserEntity {

    /**
     * Идентификатор
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", comment = "Идентификатор")
    private UUID id;

    /**
     * Логин
     */
    @NonNull
    @Column(name = "login", nullable = false, unique = true, comment = "Логин")
    private String login;

    /**
     * Пароль
     */
    @NonNull
    @Column(name = "password", nullable = false, comment = "Пароль")
    private String password;

    /**
     * ФИО
     */
    @NonNull
    @Column(name = "full_name", nullable = false, comment = "ФИО")
    private String fullName;

    /**
     * Роль
     */
    @NonNull
    @Column(name = "role", nullable = false, comment = "Роль")
    private UserRoleEnum role;

    /**
     * Дата создания
     */
    @Column(name = "created_at", comment = "Дата создания")
    private LocalDateTime createdAt;

    /**
     * Дата обновления
     */
    @Column(name = "updated_at", comment = "Дата обновления")
    private LocalDateTime updatedAt;
}
