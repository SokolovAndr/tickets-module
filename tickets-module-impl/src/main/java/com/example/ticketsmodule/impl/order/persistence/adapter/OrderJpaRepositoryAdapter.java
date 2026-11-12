package com.example.ticketsmodule.impl.order.persistence.adapter;

import com.example.ticketsmodule.domain.order.model.Order;
import com.example.ticketsmodule.domain.order.model.OrderStatusEnum;
import com.example.ticketsmodule.domain.common.PageRequest;
import com.example.ticketsmodule.domain.common.PageResult;
import com.example.ticketsmodule.domain.common.SortDirection;
import com.example.ticketsmodule.domain.order.model.OrdersFilter;
import com.example.ticketsmodule.domain.order.repository.OrdersRepositoryPort;
import com.example.ticketsmodule.impl.order.persistence.entity.OrderJpaEntity;
import com.example.ticketsmodule.impl.order.persistence.mapper.OrderPersistenceMapper;
import com.example.ticketsmodule.impl.order.persistence.repository.OrderSpringDataRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderJpaRepositoryAdapter implements OrdersRepositoryPort {

    private final OrderSpringDataRepository springRepo;
    private final OrderPersistenceMapper mapper;

    private static final List<OrderStatusEnum> activeStatuses = List.of(OrderStatusEnum.WAITING, OrderStatusEnum.PAID);

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
        return springRepo.findActiveByTicketId(ticketId, activeStatuses).map(mapper::toDomain);
    }

    /**
     * @param ticketId идентификатор билета
     * @return список заказов
     */
    @Override
    public List<Order> findByTicketId(UUID ticketId) {
        return mapper.toDomainList(springRepo.findByTicketId(ticketId));
    }

    /**
     * @param userId идентификатор пользователя
     * @return список заказов пользователя
     */
    @Override
    public List<Order> findByUserId(UUID userId) {
        return mapper.toDomainList(springRepo.findByUserId(userId));
    }

    /**
     * @param userId идентификатор пользователя
     * @param status статус заказа
     * @return список заказов пользователя
     */
    @Override
    public List<Order> findByUserIdAndStatus(UUID userId, OrderStatusEnum status){
        return mapper.toDomainList(springRepo.findByUserIdAndStatus(userId, status));
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
     * @param pageReq страница
     * @return результат поиск
     */
    @Override
    public PageResult<Order> findAll(OrdersFilter filter, PageRequest pageReq) {

        Sort sort = buildSort(pageReq);
        var springPageable = org.springframework.data.domain.PageRequest.of(
                pageReq.getPage(),
                pageReq.getSize(),
                sort
        );

        Page<OrderJpaEntity> springPage = springRepo.findAll(buildSpecification(filter), springPageable);

        return PageResult.<Order>builder()
                .content(mapper.toDomainList(springPage.getContent()))
                .totalElements(springPage.getTotalElements())
                .totalPages(springPage.getTotalPages())
                .pageNumber(springPage.getNumber())
                .pageSize(springPage.getSize())
                .build();

    }

    private Sort buildSort(PageRequest pageReq) {
        String field = (pageReq.getSortField() == null || pageReq.getSortField().isBlank())
                ? "createdAt" : pageReq.getSortField();
        Sort.Direction dir = (pageReq.getDirection() == SortDirection.ASC)
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, field);
    }

    private Specification<OrderJpaEntity> buildSpecification(OrdersFilter f) {
        return (root, query, cb) -> {
            final List<Predicate> predicates = new ArrayList<>();
            if (f != null) {
               //TODO(): реализовать фильтры
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
