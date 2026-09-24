package com.example.ticketsmodule.impl.routes.conversion;

import com.example.ticketsmodule.api.model.CreateRouteResponse;
import com.example.ticketsmodule.impl.carriers.domain.CarrierEntity;
import com.example.ticketsmodule.impl.routes.domain.RouteEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("RouteFromEntityConverter unit tests")
class RouteFromEntityConverterTest {

    private RouteFromEntityConverter converter;

    private UUID routeId;
    private UUID carrierId;

    @BeforeEach
    void setUp() {
        converter = new RouteFromEntityConverter();

        routeId = UUID.randomUUID();
        carrierId = UUID.randomUUID();
    }

    // ---------- полное заполнение ----------

    @Nested
    @DisplayName("full entity")
    class FullEntityTests {

        @Test
        @DisplayName("should map all fields from entity to response")
        void shouldMapAllFields() {
            LocalDateTime now = LocalDateTime.now();
            CarrierEntity carrier = createCarrier(carrierId);

            RouteEntity entity = RouteEntity.builder()
                    .id(routeId)
                    .departurePoint("Moscow")
                    .destinationPoint("SPb")
                    .carrier(carrier)
                    .durationMinutes(120)
                    .departureAt(now.plusDays(1))
                    .destinationAt(now.plusDays(1).plusHours(2))
                    .createdAt(now.minusDays(1))
                    .updatedAt(now)
                    .build();

            CreateRouteResponse response = converter.convert(entity);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(routeId);
            assertThat(response.getDeparturePoint()).isEqualTo("Moscow");
            assertThat(response.getDestinationPoint()).isEqualTo("SPb");
            assertThat(response.getCarrierId()).isEqualTo(carrierId);
            assertThat(response.getDurationMinutes()).isEqualTo(120);
            assertThat(response.getDepartureAt()).isEqualTo(now.plusDays(1));
            assertThat(response.getDestinationAt()).isEqualTo(now.plusDays(1).plusHours(2));
            assertThat(response.getCreatedAt()).isEqualTo(now.minusDays(1));
            assertThat(response.getUpdatedAt()).isEqualTo(now);
        }
    }

    // ---------- edge cases ----------

    @Nested
    @DisplayName("edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should preserve null values for optional fields")
        void shouldPreserveNulls() {
            CarrierEntity carrier = createCarrier(carrierId);

            RouteEntity entity = RouteEntity.builder()
                    .id(routeId)
                    .departurePoint("Moscow")
                    .destinationPoint("SPb")
                    .carrier(carrier)
                    .durationMinutes(60)
                    .departureAt(LocalDateTime.now())
                    .destinationAt(LocalDateTime.now())
                    .createdAt(null)
                    .updatedAt(null)
                    .build();

            CreateRouteResponse response = converter.convert(entity);

            assertThat(response.getCreatedAt()).isNull();
            assertThat(response.getUpdatedAt()).isNull();
        }

        @Test
        @DisplayName("should map carrier id from nested entity")
        void shouldMapCarrierIdFromNestedEntity() {
            UUID customCarrierId = UUID.randomUUID();
            CarrierEntity carrier = createCarrier(customCarrierId);

            RouteEntity entity = RouteEntity.builder()
                    .id(routeId)
                    .departurePoint("A")
                    .destinationPoint("B")
                    .carrier(carrier)
                    .durationMinutes(30)
                    .departureAt(LocalDateTime.now())
                    .destinationAt(LocalDateTime.now())
                    .build();

            CreateRouteResponse response = converter.convert(entity);

            assertThat(response.getCarrierId()).isEqualTo(customCarrierId);
        }

        @Test
        @DisplayName("should throw NullPointerException when carrier is null")
        void shouldThrowWhenCarrierIsNull() {
            RouteEntity entity = RouteEntity.builder()
                    .id(routeId)
                    .departurePoint("A")
                    .destinationPoint("B")
                    .carrier(null)
                    .durationMinutes(30)
                    .departureAt(LocalDateTime.now())
                    .destinationAt(LocalDateTime.now())
                    .build();

            assertThatThrownBy(() -> converter.convert(entity))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ---------- helpers ----------

    private static CarrierEntity createCarrier(UUID id) {
        CarrierEntity carrier = new CarrierEntity();
        carrier.setId(id);
        return carrier;
    }
}