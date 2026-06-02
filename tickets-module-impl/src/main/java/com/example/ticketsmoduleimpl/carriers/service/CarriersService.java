package com.example.ticketsmoduleimpl.carriers.service;

import com.example.ticketsmoduleimpl.carriers.conversion.CarrierFromEntityConverter;
import com.example.ticketsmoduleimpl.carriers.conversion.CarrierToEntityConverter;
import com.example.ticketsmoduleimpl.carriers.domain.CarrierEntity;
import com.example.ticketsmoduleimpl.carriers.repository.CarriersRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tickets.model.CarrierPatchRequest;
import tickets.model.CarriersSearchRequest;
import tickets.model.CreateCarrierRequest;
import tickets.model.CreateCarrierResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Сервис для работы с компаниями перевозчиками
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class CarriersService {

    private final CarriersRepository carriersRepository;
    private final CarrierToEntityConverter toEntityConverter;
    private final CarrierFromEntityConverter fromEntityConverter;

    @Transactional
    public CreateCarrierResponse create(CreateCarrierRequest createCarrierRequest) {
        final CarrierEntity carrierEntity = toEntityConverter.convert(createCarrierRequest);
        assert carrierEntity != null;
        CarrierEntity response = carriersRepository.save(carrierEntity);
        return fromEntityConverter.convert(response);
    }

    @Transactional(readOnly = true)
    public CreateCarrierResponse findOne(UUID id) {
        CarrierEntity response = carriersRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Carrier with id " + id + " not found"));
        return fromEntityConverter.convert(response);
    }

    @Transactional(readOnly = true)
    public Page<CreateCarrierResponse> findAll(CarriersSearchRequest searchParam, Pageable pageable) {
        Specification<CarrierEntity> spec = buildSpecification(searchParam);
        return carriersRepository.findAll(spec, pageable).map(fromEntityConverter::convert);
    }

    private Specification<CarrierEntity> buildSpecification(CarriersSearchRequest searchParam) {
        return (root, query, cb) -> {
            final List<Predicate> predicates = new ArrayList<>();

            query.orderBy(cb.desc(root.get("createdAt")));

            if (searchParam != null) {
                if (StringUtils.hasText(searchParam.getName())) {
                    var pattern = "%" + searchParam.getName().toLowerCase() + "%";
                    predicates.add(cb.like(cb.lower(root.get("name")), pattern));
                }

                if (StringUtils.hasText(searchParam.getPhone())) {
                    var pattern = "%" + searchParam.getPhone().toLowerCase() + "%";
                    predicates.add(cb.like(cb.lower(root.get("phone")), pattern));
                }


                if (searchParam.getCreateDateFrom() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), searchParam.getCreateDateFrom()));
                }

                if (searchParam.getCreateDateTo() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), searchParam.getCreateDateTo()));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public CreateCarrierResponse patch(@NotNull UUID id, @Valid CarrierPatchRequest carrierPatchRequest) {

        var result = carriersRepository.findById(id).map(entity -> {
            if (carrierPatchRequest.getName() != null) {
                entity.setName(carrierPatchRequest.getName());
            }
            if (carrierPatchRequest.getPhone() != null) {
                entity.setPhone(carrierPatchRequest.getPhone());
            }
            entity.setUpdatedAt(LocalDateTime.now());

            return carriersRepository.save(entity);
        }).orElseThrow(() -> new EntityNotFoundException(String.format("Carrier with id %s not found", carrierPatchRequest)));

        return fromEntityConverter.convert(result);
    }
}
