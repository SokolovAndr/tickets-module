package com.example.ticketsmodule.impl.order.persistence.entity;

import com.example.ticketsmodule.domain.order.model.OrderStatusEnum;
import com.example.ticketsmodule.impl.tickets.persistence.entity.TicketJpaEntity;
import com.example.ticketsmodule.impl.users.persistence.entity.UserJpaEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сущность заказа.
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
@Entity
@Table(name = "orders")
@Comment("Сущность заказа")
public class OrderJpaEntity {
    /**
     * Идентификатор
     */
    @Id
    @Comment("Идентификатор")
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    /**
     * Идентификатор билета
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH})
    @JoinColumn(name = "ticket_id", nullable = false)
    @ToString.Exclude
    @Comment("Идентификатор билета")
    private TicketJpaEntity ticket;

    /**
     * Идентификатор пользователя
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH})
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @Comment("Идентификатор пользователя")
    private UserJpaEntity user;

    /**
     * Цена
     */
    @Comment("Цена")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Статус
     */
    @Comment("Статус")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatusEnum status;

    /**
     * Забронировано до
     */
    @Comment("Забронировано до")
    @Column(name = "reserved_until")
    private LocalDateTime reservedUntil;

    /**
     * Дата оплаты
     */
    @Comment("Дата оплаты")
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    /**
     * Дата отмены
     */
    @Comment("Дата отмены")
    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    /**
     * Дата возврата
     */
    @Comment("Дата возврата")
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

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
