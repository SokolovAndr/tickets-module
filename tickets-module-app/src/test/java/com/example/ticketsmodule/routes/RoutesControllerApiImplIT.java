package com.example.ticketsmodule.routes;

import com.example.ticketsmodule.api.model.CreateRouteRequest;
import com.example.ticketsmodule.api.model.RoutePatchRequest;
import com.example.ticketsmodule.impl.carriers.domain.CarrierEntity;
import com.example.ticketsmodule.impl.carriers.repository.CarriersRepository;
import com.example.ticketsmodule.impl.routes.domain.RouteEntity;
import com.example.ticketsmodule.impl.routes.repository.RoutesRepository;
import com.example.ticketsmodule.support.JwtTestHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("RoutesControllerApiImpl integration tests")
class RoutesControllerApiImplIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private RoutesRepository routesRepository;
    @Autowired private CarriersRepository carriersRepository;

    private UUID carrierId;

    @BeforeEach
    void cleanDb() {
        routesRepository.deleteAll();
        carriersRepository.deleteAll();
        carrierId = createCarrierInDb();
    }

    // ---------- Security ----------

    @Nested
    @DisplayName("security")
    class SecurityTests {

        @Test
        @DisplayName("GET /api/routes without auth → 401")
        void shouldReturn401WithoutAuth() throws Exception {
            mockMvc.perform(get("/api/routes"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/routes with USER role → 200")
        void shouldAllowUserRole() throws Exception {
            mockMvc.perform(get("/api/routes")
                            .with(JwtTestHelper.withRole("USER")))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST /api/routes with USER role → 403 (only ADMIN)")
        void shouldForbidUserToCreateRoute() throws Exception {
            CreateRouteRequest request = validRouteRequest();

            mockMvc.perform(post("/api/routes")
                            .with(JwtTestHelper.withRole("USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    // ---------- CREATE ----------

    @Nested
    @DisplayName("POST /api/routes")
    class CreateTests {

        @Test
        @DisplayName("should create route and return 201")
        void shouldCreateRoute() throws Exception {
            CreateRouteRequest request = validRouteRequest();

            mockMvc.perform(post("/api/routes")
                            .with(JwtTestHelper.withRole("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.departurePoint").value("Moscow"))
                    .andExpect(jsonPath("$.destinationPoint").value("SPb"))
                    .andExpect(jsonPath("$.carrierId").value(carrierId.toString()))
                    .andExpect(jsonPath("$.durationMinutes").value(120))
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.updatedAt").exists());

            assertThat(routesRepository.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("should return 404 when carrier not found")
        void shouldReturn404WhenCarrierNotFound() throws Exception {
            CreateRouteRequest request = validRouteRequest();
            request.setCarrierId(UUID.randomUUID());

            mockMvc.perform(post("/api/routes")
                            .with(JwtTestHelper.withRole("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());

            assertThat(routesRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("should return 400 when departurePoint is blank")
        void shouldReturn400WhenDeparturePointBlank() throws Exception {
            CreateRouteRequest request = validRouteRequest();
            request.setDeparturePoint("");

            mockMvc.perform(post("/api/routes")
                            .with(JwtTestHelper.withRole("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ---------- READ ----------

    @Nested
    @DisplayName("GET /api/routes/{id}")
    class ReadTests {

        @Test
        @DisplayName("should return 200 with route")
        void shouldReturnRoute() throws Exception {
            RouteEntity saved = createRouteInDb();

            mockMvc.perform(get("/api/routes/{id}", saved.getId())
                            .with(JwtTestHelper.withRole("USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(saved.getId().toString()))
                    .andExpect(jsonPath("$.departurePoint").value("Moscow"))
                    .andExpect(jsonPath("$.destinationPoint").value("SPb"))
                    .andExpect(jsonPath("$.carrierId").value(carrierId.toString()));
        }

        @Test
        @DisplayName("should return 404 when route not found")
        void shouldReturn404WhenNotFound() throws Exception {
            mockMvc.perform(get("/api/routes/{id}", UUID.randomUUID())
                            .with(JwtTestHelper.withRole("USER")))
                    .andExpect(status().isNotFound());
        }
    }

    // ---------- LIST ----------

    @Nested
    @DisplayName("GET /api/routes")
    class ListTests {

        @Test
        @DisplayName("should return 200 with empty list")
        void shouldReturnEmptyList() throws Exception {
            mockMvc.perform(get("/api/routes")
                            .with(JwtTestHelper.withRole("USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)));
        }

        @Test
        @DisplayName("should return 200 with list of routes")
        void shouldReturnList() throws Exception {
            createRouteInDb();
            createRouteInDb();

            mockMvc.perform(get("/api/routes")
                            .with(JwtTestHelper.withRole("USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)));
        }

        @Test
        @DisplayName("should filter by departurePoint")
        void shouldFilterByDeparturePoint() throws Exception {
            createRouteInDb();
            createRouteInDb();

            mockMvc.perform(get("/api/routes")
                            .param("departurePoint", "moscow")
                            .with(JwtTestHelper.withRole("USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)));
        }
    }

    // ---------- PATCH ----------

    @Nested
    @DisplayName("PATCH /api/routes/{id}")
    class PatchTests {

        @Test
        @DisplayName("should patch departurePoint and return 200")
        void shouldPatchDeparturePoint() throws Exception {
            RouteEntity saved = createRouteInDb();

            RoutePatchRequest patch = new RoutePatchRequest();
            patch.setDeparturePoint("Kazan");

            mockMvc.perform(patch("/api/routes/{id}", saved.getId())
                            .with(JwtTestHelper.withRole("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patch)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.departurePoint").value("Kazan"))
                    .andExpect(jsonPath("$.destinationPoint").value("SPb"));

            RouteEntity updated = routesRepository.findById(saved.getId()).orElseThrow();
            assertThat(updated.getDeparturePoint()).isEqualTo("Kazan");
        }

        @Test
        @DisplayName("should return 404 when route not found")
        void shouldReturn404WhenNotFound() throws Exception {
            RoutePatchRequest patch = new RoutePatchRequest();
            patch.setDeparturePoint("Kazan");

            mockMvc.perform(patch("/api/routes/{id}", UUID.randomUUID())
                            .with(JwtTestHelper.withRole("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patch)))
                    .andExpect(status().isNotFound());
        }
    }

    // ---------- helpers ----------

    private UUID createCarrierInDb() {
        CarrierEntity carrier = CarrierEntity.builder()
                .name("Acme-" + UUID.randomUUID())
                .phone("+7-999")
                .build();
        return carriersRepository.save(carrier).getId();
    }

    private RouteEntity createRouteInDb() {
        CarrierEntity carrier = carriersRepository.findById(carrierId).orElseThrow();
        RouteEntity route = RouteEntity.builder()
                .departurePoint("Moscow")
                .destinationPoint("SPb")
                .carrier(carrier)
                .durationMinutes(120)
                .departureAt(LocalDateTime.now().plusDays(1))
                .destinationAt(LocalDateTime.now().plusDays(1).plusHours(2))
                .build();
        return routesRepository.save(route);
    }

    private CreateRouteRequest validRouteRequest() {
        CreateRouteRequest request = new CreateRouteRequest();
        request.setDeparturePoint("Moscow");
        request.setDestinationPoint("SPb");
        request.setCarrierId(carrierId);
        request.setDurationMinutes(120);
        request.setDepartureAt(LocalDateTime.now().plusDays(1));
        request.setDestinationAt(LocalDateTime.now().plusDays(1).plusHours(2));
        return request;
    }
}