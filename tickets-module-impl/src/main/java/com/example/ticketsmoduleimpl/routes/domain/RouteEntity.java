package com.example.ticketsmoduleimpl.routes.domain;

import com.example.ticketsmoduleimpl.carriers.domain.CarrierEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.jspecify.annotations.NonNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сущность маршрута – определенный путь движения
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
@Entity
@Table(name = "routes", comment = "Сущность маршрута – определенный путь движения")
public class RouteEntity {

    /**
     * Идентификатор
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", comment = "Идентификатор")
    private UUID id;

    /**
     * Пункт отправления
     */
    @NonNull
    @Column(name = "departure_point", nullable = false, comment = "Пункт отправления", length = 255)
    private String departurePoint;

    /**
     * Пункт назначения
     */
    @NonNull
    @Column(name = "destination_point", nullable = false, comment = "Пункт назначения", length = 255)
    private String destinationPoint;

    /**
     * Идентификатор перевозчика
     */
    @Fetch(FetchMode.JOIN)
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH})
    @JoinColumn(name = "carrier_id", nullable = false, comment = "Идентификатор перевозчика")
    @ToString.Exclude
    private CarrierEntity carrier;

    /**
     * Длительность в минутах
     */
    @Column(name = "duration_minutes", nullable = false, comment = "Длительность в минутах")
    private int durationMinutes;

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
