package com.example.ticketsmodule.impl.tickets.conversion;

import com.example.ticketsmodule.api.model.CreateTicketRequest;
import com.example.ticketsmodule.api.model.CreateTicketResponse;
import com.example.ticketsmodule.domain.route.model.Route;
import com.example.ticketsmodule.domain.ticket.model.Ticket;
import com.example.ticketsmodule.domain.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TicketMapper unit tests")
class TicketMapperTest {

    private TicketMapper mapper;

    private UUID ticketId;
    private UUID routeId;
    private UUID userId;
    private Route route;
    private User user;

    @BeforeEach
    void setUp() {
        mapper = new TicketMapperImpl();

        ticketId = UUID.randomUUID();
        routeId = UUID.randomUUID();
        userId = UUID.randomUUID();

        route = Route.builder().id(routeId).build();
        user = User.builder().id(userId).login("admin").build();
    }

    // ---------- toDomain() ----------

    @Nested
    @DisplayName("toDomain()")
    class ToDomainTests {

        @Test
        @DisplayName("should map seatNumber and price from request")
        void shouldMapSeatNumberAndPrice() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setSeatNumber(15);
            request.setPrice(BigDecimal.valueOf(250));

            Ticket ticket = mapper.toDomain(request);

            assertThat(ticket).isNotNull();
            assertThat(ticket.getSeatNumber()).isEqualTo(15);
            assertThat(ticket.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(250));
        }

        @Test
        @DisplayName("should ignore id, route, user, purchased, purchasedAt, timestamps")
        void shouldIgnoreSystemFields() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setSeatNumber(1);
            request.setPrice(BigDecimal.ONE);

            Ticket ticket = mapper.toDomain(request);

