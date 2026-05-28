package com.example.ticketsmoduleimpl.tickets.domain;

import com.example.ticketsmoduleimpl.routes.domain.RouteEntity;
import com.example.ticketsmoduleimpl.users.domain.UserEntity;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сущность билета – право проезда по определенному маршруту в определенную дату и место в транспортном средстве.
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
@Entity
@Table(name = "tickets", comment = "Сущность билета – право проезда по определенному маршруту в определенную дату и место в транспортном средстве.")
public class TicketEntity {

    /**
     * Идентификатор
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", comment = "Идентификатор")
    private UUID id;

    /**
     * Идентификатор маршрута
     */
    @Fetch(FetchMode.JOIN)
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH})
    @JoinColumn(name = "route_id", nullable = false, comment = "Идентификатор маршрута")
    @ToString.Exclude
    private RouteEntity route;

    /**
     * Время отправления
     */
    @NonNull
    @Column(name = "departure_datetime", nullable = false, comment = "Время отправления")
    private LocalDateTime departureDatetime;

    /**
     * Номер места
     */
    @Column(name = "seat_number", nullable = false, comment = "Номер места")
    private int seatNumber;

    /**
     * Цена
     */
    @Column(name = "price", nullable = false, comment = "Цена", precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Признак занятости
     */
    @Column(name = "is_purchased", nullable = false, comment = "Признак занятости")
    private boolean isPurchased;

    /**
     * Идентификатор пользователя
     */
    @Fetch(FetchMode.JOIN)
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH})
    @JoinColumn(name = "purchased_by", comment = "Идентификатор пользователя")
    @ToString.Exclude
    private UserEntity user;

    /**
     * Дата покупки
     */
    @Column(name = "purchased_at", comment = "Дата покупки")
    private LocalDateTime purchasedAt;

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
