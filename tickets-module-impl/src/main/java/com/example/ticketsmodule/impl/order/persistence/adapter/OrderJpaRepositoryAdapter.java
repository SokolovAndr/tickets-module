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
import java.time.LocalTime;
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
    @Transactional
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
        return mapper.toDomainList(
                springRepo.findExpiredWaiting(OrderStatusEnum.WAITING, now));
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
            if (Long.class != query.getResultType() && long.class != query.getResultType()) {
                root.fetch("ticket", jakarta.persistence.criteria.JoinType.LEFT)
                        .fetch("route", jakarta.persistence.criteria.JoinType.LEFT)
                        .fetch("carrier", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("user", jakarta.persistence.criteria.JoinType.LEFT);
            }
            final List<Predicate> p = new ArrayList<>();
            if (f != null) {
                if (f.getTicketId() != null)
                    p.add(cb.equal(root.get("ticket").get("id"), f.getTicketId()));
                if (f.getUserId() != null)
                    p.add(cb.equal(root.get("user").get("id"), f.getUserId()));
                if (f.getStatus() != null)
                    p.add(cb.equal(root.get("status"), f.getStatus()));
                if (f.getCreatedDateFrom() != null)
                    p.add(cb.greaterThanOrEqualTo(root.get("createdAt"),
                            f.getCreatedDateFrom().atStartOfDay()));
                if (f.getCreatedDateTo() != null)
                    p.add(cb.lessThanOrEqualTo(root.get("createdAt"),
                            f.getCreatedDateTo().atTime(LocalTime.MAX)));
                if (f.getReservedUntilFrom() != null)
                    p.add(cb.greaterThanOrEqualTo(root.get("reservedUntil"),
                            f.getReservedUntilFrom().atStartOfDay()));
                if (f.getReservedUntilTo() != null)
                    p.add(cb.lessThanOrEqualTo(root.get("reservedUntil"),
                            f.getReservedUntilTo().atTime(LocalTime.MAX)));
                if (f.getPaidDateFrom() != null)
                    p.add(cb.greaterThanOrEqualTo(root.get("paidAt"),
                            f.getPaidDateFrom().atStartOfDay()));
                if (f.getPaidDateTo() != null)
                    p.add(cb.lessThanOrEqualTo(root.get("paidAt"),
                            f.getPaidDateTo().atTime(LocalTime.MAX)));
                if (f.getCanceledDateFrom() != null)
                    p.add(cb.greaterThanOrEqualTo(root.get("canceledAt"),
                            f.getCanceledDateFrom().atStartOfDay()));
                if (f.getCanceledDateTo() != null)
                    p.add(cb.lessThanOrEqualTo(root.get("canceledAt"),
                            f.getCanceledDateTo().atTime(LocalTime.MAX)));
                if (f.getRefundedDateFrom() != null)
                    p.add(cb.greaterThanOrEqualTo(root.get("refundedAt"),
                            f.getRefundedDateFrom().atStartOfDay()));
                if (f.getRefundedDateTo() != null)
                    p.add(cb.lessThanOrEqualTo(root.get("refundedAt"),
                            f.getRefundedDateTo().atTime(LocalTime.MAX)));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
    }
}
