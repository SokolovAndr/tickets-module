package com.example.ticketsmodule.support;

import com.example.ticketsmodule.impl.carriers.persistence.repository.CarrierSpringDataRepository;
import com.example.ticketsmodule.impl.routes.persistence.repository.RouteSpringDataRepository;
import com.example.ticketsmodule.impl.tickets.persistence.repository.TicketSpringDataRepository;
import com.example.ticketsmodule.impl.users.persistence.repository.UserSpringDataRepository;
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

    @Autowired protected TicketSpringDataRepository ticketSpringDataRepository;
    @Autowired protected RouteSpringDataRepository routeSpringDataRepository;
    @Autowired protected CarrierSpringDataRepository carrierSpringDataRepository;
    @Autowired protected UserSpringDataRepository userSpringDataRepository;

    protected void cleanDatabase() {
        ticketSpringDataRepository.deleteAll();
        routeSpringDataRepository.deleteAll();
        carrierSpringDataRepository.deleteAll();
        userSpringDataRepository.deleteAll();
    }
}