package com.example.ticketsmodule.impl.routes.conversion;

import com.example.ticketsmodule.api.model.CreateRouteRequest;
import com.example.ticketsmodule.api.model.CreateRouteResponse;
import com.example.ticketsmodule.domain.carrier.model.Carrier;
import com.example.ticketsmodule.domain.route.model.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RouteMapper unit tests")
class RouteMapperTest {

    private RouteMapper mapper;

    private UUID routeId;
    private UUID carrierId;
    private String carrierName;
    private Carrier carrier;

    @BeforeEach
    void setUp() {
        mapper = new RouteMapperImpl();

        routeId = UUID.randomUUID();
        carrierId = UUID.randomUUID();
        carrierName = "PonyExpress";

        carrier = Carrier.builder()
                .id(carrierId)
                .name(carrierName)
                .build();
    }

    // ---------- toDomain() ----------

    @Nested
    @DisplayName("toDomain()")
    class ToDomainTests {

        @Test
        @DisplayName("should map all fields from request")
        void shouldMapAllFields() {
            LocalDateTime departureAt = LocalDateTime.now().plusDays(1);
            LocalDateTime destinationAt = departureAt.plusHours(2);

            CreateRouteRequest request = new CreateRouteRequest();
            request.setDeparturePoint("Moscow");
            request.setDestinationPoint("SPb");
            request.setDurationMinutes(120);
            request.setDepartureAt(departureAt);
            request.setDestinationAt(destinationAt);

            Route route = mapper.toDomain(request);

            assertThat(route).isNotNull();
            assertThat(route.getDeparturePoint()).isEqualTo("Moscow");
            assertThat(route.getDestinationPoint()).isEqualTo("SPb");
            assertThat(route.getDurationMinutes()).isEqualTo(120);
            assertThat(route.getDepartureAt()).isEqualTo(departureAt);
            assertThat(route.getDestinationAt()).isEqualTo(destinationAt);
        }

        @Test
        @DisplayName("should ignore id and timestamps")
        void shouldIgnoreSystemFields() {
            CreateRouteRequest request = createMinimalRequest();

            Route route = mapper.toDomain(request);

            assertThat(route.getId()).isNull();
            assertThat(route.getCreatedAt()).isNull();
            assertThat(route.getUpdatedAt()).isNull();
        }

        @Test
        @DisplayName("should ignore carrier (set in service)")
        void shouldIgnoreCarrier() {
            CreateRouteRequest request = createMinimalRequest();

            Route route = mapper.toDomain(request);

            assertThat(route.getCarrier()).isNull();
        }

        @Test
        @DisplayName("should return null when source is null")
        void shouldReturnNullWhenSourceNull() {
            Route route = mapper.toDomain(null);
            assertThat(route).isNull();
        }
    }

    // ---------- toResponse() ----------

    @Nested
    @DisplayName("toResponse()")
    class ToResponseTests {

        @Test
        @DisplayName("should map all fields from domain")
        void shouldMapAllFields() {
            LocalDateTime departureAt = LocalDateTime.now().plusDays(1);
            LocalDateTime destinationAt = departureAt.plusHours(2);
            LocalDateTime created = LocalDateTime.now().minusDays(1);
            LocalDateTime updated = LocalDateTime.now();

            Route route = Route.builder()
                    .id(routeId)
                    .departurePoint("Moscow")
                    .destinationPoint("SPb")
                    .carrier(carrier)
                    .durationMinutes(120)
                    .departureAt(departureAt)
                    .destinationAt(destinationAt)
                    .createdAt(created)
                    .updatedAt(updated)
                    .build();

            CreateRouteResponse response = mapper.toResponse(route);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(routeId);
            assertThat(response.getDeparturePoint()).isEqualTo("Moscow");
            assertThat(response.getDestinationPoint()).isEqualTo("SPb");
            assertThat(response.getDurationMinutes()).isEqualTo(120);
            assertThat(response.getDepartureAt()).isEqualTo(departureAt);
            assertThat(response.getDestinationAt()).isEqualTo(destinationAt);
            assertThat(response.getCreatedAt()).isEqualTo(created);
            assertThat(response.getUpdatedAt()).isEqualTo(updated);
        }

        @Test
        @DisplayName("should map carrier.id and carrier.name")
        void shouldMapCarrierIdAndCarrierName() {
            Route route = Route.builder()
                    .id(routeId)
                    .carrier(carrier)
                    .build();

            CreateRouteResponse response = mapper.toResponse(route);

            assertThat(response.getCarrierId()).isEqualTo(carrierId);
            assertThat(response.getCarrierName()).isEqualTo(carrierName);
        }

        @Test
        @DisplayName("should return null when source is null")
        void shouldReturnNullWhenSourceNull() {
            CreateRouteResponse response = mapper.toResponse(null);
            assertThat(response).isNull();
        }
    }

    // ---------- helper ----------

    private static CreateRouteRequest createMinimalRequest() {
        CreateRouteRequest request = new CreateRouteRequest();
        request.setDeparturePoint("A");
        request.setDestinationPoint("B");
        request.setDurationMinutes(60);
        request.setDepartureAt(LocalDateTime.now());
        request.setDestinationAt(LocalDateTime.now());
        return request;
    }
}