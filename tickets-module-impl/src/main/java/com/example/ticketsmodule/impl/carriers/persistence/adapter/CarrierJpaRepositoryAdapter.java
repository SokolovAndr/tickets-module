package com.example.ticketsmodule.impl.carriers.persistence.adapter;

import com.example.ticketsmodule.domain.carrier.model.Carrier;
import com.example.ticketsmodule.domain.carrier.model.CarrierFilter;
import com.example.ticketsmodule.domain.carrier.repository.CarriersRepositoryPort;
import com.example.ticketsmodule.domain.common.PageRequest;
import com.example.ticketsmodule.domain.common.PageResult;
import com.example.ticketsmodule.domain.common.SortDirection;
import com.example.ticketsmodule.impl.carriers.persistence.entity.CarrierJpaEntity;
import com.example.ticketsmodule.impl.carriers.persistence.mapper.CarrierPersistenceMapper;
import com.example.ticketsmodule.impl.carriers.persistence.repository.CarrierSpringDataRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CarrierJpaRepositoryAdapter implements CarriersRepositoryPort {

    private final CarrierSpringDataRepository springRepo;
    private final CarrierPersistenceMapper mapper;

    @Override
    @Transactional
    public Carrier save(Carrier carrier) {
        CarrierJpaEntity saved = springRepo.save(mapper.toEntity(carrier));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Carrier> findById(UUID id) {
        return springRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsByName(String name) {
        return springRepo.existsByName(name);
    }

    @Override
    public PageResult<Carrier> findAll(CarrierFilter filter, PageRequest pageReq) {
        Sort sort = buildSort(pageReq);
        var springPageable = org.springframework.data.domain.PageRequest.of(
                pageReq.getPage(),
                pageReq.getSize(),
                sort
        );

        Page<CarrierJpaEntity> springPage = springRepo.findAll(buildSpecification(filter), springPageable);

        return PageResult.<Carrier>builder()
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

    private Specification<CarrierJpaEntity> buildSpecification(CarrierFilter f) {
        return (root, query, cb) -> {
            final List<Predicate> predicates = new ArrayList<>();
            if (f != null) {
                if (StringUtils.hasText(f.getQuery())) {
                    var pattern = "%" + f.getQuery().toLowerCase() + "%";
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("name")), pattern),
                            cb.like(cb.lower(root.get("phone")), pattern)
                    ));
                }
                if (StringUtils.hasText(f.getName())) {
                    predicates.add(cb.like(cb.lower(root.get("name")), "%" + f.getName().toLowerCase() + "%"));
                }
                if (StringUtils.hasText(f.getPhone())) {
                    predicates.add(cb.like(cb.lower(root.get("phone")), "%" + f.getPhone().toLowerCase() + "%"));
                }
                if (f.getCreateDateFrom() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), f.getCreateDateFrom()));
                }
                if (f.getCreateDateTo() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), f.getCreateDateTo()));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}