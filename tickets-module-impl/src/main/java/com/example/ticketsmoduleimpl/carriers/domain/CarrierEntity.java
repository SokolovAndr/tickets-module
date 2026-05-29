package com.example.ticketsmoduleimpl.carriers.domain;

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
 * Сущность перевозчика – компания, осуществляющая перевозки.
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
@Entity
@Table(name = "carriers")
@Comment("Сущность перевозчика – компания, осуществляющая перевозки.")
public class CarrierEntity {

    /**
     * Идентификатор
     */
    @Id
    @Comment("Идентификатор")
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    /**
     * Название компании
     */
    @NonNull
    @Comment("Название компании")
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    /**
     * Телефон
     */
    @NonNull
    @Comment("Телефон")
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

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
