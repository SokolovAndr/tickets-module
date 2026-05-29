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
import org.hibernate.annotations.Comment;
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
@Table(name = "users")
@Comment("Сущность пользователя – субъект, осуществляющий покупку билетов")
public class UserEntity {

    /**
     * Идентификатор
     */
    @Id
    @Comment("Идентификатор")
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    /**
     * Логин
     */
    @NonNull
    @Comment("Логин")
    @Column(name = "login", nullable = false, unique = true)
    private String login;

    /**
     * Пароль
     */
    @NonNull
    @Comment("Пароль")
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * ФИО
     */
    @NonNull
    @Comment("ФИО")
    @Column(name = "full_name", nullable = false)
    private String fullName;

    /**
     * Роль
     */
    @NonNull
    @Comment("Роль")
    @Column(name = "role", nullable = false)
    private UserRoleEnum role;

    /**
     * Дата создания
     */
    @Comment("Дата создания")
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Дата обновления
     */
    @Comment("Дата обновления")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
