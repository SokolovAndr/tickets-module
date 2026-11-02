package com.example.ticketsmodule.domain.carrier.repository;

import com.example.ticketsmodule.domain.carrier.model.Carrier;
import com.example.ticketsmodule.domain.carrier.model.CarrierFilter;
import com.example.ticketsmodule.domain.common.PageRequest;
import com.example.ticketsmodule.domain.common.PageResult;
import java.util.Optional;
import java.util.UUID;

public interface CarriersRepositoryPort {
    Carrier save(Carrier carrier);
    Optional<Carrier> findById(UUID id);
    boolean existsByName(String name);
    PageResult<Carrier> findAll(CarrierFilter filter, PageRequest page);
}