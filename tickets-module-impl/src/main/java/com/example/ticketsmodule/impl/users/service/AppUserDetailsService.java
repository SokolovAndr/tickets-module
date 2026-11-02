package com.example.ticketsmodule.impl.users.service;

import com.example.ticketsmodule.domain.user.model.User;
import com.example.ticketsmodule.domain.user.repository.UsersRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Отвечает за загрузку пользователя из БД при входе через HTML-форму (form login).
 *
 * <p>Вызывается Spring Security во время аутентификации в UI-цепочке.
 * Для REST API (JWT) не используется.
 */

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UsersRepositoryPort usersRepositoryPort;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User user = usersRepositoryPort.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + login));

        return new org.springframework.security.core.userdetails.User(
                user.getLogin(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}