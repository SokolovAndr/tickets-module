package com.example.ticketsmodule.impl.tickets.conversion;

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

@DisplayName("TicketFromEntityConverter unit tests")
class TicketFromEntityConverterTest {

    private TicketFromEntityConverter converter;

    private UUID ticketId;
    private UUID routeId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        converter = new TicketFromEntityConverter();

        ticketId = UUID.randomUUID();
        routeId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    // ---------- полное заполнение ----------

    @Nested
    @DisplayName("full entity")
    class FullEntityTests {

        @Test
        @DisplayName("should map all fields when user is present")
        void shouldMapAllFields() {
            LocalDateTime now = LocalDateTime.now();
            RouteEntity route = createRoute(routeId);
            UserEntity user = createUser(userId);

            TicketEntity entity = TicketEntity.builder()
                    .id(ticketId)
                    .route(route)
                    .seatNumber(15)
                    .price(BigDecimal.valueOf(250))
                    .isPurchased(true)
                    .user(user)
                    .purchasedAt(now.minusHours(1))
                    .createdAt(now.minusDays(1))
                    .updatedAt(now)
                    .build();

            CreateTicketResponse response = converter.convert(entity);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(ticketId);
            assertThat(response.getRouteId()).isEqualTo(routeId);
            assertThat(response.getSeatNumber()).isEqualTo(15);
            assertThat(response.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(250));
            assertThat(response.getIsPurchased()).isTrue();
            assertThat(response.getPurchasedById()).isEqualTo(userId);
            assertThat(response.getPurchasedAt()).isEqualTo(now.minusHours(1));
            assertThat(response.getCreatedAt()).isEqualTo(now.minusDays(1));
            assertThat(response.getUpdatedAt()).isEqualTo(now);
        }
    }

    // ---------- не купленный билет ----------

    @Nested
    @DisplayName("unpurchased ticket")
    class UnpurchasedTicketTests {

        @Test
        @DisplayName("should set purchasedById=null when user is null")
        void shouldSetPurchasedByIdNull() {
            RouteEntity route = createRoute(routeId);

            TicketEntity entity = TicketEntity.builder()
                    .id(ticketId)
                    .route(route)
                    .seatNumber(1)
                    .price(BigDecimal.TEN)
                    .isPurchased(false)
                    .user(null)
                    .purchasedAt(null)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            CreateTicketResponse response = converter.convert(entity);

            assertThat(response.getIsPurchased()).isFalse();
            assertThat(response.getPurchasedById()).isNull();
            assertThat(response.getPurchasedAt()).isNull();
        }
    }

    // ---------- edge cases ----------

    @Nested
    @DisplayName("edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should preserve null values for optional fields")
        void shouldPreserveNulls() {
            RouteEntity route = createRoute(routeId);

            TicketEntity entity = TicketEntity.builder()
                    .id(ticketId)
                    .route(route)
                    .seatNumber(5)
                    .price(BigDecimal.ONE)
                    .isPurchased(false)
                    .user(null)
                    .purchasedAt(null)
                    .createdAt(null)
                    .updatedAt(null)
                    .build();

            CreateTicketResponse response = converter.convert(entity);

            assertThat(response.getCreatedAt()).isNull();
            assertThat(response.getUpdatedAt()).isNull();
            assertThat(response.getPurchasedAt()).isNull();
        }

        @Test
        @DisplayName("should map route id from nested entity")
        void shouldMapRouteIdFromNestedEntity() {
            UUID customRouteId = UUID.randomUUID();
            RouteEntity route = createRoute(customRouteId);

            TicketEntity entity = TicketEntity.builder()
                    .id(ticketId)
                    .route(route)
                    .seatNumber(1)
                    .price(BigDecimal.ONE)
                    .isPurchased(false)
                    .build();

            CreateTicketResponse response = converter.convert(entity);

            assertThat(response.getRouteId()).isEqualTo(customRouteId);
        }
    }

    // ---------- helpers ----------

    private static RouteEntity createRoute(UUID id) {
        RouteEntity route = new RouteEntity();
        route.setId(id);
        return route;
    }

    private static UserEntity createUser(UUID id) {
        UserEntity user = new UserEntity();
        user.setId(id);
        return user;
    }
}