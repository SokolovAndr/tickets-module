package com.example.ticketsmodule.impl.users.persistence.adapter;

import com.example.ticketsmodule.domain.user.model.User;
import com.example.ticketsmodule.domain.user.repository.UsersRepositoryPort;
import com.example.ticketsmodule.impl.users.persistence.mapper.UserPersistenceMapper;
import com.example.ticketsmodule.impl.users.persistence.repository.UserSpringDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserJpaRepositoryAdapter implements UsersRepositoryPort {

    private final UserSpringDataRepository springRepo;
    private final UserPersistenceMapper mapper;

    @Override
    @Transactional
    public User save(User user) {
        return mapper.toDomain(springRepo.save(mapper.toEntity(user)));
    }

    @Override
    public Optional<User> findById(UUID id) {
        return springRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByLogin(String login) {
        return springRepo.findByLogin(login).map(mapper::toDomain);
    }
}