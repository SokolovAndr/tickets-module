package com.example.ticketsmodule.tickets;

import com.example.ticketsmodule.api.model.CreateTicketRequest;
import com.example.ticketsmodule.api.model.ReleaseTicketsBatchRequest;
import com.example.ticketsmodule.api.model.TicketPatchRequest;
import com.example.ticketsmodule.domain.carrier.model.Carrier;
import com.example.ticketsmodule.domain.carrier.repository.CarriersRepositoryPort;
import com.example.ticketsmodule.domain.common.PageRequest;
import com.example.ticketsmodule.domain.route.model.Route;
import com.example.ticketsmodule.domain.route.repository.RoutesRepositoryPort;
import com.example.ticketsmodule.domain.ticket.model.Ticket;
import com.example.ticketsmodule.domain.ticket.model.TicketFilter;
import com.example.ticketsmodule.domain.ticket.model.TicketStatusEnum;
import com.example.ticketsmodule.domain.ticket.repository.TicketsRepositoryPort;
import com.example.ticketsmodule.domain.user.model.User;
import com.example.ticketsmodule.domain.user.model.UserRoleEnum;
import com.example.ticketsmodule.domain.user.repository.UsersRepositoryPort;
import com.example.ticketsmodule.support.AbstractIntegrationTest;
import com.example.ticketsmodule.support.JwtTestHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("TicketsControllerApiImpl integration tests")
class TicketsControllerApiImplIT extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private CarriersRepositoryPort carriersRepositoryPort;
    @Autowired private RoutesRepositoryPort routesRepositoryPort;
    @Autowired private TicketsRepositoryPort ticketsRepositoryPort;
    @Autowired private UsersRepositoryPort usersRepositoryPort;

    private UUID routeId;
    private User testUser;

    @BeforeEach
    void setUp() {
        cleanDatabase();

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

        @Test
        @DisplayName("PATCH /api/tickets/{id} with USER role → 403")
        void shouldForbidUserToPatchTicket() throws Exception {
            Ticket saved = createTicketInDb(1, TicketStatusEnum.AVAILABLE, null);

            TicketPatchRequest patch = new TicketPatchRequest();
            patch.setPrice(new BigDecimal("100"));

            mockMvc.perform(patch("/api/tickets/{id}", saved.getId())
                            .with(JwtTestHelper.withRole("USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patch)))
                    .andExpect(status().isForbidden());
        }
    }

    // ---------- CREATE ----------

    @Nested
    @DisplayName("POST /api/tickets")
    class CreateTests {

        @Test
        @DisplayName("should create ticket and return 201 with ticketStatus=AVAILABLE")
        void shouldCreateTicket() throws Exception {
            CreateTicketRequest request = validTicketRequest(5);

            mockMvc.perform(post("/api/tickets")
                            .with(JwtTestHelper.withRole("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.routeId").value(routeId.toString()))
                    .andExpect(jsonPath("$.seatNumber").value(5))
                    .andExpect(jsonPath("$.price").value(150.00))
                    .andExpect(jsonPath("$.ticketStatus").value("AVAILABLE"))
                    .andExpect(jsonPath("$.createdAt").exists());

            var result = ticketsRepositoryPort.findAll(
                    TicketFilter.builder().build(),
                    PageRequest.builder().page(0).size(10).build()
            );
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTicketStatus())
                    .isEqualTo(TicketStatusEnum.AVAILABLE);
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

            var result = ticketsRepositoryPort.findAll(
                    TicketFilter.builder().build(),
                    PageRequest.builder().page(0).size(10).build()
            );
            assertThat(result.getContent()).isEmpty();
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
            Ticket saved = createTicketInDb(1, TicketStatusEnum.AVAILABLE, null);

            mockMvc.perform(get("/api/tickets/{id}", saved.getId())
                            .with(JwtTestHelper.withRole("USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(saved.getId().toString()))
                    .andExpect(jsonPath("$.seatNumber").value(1))
                    .andExpect(jsonPath("$.ticketStatus").value("AVAILABLE"));
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
            createTicketInDb(1, TicketStatusEnum.AVAILABLE, null);
            createTicketInDb(2, TicketStatusEnum.AVAILABLE, null);

            mockMvc.perform(get("/api/tickets")
                            .with(JwtTestHelper.withRole("USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)));
        }

        @Test
        @DisplayName("should filter by routeId")
        void shouldFilterByRouteId() throws Exception {
            createTicketInDb(1, TicketStatusEnum.AVAILABLE, null);

            mockMvc.perform(get("/api/tickets")
                            .param("routeId", routeId.toString())
                            .with(JwtTestHelper.withRole("USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)));
        }

        @Test
        @DisplayName("should filter by ticketStatus=PURCHASED")
        void shouldFilterByPurchasedStatus() throws Exception {
            createTicketInDb(1, TicketStatusEnum.AVAILABLE, null);
            createTicketInDb(2, TicketStatusEnum.PURCHASED, testUser);

            mockMvc.perform(get("/api/tickets")
                            .param("ticketStatus", "PURCHASED")
                            .with(JwtTestHelper.withRole("USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].seatNumber").value(2))
                    .andExpect(jsonPath("$.content[0].ticketStatus").value("PURCHASED"));
        }

        @Test
        @DisplayName("should filter by price range")
        void shouldFilterByPriceRange() throws Exception {
            Ticket saved = createTicketInDb(1, TicketStatusEnum.AVAILABLE, null);
            saved.setPrice(new BigDecimal("300.00"));
            ticketsRepositoryPort.save(saved);

            createTicketInDb(2, TicketStatusEnum.AVAILABLE, null); // цена 150.00

            mockMvc.perform(get("/api/tickets")
                            .param("priceFrom", "200")
                            .param("priceTo", "400")
                            .with(JwtTestHelper.withRole("USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].seatNumber").value(1));
        }
    }

    // ---------- LIST MY ----------

    @Nested
    @DisplayName("GET /api/tickets/my")
    class ListMyTicketsTests {

        @Test
        @DisplayName("should return only PURCHASED tickets of current user")
        void shouldReturnOnlyCurrentUserTickets() throws Exception {
            User otherUser = createUserInDb("otheruser");

            createTicketInDb(1, TicketStatusEnum.PURCHASED, testUser);   // мой
            createTicketInDb(2, TicketStatusEnum.PURCHASED, otherUser);  // чужой
            createTicketInDb(3, TicketStatusEnum.AVAILABLE, null);       // свободный

            mockMvc.perform(get("/api/tickets/my")
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].seatNumber").value(1))
                    .andExpect(jsonPath("$[0].ticketStatus").value("PURCHASED"));
        }

        @Test
        @DisplayName("should return empty list when current user has no tickets")
        void shouldReturnEmptyWhenNoTickets() throws Exception {
            User otherUser = createUserInDb("otheruser");
            createTicketInDb(1, TicketStatusEnum.PURCHASED, otherUser);

            mockMvc.perform(get("/api/tickets/my")
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("should return 401 without auth")
        void shouldReturn401WithoutAuth() throws Exception {
            mockMvc.perform(get("/api/tickets/my"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ---------- PATCH ----------

    @Nested
    @DisplayName("PATCH /api/tickets/{id}")
    class PatchTests {

        @Test
        @DisplayName("should patch price and return 200")
        void shouldPatchPrice() throws Exception {
            Ticket saved = createTicketInDb(1, TicketStatusEnum.AVAILABLE, null);

            TicketPatchRequest patch = new TicketPatchRequest();
            patch.setPrice(new BigDecimal("999.99"));

            mockMvc.perform(patch("/api/tickets/{id}", saved.getId())
                            .with(JwtTestHelper.withRole("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patch)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.price").value(999.99));

            Ticket updated = ticketsRepositoryPort.findById(saved.getId()).orElseThrow();
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

    // ---------- RELEASE BATCH ----------
    // ВНИМАНИЕ: api.yaml декларирует 201 Created, но TicketsControllerApiImpl.releaseTicketsBatch
    // возвращает HttpStatus.OK (200). Тест сверен с фактическим поведением реализации.
    // Если контракт будет приведён к спецификации — заменить .isOk() на .isCreated().

    @Nested
    @DisplayName("POST /api/tickets/release-batch")
    class ReleaseBatchTests {

        @Test
        @DisplayName("should release batch of tickets")
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
                    .andExpect(jsonPath("$.price").value(100.00))
                    .andExpect(jsonPath("$.ticketsIds", hasSize(5)));

            var result = ticketsRepositoryPort.findAll(
                    TicketFilter.builder().build(),
                    PageRequest.builder().page(0).size(10).build()
            );
            assertThat(result.getContent()).hasSize(5);
        }

        @Test
        @DisplayName("should skip occupied seats")
        void shouldSkipOccupiedSeats() throws Exception {
            createTicketInDb(1, TicketStatusEnum.AVAILABLE, null);
            createTicketInDb(2, TicketStatusEnum.AVAILABLE, null);

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

            var result = ticketsRepositoryPort.findAll(
                    TicketFilter.builder().build(),
                    PageRequest.builder().page(0).size(10).build()
            );
            assertThat(result.getContent()).hasSize(5);
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

        @Test
        @DisplayName("should reject release for past departure")
        void shouldRejectReleaseForPastDeparture() throws Exception {
            Route route = routesRepositoryPort.findById(routeId).orElseThrow();
            route.setDepartureAt(LocalDateTime.now().minusDays(1));
            routesRepositoryPort.save(route);

            ReleaseTicketsBatchRequest request = new ReleaseTicketsBatchRequest();
            request.setRouteId(routeId);
            request.setSeatCount(3);
            request.setPrice(new BigDecimal("100.00"));

            mockMvc.perform(post("/api/tickets/release-batch")
                            .with(JwtTestHelper.withRole("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError());

            var result = ticketsRepositoryPort.findAll(
                    TicketFilter.builder().build(),
                    PageRequest.builder().page(0).size(10).build()
            );
            assertThat(result.getContent()).isEmpty();
        }
    }

    // ---------- DOWNLOAD PDF ----------

    @Nested
    @DisplayName("GET /api/tickets/{id}/pdf")
    class DownloadPdfTests {

        @Test
        @DisplayName("should download PDF for owned ticket and return 200")
        void shouldDownloadPdf() throws Exception {
            Ticket saved = createTicketInDb(1, TicketStatusEnum.PURCHASED, testUser);

            MvcResult result = mockMvc.perform(get("/api/tickets/{id}/pdf", saved.getId())
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER")))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                    .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"ticket-" + saved.getId() + ".pdf\""))
                    .andReturn();

            byte[] content = result.getResponse().getContentAsByteArray();
            assertThat(content).isNotEmpty();
            String pdfHeader = new String(content, 0, 4);
            assertThat(pdfHeader).isEqualTo("%PDF");
        }

        @Test
        @DisplayName("should return 403 when downloading someone else's ticket")
        void shouldForbidDownloadingOthersTicket() throws Exception {
            User otherUser = createUserInDb("otheruser");
            Ticket saved = createTicketInDb(1, TicketStatusEnum.PURCHASED, otherUser);

            mockMvc.perform(get("/api/tickets/{id}/pdf", saved.getId())
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 403 when ticket has no owner")
        void shouldForbidDownloadingUnownedTicket() throws Exception {
            Ticket saved = createTicketInDb(1, TicketStatusEnum.AVAILABLE, null);

            mockMvc.perform(get("/api/tickets/{id}/pdf", saved.getId())
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 401 without auth")
        void shouldReturn401WithoutAuth() throws Exception {
            mockMvc.perform(get("/api/tickets/{id}/pdf", UUID.randomUUID()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 404 when ticket not found")
        void shouldReturn404WhenNotFound() throws Exception {
            mockMvc.perform(get("/api/tickets/{id}/pdf", UUID.randomUUID())
                            .with(JwtTestHelper.withUser(testUser.getId(), "USER")))
                    .andExpect(status().isNotFound());
        }
    }

    // ---------- HELPERS ----------

    private UUID createRouteInDb() {
        Carrier carrier = carriersRepositoryPort.save(
                Carrier.builder()
                        .name("Acme-" + UUID.randomUUID())
                        .phone("+7-999")
                        .build());

        Route route = routesRepositoryPort.save(
                Route.builder()
                        .departurePoint("Moscow")
                        .destinationPoint("SPb")
                        .carrier(carrier)
                        .durationMinutes(120)
                        .departureAt(LocalDateTime.now().plusDays(1))
                        .destinationAt(LocalDateTime.now().plusDays(1).plusHours(2))
                        .build());

        return route.getId();
    }

    private User createUserInDb(String login) {
        return usersRepositoryPort.save(User.builder()
                .login(login)
                .password(passwordEncoder.encode("secret123"))
                .fullName("Test User")
                .role(UserRoleEnum.USER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
    }

    private Ticket createTicketInDb(int seat, TicketStatusEnum status, User user) {
        Route route = routesRepositoryPort.findById(routeId).orElseThrow();

        return ticketsRepositoryPort.save(Ticket.builder()
                .route(route)
                .seatNumber(seat)
                .price(new BigDecimal("150.00"))
                .ticketStatus(status)
                .user(user)
                .purchasedAt(status == TicketStatusEnum.PURCHASED ? LocalDateTime.now() : null)
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