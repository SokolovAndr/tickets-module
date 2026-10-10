package com.example.ticketsmodule.support;

import com.example.ticketsmodule.impl.carriers.repository.CarriersRepository;
import com.example.ticketsmodule.impl.routes.repository.RoutesRepository;
import com.example.ticketsmodule.impl.tickets.repository.TicketRepository;
import com.example.ticketsmodule.impl.users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Базовый класс для интеграционных тестов.
 *
 * <p>Поднимает настоящий PostgreSQL в контейнере Docker.
 * Все {@code *IT} тесты наследуются от него.
 *
 * <p>Контейнер создаётся один раз на весь набор тестов.
 * Spring Boot кэширует контекст. Скорость не страдает.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:13");

    static {
        POSTGRES.start();
        Runtime.getRuntime().addShutdownHook(new Thread(POSTGRES::stop));
    }

    @Autowired protected TicketRepository ticketRepository;
    @Autowired protected RoutesRepository routesRepository;
    @Autowired protected CarriersRepository carriersRepository;
    @Autowired protected UserRepository userRepository;

    protected void cleanDatabase() {
        ticketRepository.deleteAll();
        routesRepository.deleteAll();
        carriersRepository.deleteAll();
        userRepository.deleteAll();
    }
}