package com.example.ticketsmodule.tickets;

import com.example.ticketsmodule.api.model.CreateTicketRequest;
import com.example.ticketsmodule.api.model.ReleaseTicketsBatchRequest;
import com.example.ticketsmodule.api.model.TicketPatchRequest;
import com.example.ticketsmodule.impl.carriers.domain.CarrierEntity;
import com.example.ticketsmodule.impl.carriers.repository.CarriersRepository;
import com.example.ticketsmodule.impl.routes.domain.RouteEntity;
import com.example.ticketsmodule.impl.routes.repository.RoutesRepository;
import com.example.ticketsmodule.impl.tickets.domain.TicketEntity;
import com.example.ticketsmodule.impl.tickets.repository.TicketRepository;
import com.example.ticketsmodule.impl.users.domain.UserEntity;
import com.example.ticketsmodule.impl.users.domain.UserRoleEnum;
import com.example.ticketsmodule.impl.users.repository.UserRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("TicketsControllerApiImpl integration tests")
class TicketsControllerApiImplIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TicketRepository ticketRepository;
    @Autowired private RoutesRepository routesRepository;
    @Autowired private CarriersRepository carriersRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private UUID routeId;
    private UserEntity testUser;

    @BeforeEach
    void cleanDb() {
        ticketRepository.deleteAll();
        routesRepository.deleteAll();
        carriersRepository.deleteAll();
        userRepository.deleteAll();

        routeId = createRouteInDb();
        testUser = createUserInDb("testuser");
    }

    // ---------- SECURITY ----------

    @Nested
    @DisplayName("security")
    class SecurityTests {

        @Test
        @DisplayName("GET /api/tickets without auth → 401")
        void shouldReturn401WithoutAuth() throws Exception {
            mockMvc.perform(get("/api/tickets"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/tickets with USER role → 200")
        void shouldAllowUserRole() throws Exception {
            mockMvc.perform(get("/api/tickets")
                            .with(JwtTestHelper.withRole("USER")))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST /api/tickets with USER role → 403 (only ADMIN)")
        void shouldForbidUserToCreateTicket() throws Exception {
            CreateTicketRequest request = validTicketRequest(1);

            mockMvc.perform(post("/api/tickets")
                            .with(JwtTestHelper.withRole("USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    // ---------- CREATE ----------

    @Nested
    @DisplayName("POST /api/tickets")
    class CreateTests {

        @Test
        @DisplayName("should create ticket and return 201")
        void shouldCreateTicket() throws Exception {
            CreateTicketRequest request = validTicketRequest(5);

            mockMvc.perform(post("/api/tickets")
                            .with(JwtTestHelper.withRole("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.routeId").value(routeId.toString()))
                    .andExpect(jsonPath("$.seatNumber").value(5))
                    .andExpect(jsonPath("$.isPurchased").value(false))
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.createdAt").exists());

            assertThat(ticketRepository.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("should return 404 when route not found")
        void shouldReturn404WhenRouteNotFound() throws Exception {
            CreateTicketRequest request = validTicketRequest(1);
            request.setRouteId(UUID.randomUUID());

            mockMvc.perform(post("/api/tickets")
                            .with(JwtTestHelper.withRole("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());

            assertThat(ticketRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("should return 400 when seatNumber is null")
        void shouldReturn400WhenSeatNumberNull() throws Exception {
            CreateTicketRequest request = validTicketRequest(1);
            request.setSeatNumber(null);

            mockMvc.perform(post("/api/tickets")
                            .with(JwtTestHelper.withRole("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ---------- READ ----------

    @Nested
    @DisplayName("GET /api/tickets/{id}")
    class ReadTests {

        @Test
        @DisplayName("should return 200 with ticket")
        void shouldReturnTicket() throws Exception {
            TicketEntity saved = createTicketInDb(1, false, null);

            mockMvc.perform(get("/api/tickets/{id}", saved.getId())
                            .with(JwtTestHelper.withRole("USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(saved.getId().toString()))
                    .andExpect(jsonPath("$.seatNumber").value(1))
                    .andExpect(jsonPath("$.isPurchased").value(false));
        }

        @Test
        @DisplayName("should return 404 when ticket not found")
        void shouldReturn404WhenNotFound() throws Exception {
            mockMvc.perform(get("/api/tickets/{id}", UUID.randomUUID())
                            .with(JwtTestHelper.withRole("USER")))
                    .andExpect(status().isNotFound());
        }
    }

    // ---------- LIST ----------

    @Nested
    @DisplayName("GET /api/tickets")
    class ListTests {

        @Test
        @DisplayName("should return 200 with empty list")
        void shouldReturnEmptyList() throws Exception {
            mockMvc.perform(get("/api/tickets")
                            .with(JwtTestHelper.withRole("USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)));
        }

        @Test
        @DisplayName("should return 200 with list of tickets")
        void shouldReturnList() throws Exception {
            createTicketInDb(1, false, null);
            createTicketInDb(2, false, null);

            mockMvc.perform(get("/api/tickets")
                            .with(JwtTestHelper.withRole("USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)));
        }

        @Test
        @DisplayName("should filter by routeId")
        void shouldFilterByRouteId() throws Exception {
            createTicketInDb(1, false, null);

            mockMvc.perform(get("/api/tickets")
                            .param("routeId", routeId.toString())
                            .with(JwtTestHelper.withRole("USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)));
        }

        @Test
        @DisplayName("should filter by isPurchased")
        void shouldFilterByIsPurchased() throws Exception {
            createTicketInDb(1, false, null);
            createTicketInDb(2, true, testUser);

            mockMvc.perform(get("/api/tickets")
                            .param("isPurchased", "true")
                            .with(JwtTestHelper.withRole("USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].seatNumber").value(2));
        }
    }

    // ---------- PATCH ----------

    @Nested
    @DisplayName("PATCH /api/tickets/{id}")
    class PatchTests {

        @Test
        @DisplayName("should patch price and return 200")
        void shouldPatchPrice() throws Exception {
            TicketEntity saved = createTicketInDb(1, false, null);

            TicketPatchRequest patch = new TicketPatchRequest();
            patch.setPrice(new BigDecimal("999.99"));

            mockMvc.perform(patch("/api/tickets/{id}", saved.getId())
                            .with(JwtTestHelper.withRole("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patch)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.price").value(999.99));

            TicketEntity updated = ticketRepository.findById(saved.getId()).orElseThrow();
            assertThat(updated.getPrice()).isEqualByComparingTo("999.99");
        }

        @Test
        @DisplayName("should return 404 when ticket not found")
        void shouldReturn404WhenNotFound() throws Exception {
            TicketPatchRequest patch = new TicketPatchRequest();
            patch.setPrice(new BigDecimal("100"));

            mockMvc.perform(patch("/api/tickets/{id}", UUID.randomUUID())
                            .with(JwtTestHelper.withRole("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patch)))
                    .andExpect(status().isNotFound());
        }
    }

    // ---------- BUY ----------

    @Nested
    @DisplayName("POST /api/tickets/{id}/buy")
    class BuyTests {

        @Test
        @DisplayName("should buy ticket and return 200")
        void shouldBuyTicket() throws Exception {
            TicketEntity saved = createTicketInDb(1, false, null);

            mockMvc.perform(post("/api/tickets/{id}/buy", saved.getId())
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isPurchased").value(true))
                    .andExpect(jsonPath("$.purchasedById").value(testUser.getId().toString()));

            TicketEntity updated = ticketRepository.findById(saved.getId()).orElseThrow();
            assertThat(updated.isPurchased()).isTrue();
            assertThat(updated.getUser().getId()).isEqualTo(testUser.getId());
        }

        @Test
        @DisplayName("should reject already purchased ticket")
        void shouldRejectAlreadyPurchased() throws Exception {
            TicketEntity saved = createTicketInDb(1, true, testUser);

            mockMvc.perform(post("/api/tickets/{id}/buy", saved.getId())
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER")))
                    .andExpect(status().is5xxServerError());
        }

        @Test
        @DisplayName("should return 404 when ticket not found")
        void shouldReturn404WhenNotFound() throws Exception {
            mockMvc.perform(post("/api/tickets/{id}/buy", UUID.randomUUID())
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER")))
                    .andExpect(status().isNotFound());
        }
    }

    // ---------- RETURN ----------

    @Nested
    @DisplayName("POST /api/tickets/{id}/return")
    class ReturnTests {

        @Test
        @DisplayName("should return ticket and return 200")
        void shouldReturnTicket() throws Exception {
            TicketEntity saved = createTicketInDb(1, true, testUser);

            mockMvc.perform(post("/api/tickets/{id}/return", saved.getId())
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isPurchased").value(false))
                    .andExpect(jsonPath("$.purchasedById").doesNotExist());

            TicketEntity updated = ticketRepository.findById(saved.getId()).orElseThrow();
            assertThat(updated.isPurchased()).isFalse();
            assertThat(updated.getUser()).isNull();
        }

        @Test
        @DisplayName("should reject return of not purchased ticket")
        void shouldRejectReturnNotPurchased() throws Exception {
            TicketEntity saved = createTicketInDb(1, false, null);

            mockMvc.perform(post("/api/tickets/{id}/return", saved.getId())
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER")))
                    .andExpect(status().is5xxServerError());
        }
    }

    // ---------- RELEASE BATCH ----------

    @Nested
    @DisplayName("POST /api/tickets/release-batch")
    class ReleaseBatchTests {

        @Test
        @DisplayName("should release batch of tickets and return 201")
        void shouldReleaseBatch() throws Exception {
            ReleaseTicketsBatchRequest request = new ReleaseTicketsBatchRequest();
            request.setRouteId(routeId);
            request.setSeatCount(5);
            request.setPrice(new BigDecimal("100.00"));

            mockMvc.perform(post("/api/tickets/release-batch")
                            .with(JwtTestHelper.withRole("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.createdCount").value(5))
                    .andExpect(jsonPath("$.routeId").value(routeId.toString()))
                    .andExpect(jsonPath("$.ticketsIds", hasSize(5)));

            assertThat(ticketRepository.findAll()).hasSize(5);
        }

        @Test
        @DisplayName("should skip occupied seats")
        void shouldSkipOccupiedSeats() throws Exception {
            createTicketInDb(1, false, null);
            createTicketInDb(2, false, null);

            ReleaseTicketsBatchRequest request = new ReleaseTicketsBatchRequest();
            request.setRouteId(routeId);
            request.setSeatCount(5);
            request.setPrice(new BigDecimal("100.00"));

            mockMvc.perform(post("/api/tickets/release-batch")
                            .with(JwtTestHelper.withRole("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.createdCount").value(3));

            assertThat(ticketRepository.findAll()).hasSize(5);
        }

        @Test
        @DisplayName("should return 404 when route not found")
        void shouldReturn404WhenRouteNotFound() throws Exception {
            ReleaseTicketsBatchRequest request = new ReleaseTicketsBatchRequest();
            request.setRouteId(UUID.randomUUID());
            request.setSeatCount(5);
            request.setPrice(new BigDecimal("100.00"));

            mockMvc.perform(post("/api/tickets/release-batch")
                            .with(JwtTestHelper.withRole("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    // ---------- HELPERS ----------

    private UUID createRouteInDb() {
        CarrierEntity carrier = carriersRepository.save(
                CarrierEntity.builder()
                        .name("Acme-" + UUID.randomUUID())
                        .phone("+7-999")
                        .build());

        RouteEntity route = routesRepository.save(
                RouteEntity.builder()
                        .departurePoint("Moscow")
                        .destinationPoint("SPb")
                        .carrier(carrier)
                        .durationMinutes(120)
                        .departureAt(LocalDateTime.now().plusDays(1))
                        .destinationAt(LocalDateTime.now().plusDays(1).plusHours(2))
                        .build());

        return route.getId();
    }

    private UserEntity createUserInDb(String login) {
        return userRepository.save(UserEntity.builder()
                .login(login)
                .password(passwordEncoder.encode("secret123"))
                .fullName("Test User")
                .role(UserRoleEnum.USER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
    }

    private TicketEntity createTicketInDb(int seat, boolean purchased, UserEntity user) {
        RouteEntity route = routesRepository.findById(routeId).orElseThrow();

        return ticketRepository.save(TicketEntity.builder()
                .route(route)
                .seatNumber(seat)
                .price(new BigDecimal("150.00"))
                .isPurchased(purchased)
                .user(user)
                .purchasedAt(purchased ? LocalDateTime.now() : null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
    }

    private CreateTicketRequest validTicketRequest(int seat) {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setRouteId(routeId);
        request.setSeatNumber(seat);
        request.setPrice(new BigDecimal("150.00"));
        return request;
    }
}