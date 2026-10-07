package com.example.ticketsmodule.impl.tickets.conversion;

import com.example.ticketsmodule.api.model.CreateTicketRequest;
import com.example.ticketsmodule.api.model.CreateTicketResponse;
import com.example.ticketsmodule.impl.routes.domain.RouteEntity;
import com.example.ticketsmodule.impl.tickets.domain.TicketEntity;
import com.example.ticketsmodule.impl.users.domain.UserEntity;
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
    private RouteEntity route;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        mapper = new TicketMapperImpl();

        ticketId = UUID.randomUUID();
        routeId = UUID.randomUUID();
        userId = UUID.randomUUID();

        route = new RouteEntity();
        route.setId(routeId);

        user = new UserEntity();
        user.setId(userId);
        user.setLogin("admin");
    }

    // ---------- toEntity() ----------

    @Nested
    @DisplayName("toEntity()")
    class ToEntityTests {

        @Test
        @DisplayName("should map seatNumber and price from request")
        void shouldMapSeatNumberAndPrice() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setSeatNumber(15);
            request.setPrice(BigDecimal.valueOf(250));

            TicketEntity entity = mapper.toEntity(request, route);

            assertThat(entity).isNotNull();
            assertThat(entity.getSeatNumber()).isEqualTo(15);
            assertThat(entity.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(250));
        }

        @Test
        @DisplayName("should set route from argument")
        void shouldSetRouteFromArgument() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setSeatNumber(1);
            request.setPrice(BigDecimal.ONE);

            TicketEntity entity = mapper.toEntity(request, route);

            assertThat(entity.getRoute()).isSameAs(route);
            assertThat(entity.getRoute().getId()).isEqualTo(routeId);
        }

        @Test
        @DisplayName("should ignore id")
        void shouldIgnoreId() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setSeatNumber(1);
            request.setPrice(BigDecimal.ONE);

            TicketEntity entity = mapper.toEntity(request, route);

            assertThat(entity.getId()).isNull();
        }

        @Test
        @DisplayName("should ignore createdAt")
        void shouldIgnoreCreatedAt() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setSeatNumber(1);
            request.setPrice(BigDecimal.ONE);

            TicketEntity entity = mapper.toEntity(request, route);

            assertThat(entity.getCreatedAt()).isNull();
        }

        @Test
        @DisplayName("should ignore updatedAt")
        void shouldIgnoreUpdatedAt() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setSeatNumber(1);
            request.setPrice(BigDecimal.ONE);

            TicketEntity entity = mapper.toEntity(request, route);

            assertThat(entity.getUpdatedAt()).isNull();
        }

        @Test
        @DisplayName("should set isPurchased=false by default")
        void shouldSetIsPurchasedFalse() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setSeatNumber(1);
            request.setPrice(BigDecimal.ONE);

            TicketEntity entity = mapper.toEntity(request, route);

            assertThat(entity.isPurchased()).isFalse();
        }

        @Test
        @DisplayName("should set user=null by default")
        void shouldSetUserNull() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setSeatNumber(1);
            request.setPrice(BigDecimal.ONE);

            TicketEntity entity = mapper.toEntity(request, route);

            assertThat(entity.getUser()).isNull();
        }

        @Test
        @DisplayName("should set purchasedAt=null by default")
        void shouldSetPurchasedAtNull() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setSeatNumber(1);
            request.setPrice(BigDecimal.ONE);

            TicketEntity entity = mapper.toEntity(request, route);

            assertThat(entity.getPurchasedAt()).isNull();
        }

        @Test
        @DisplayName("should accept null route")
        void shouldAcceptNullRoute() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setSeatNumber(1);
            request.setPrice(BigDecimal.ONE);

            TicketEntity entity = mapper.toEntity(request, null);

            assertThat(entity).isNotNull();
            assertThat(entity.getRoute()).isNull();
        }

        @Test
        @DisplayName("should return null when both sources are null")
        void shouldReturnNullWhenBothSourcesNull() {
            TicketEntity entity = mapper.toEntity(null, null);

            assertThat(entity).isNull();
        }

        @Test
        @DisplayName("should set seatNumber=0 when request has null seatNumber")
        void shouldSetZeroWhenSeatNumberNull() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setSeatNumber(null);
            request.setPrice(BigDecimal.ONE);

            TicketEntity entity = mapper.toEntity(request, route);

            assertThat(entity.getSeatNumber()).isZero();
        }
    }

    // ---------- toResponse() ----------

    @Nested
    @DisplayName("toResponse()")
    class ToResponseTests {

        @Test
        @DisplayName("should map all fields from entity")
        void shouldMapAllFields() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime purchasedAt = now.minusHours(1);
            LocalDateTime created = now.minusDays(1);

            TicketEntity entity = TicketEntity.builder()
                    .id(ticketId)
                    .route(route)
                    .seatNumber(15)
                    .price(BigDecimal.valueOf(250))
                    .isPurchased(true)
                    .user(user)
                    .purchasedAt(purchasedAt)
                    .createdAt(created)
                    .updatedAt(now)
                    .build();

            CreateTicketResponse response = mapper.toResponse(entity);

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
            RouteEntity customRoute = new RouteEntity();
            customRoute.setId(customRouteId);

            TicketEntity entity = TicketEntity.builder()
                    .id(ticketId)
                    .route(customRoute)
                    .seatNumber(1)
                    .price(BigDecimal.ONE)
                    .isPurchased(false)
                    .build();

            CreateTicketResponse response = mapper.toResponse(entity);

            assertThat(response.getRouteId()).isEqualTo(customRouteId);
        }

        @Test
        @DisplayName("should map user.id to purchasedById")
        void shouldMapPurchasedByIdFromNestedUser() {
            UUID customUserId = UUID.randomUUID();
            UserEntity customUser = new UserEntity();
            customUser.setId(customUserId);

            TicketEntity entity = TicketEntity.builder()
                    .id(ticketId)
                    .route(route)
                    .seatNumber(1)
                    .price(BigDecimal.ONE)
                    .isPurchased(true)
                    .user(customUser)
                    .build();

            CreateTicketResponse response = mapper.toResponse(entity);

            assertThat(response.getPurchasedById()).isEqualTo(customUserId);
        }

        @Test
        @DisplayName("should set purchasedById=null when user is null")
        void shouldSetPurchasedByIdNull() {
            TicketEntity entity = TicketEntity.builder()
                    .id(ticketId)
                    .route(route)
                    .seatNumber(1)
                    .price(BigDecimal.ONE)
                    .isPurchased(false)
                    .user(null)
                    .build();

            CreateTicketResponse response = mapper.toResponse(entity);

            assertThat(response.getPurchasedById()).isNull();
        }

        @Test
        @DisplayName("should map isPurchased=true")
        void shouldMapIsPurchasedTrue() {
            TicketEntity entity = TicketEntity.builder()
                    .id(ticketId)
                    .route(route)
                    .seatNumber(1)
                    .price(BigDecimal.ONE)
                    .isPurchased(true)
                    .user(user)
                    .build();

            CreateTicketResponse response = mapper.toResponse(entity);

            assertThat(response.getIsPurchased()).isTrue();
        }

        @Test
        @DisplayName("should map isPurchased=false")
        void shouldMapIsPurchasedFalse() {
            TicketEntity entity = TicketEntity.builder()
                    .id(ticketId)
                    .route(route)
                    .seatNumber(1)
                    .price(BigDecimal.ONE)
                    .isPurchased(false)
                    .build();

            CreateTicketResponse response = mapper.toResponse(entity);

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
            TicketEntity entity = TicketEntity.builder()
                    .id(ticketId)
                    .route(route)
                    .seatNumber(1)
                    .price(BigDecimal.ONE)
                    .isPurchased(false)
                    .purchasedAt(null)
                    .createdAt(null)
                    .updatedAt(null)
                    .build();

            CreateTicketResponse response = mapper.toResponse(entity);

            assertThat(response.getPurchasedAt()).isNull();
            assertThat(response.getCreatedAt()).isNull();
            assertThat(response.getUpdatedAt()).isNull();
        }

        @Test
        @DisplayName("should preserve null id")
        void shouldPreserveNullId() {
            TicketEntity entity = TicketEntity.builder()
                    .route(route)
                    .seatNumber(1)
                    .price(BigDecimal.ONE)
                    .isPurchased(false)
                    .build();

            CreateTicketResponse response = mapper.toResponse(entity);

            assertThat(response.getId()).isNull();
        }
    }

    // ---------- round-trip ----------

    @Nested
    @DisplayName("round-trip")
    class RoundTripTests {

        @Test
        @DisplayName("request -> entity -> response should preserve main fields")
        void shouldPreserveFieldsOnRoundTrip() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setSeatNumber(15);
            request.setPrice(BigDecimal.valueOf(250));

            TicketEntity entity = mapper.toEntity(request, route);
            entity.setId(ticketId);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());

            CreateTicketResponse response = mapper.toResponse(entity);

            assertThat(response.getSeatNumber()).isEqualTo(15);
            assertThat(response.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(250));
            assertThat(response.getRouteId()).isEqualTo(routeId);
            assertThat(response.getIsPurchased()).isFalse();
        }
    }
}