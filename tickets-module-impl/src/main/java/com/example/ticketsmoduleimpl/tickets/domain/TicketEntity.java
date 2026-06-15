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
import org.hibernate.annotations.Comment;
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
@Table(name = "tickets")
@Comment("Сущность билета – право проезда по определенному маршруту в определенную дату и место в транспортном средстве.")
public class TicketEntity {

    /**
     * Идентификатор
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    @Comment("Идентификатор")
    private UUID id;

    /**
     * Идентификатор маршрута
     */
    @Fetch(FetchMode.JOIN)
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH})
    @JoinColumn(name = "route_id", nullable = false)
    @Comment("Идентификатор маршрута")
    @ToString.Exclude
    private RouteEntity route;

    /**
     * Номер места
     */
    @Comment("Номер места")
    @Column(name = "seat_number", nullable = false)
    private int seatNumber;

    /**
     * Цена
     */
    @Comment("Цена")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Признак занятости
     */
    @Comment("Признак занятости")
    @Column(name = "is_purchased", nullable = false)
    private boolean isPurchased;

    /**
     * Идентификатор пользователя
     */
    @Fetch(FetchMode.JOIN)
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH})
    @JoinColumn(name = "purchased_by")
    @ToString.Exclude
    @Comment("Идентификатор пользователя")
    private UserEntity user;

    /**
     * Дата покупки
     */
    @Comment("Дата покупки")
    @Column(name = "purchased_at")
    private LocalDateTime purchasedAt;

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