            assertThat(ticket.getId()).isNull();
            assertThat(ticket.getRoute()).isNull();
            assertThat(ticket.getUser()).isNull();
            assertThat(ticket.isPurchased()).isFalse();
            assertThat(ticket.getPurchasedAt()).isNull();
            assertThat(ticket.getCreatedAt()).isNull();
            assertThat(ticket.getUpdatedAt()).isNull();
        }

        @Test
        @DisplayName("should return null when source is null")
        void shouldReturnNullWhenSourceNull() {
            Ticket ticket = mapper.toDomain(null);
            assertThat(ticket).isNull();
        }
    }

    // ---------- toResponse() ----------

    @Nested
    @DisplayName("toResponse()")
    class ToResponseTests {

        @Test
        @DisplayName("should map all fields from domain")
        void shouldMapAllFields() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime purchasedAt = now.minusHours(1);
            LocalDateTime created = now.minusDays(1);

            Ticket ticket = Ticket.builder()
                    .id(ticketId)
                    .route(route)
                    .seatNumber(15)
                    .price(BigDecimal.valueOf(250))
                    .purchased(true)
                    .user(user)
                    .purchasedAt(purchasedAt)
                    .createdAt(created)
                    .updatedAt(now)
                    .build();

            CreateTicketResponse response = mapper.toResponse(ticket);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(ticketId);
            assertThat(response.getRouteId()).isEqualTo(routeId);
            assertThat(response.getSeatNumber()).isEqualTo(15);
            assertThat(response.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(250));
            assertThat(response.getIsPurchased()).isTrue();
            assertThat(response.getPurchasedById()).isEqualTo(userId);
            assertThat(response.getPurchasedAt()).isEqualTo(purchasedAt);
            assertThat(response.getCreatedAt()).isEqualTo(created);
            assertThat(response.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("should map route.id to routeId")
        void shouldMapRouteIdFromNestedRoute() {
            UUID customRouteId = UUID.randomUUID();
            Route customRoute = Route.builder().id(customRouteId).build();

            Ticket ticket = Ticket.builder()
                    .id(ticketId)
                    .route(customRoute)
                    .seatNumber(1)
                    .price(BigDecimal.ONE)
                    .purchased(false)
                    .build();

            CreateTicketResponse response = mapper.toResponse(ticket);

            assertThat(response.getRouteId()).isEqualTo(customRouteId);
        }

        @Test
        @DisplayName("should map user.id to purchasedById")
        void shouldMapPurchasedByIdFromNestedUser() {
            UUID customUserId = UUID.randomUUID();
            User customUser = User.builder().id(customUserId).build();

            Ticket ticket = Ticket.builder()
                    .id(ticketId)
                    .route(route)
                    .seatNumber(1)
                    .price(BigDecimal.ONE)
                    .purchased(true)
                    .user(customUser)
                    .build();

            CreateTicketResponse response = mapper.toResponse(ticket);

            assertThat(response.getPurchasedById()).isEqualTo(customUserId);
        }

        @Test
        @DisplayName("should set purchasedById=null when user is null")
        void shouldSetPurchasedByIdNull() {
            Ticket ticket = Ticket.builder()
                    .id(ticketId)
                    .route(route)
                    .seatNumber(1)
                    .price(BigDecimal.ONE)
                    .purchased(false)
                    .user(null)
                    .build();

            CreateTicketResponse response = mapper.toResponse(ticket);

            assertThat(response.getPurchasedById()).isNull();
        }

        @Test
        @DisplayName("should map purchased=true to isPurchased=true")
        void shouldMapIsPurchasedTrue() {
            Ticket ticket = Ticket.builder()
                    .id(ticketId)
                    .route(route)
                    .seatNumber(1)
                    .price(BigDecimal.ONE)
                    .purchased(true)
                    .user(user)
                    .build();

            CreateTicketResponse response = mapper.toResponse(ticket);

            assertThat(response.getIsPurchased()).isTrue();
        }

        @Test
        @DisplayName("should map purchased=false to isPurchased=false")
        void shouldMapIsPurchasedFalse() {
            Ticket ticket = Ticket.builder()
                    .id(ticketId)
                    .route(route)
                    .seatNumber(1)
                    .price(BigDecimal.ONE)
                    .purchased(false)
                    .build();

            CreateTicketResponse response = mapper.toResponse(ticket);

            assertThat(response.getIsPurchased()).isFalse();
        }

        @Test
        @DisplayName("should return null when source is null")
        void shouldReturnNullWhenSourceNull() {
            CreateTicketResponse response = mapper.toResponse(null);
            assertThat(response).isNull();
        }

        @Test
        @DisplayName("should preserve null timestamps")
        void shouldPreserveNullTimestamps() {
            Ticket ticket = Ticket.builder()
                    .id(ticketId)
                    .route(route)
                    .seatNumber(1)
                    .price(BigDecimal.ONE)
                    .purchased(false)
                    .purchasedAt(null)
                    .createdAt(null)
                    .updatedAt(null)
                    .build();

            CreateTicketResponse response = mapper.toResponse(ticket);

            assertThat(response.getPurchasedAt()).isNull();
            assertThat(response.getCreatedAt()).isNull();
            assertThat(response.getUpdatedAt()).isNull();
        }

        @Test
        @DisplayName("should preserve null id")
        void shouldPreserveNullId() {
            Ticket ticket = Ticket.builder()
                    .route(route)
                    .seatNumber(1)
                    .price(BigDecimal.ONE)
                    .purchased(false)
                    .build();

            CreateTicketResponse response = mapper.toResponse(ticket);

            assertThat(response.getId()).isNull();
        }
    }

    // ---------- round-trip ----------

    @Nested
    @DisplayName("round-trip")
    class RoundTripTests {

        @Test
        @DisplayName("request -> domain -> response should preserve main fields")
        void shouldPreserveFieldsOnRoundTrip() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setSeatNumber(15);
            request.setPrice(BigDecimal.valueOf(250));

            Ticket ticket = mapper.toDomain(request);
            ticket.setId(ticketId);
            ticket.setRoute(route);
            ticket.setCreatedAt(LocalDateTime.now());
            ticket.setUpdatedAt(LocalDateTime.now());

            CreateTicketResponse response = mapper.toResponse(ticket);

            assertThat(response.getSeatNumber()).isEqualTo(15);
            assertThat(response.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(250));
            assertThat(response.getRouteId()).isEqualTo(routeId);
            assertThat(response.getIsPurchased()).isFalse();
        }
    }
}