package com.example.ticketsmodule.impl.order.persistence.adapter;

import com.example.ticketsmodule.domain.common.PageRequest;
import com.example.ticketsmodule.domain.common.PageResult;
import com.example.ticketsmodule.domain.order.model.Order;
import com.example.ticketsmodule.domain.order.model.OrderStatusEnum;
import com.example.ticketsmodule.domain.order.model.OrdersFilter;
import com.example.ticketsmodule.domain.order.repository.OrdersRepositoryPort;
import com.example.ticketsmodule.impl.carriers.persistence.entity.CarrierJpaEntity;
import com.example.ticketsmodule.impl.order.persistence.entity.OrderJpaEntity;
import com.example.ticketsmodule.impl.order.persistence.mapper.OrderPersistenceMapper;
import com.example.ticketsmodule.impl.order.persistence.repository.OrderSpringDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderJpaRepositoryAdapter implements OrdersRepositoryPort {

    private final OrderSpringDataRepository springRepo;
    private final OrderPersistenceMapper mapper;

    @Override
    public Order save(Order order) {
        OrderJpaEntity saved = springRepo.save(mapper.toEntity(order));
        return mapper.toDomain(saved);
    }

    /**
     * @param id идентификатор заказа
     * @return Optional<Order>
     */
    @Override
    public Optional<Order> findById(UUID id) {
        return springRepo.findById(id).map(mapper::toDomain);
    }

    /**
     * @param ticketId идентификатор билета
     * @return Optional<Order>
     */
    @Override
    public Optional<Order> findActiveByTicketId(UUID ticketId) {
        return Optional.empty();
    }

    /**
     * @param ticketId идентификатор билета
     * @return список заказов
     */
    @Override
    public List<Order> findByTicketId(UUID ticketId) {
        return List.of();
    }

    /**
     * @param userId идентификатор пользователя
     * @return список заказов пользователя
     */
    @Override
    public List<Order> findByUserId(UUID userId) {
        return List.of();
    }

    /**
     * @param userId идентификатор пользователя
     * @param status статус заказа
     * @return список заказов пользователя
     */
    @Override
    public List<Order> findByUserIdAndStatus(UUID userId, OrderStatusEnum status) {
        return List.of();
    }

    /**
     * @param now время
     * @return список заказов
     */
    @Override
    public List<Order> findExpiredWaiting(LocalDateTime now) {
        return List.of();
    }

    /**
     * @param filter фильтр
     * @param page страница
     * @return результат поиск
     */
    @Override
    public PageResult<Order> findAll(OrdersFilter filter, PageRequest page) {
        return null;
    }
}
