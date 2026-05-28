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
@Table(name = "carriers", comment = "Сущность перевозчика – компания, осуществляющая перевозки.")
public class CarrierEntity {

    /**
     * Идентификатор
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", comment = "Идентификатор")
    private UUID id;

    /**
     * Название компании
     */
    @NonNull
    @Column(name = "name", nullable = false, unique = true, comment = "Название компании")
    private String name;

    /**
     * Телефон
     */
    @NonNull
    @Column(name = "phone", nullable = false, comment = "Телефон", length = 20)
    private String phone;

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
