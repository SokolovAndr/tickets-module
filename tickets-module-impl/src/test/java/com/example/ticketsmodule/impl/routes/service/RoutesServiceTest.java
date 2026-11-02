package com.example.ticketsmodule.impl.routes.service;

import com.example.ticketsmodule.api.model.CreateRouteRequest;
import com.example.ticketsmodule.api.model.CreateRouteResponse;
import com.example.ticketsmodule.api.model.RoutePatchRequest;
import com.example.ticketsmodule.api.model.RoutesSearchRequest;
import com.example.ticketsmodule.domain.carrier.exception.CarrierNotFoundException;
import com.example.ticketsmodule.domain.carrier.model.Carrier;
import com.example.ticketsmodule.domain.carrier.repository.CarriersRepositoryPort;
import com.example.ticketsmodule.domain.common.PageRequest;
import com.example.ticketsmodule.domain.common.PageResult;
import com.example.ticketsmodule.domain.route.exception.RouteNotFoundException;
import com.example.ticketsmodule.domain.route.model.Route;
import com.example.ticketsmodule.domain.route.repository.RoutesRepositoryPort;
import com.example.ticketsmodule.impl.routes.conversion.RouteMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoutesService unit tests")
class RoutesServiceTest {

    @Mock private RoutesRepositoryPort routesRepositoryPort;
    @Mock private CarriersRepositoryPort carriersRepositoryPort;
    @Mock private RouteMapper routeMapper;

    @InjectMocks
    private RoutesService routesService;

    private UUID routeId;
    private UUID carrierId;
    private Route route;
    private Carrier carrier;
    private CreateRouteResponse expectedResponse;

    @BeforeEach
    void setUp() {
        routeId = UUID.randomUUID();
        carrierId = UUID.randomUUID();

        carrier = Carrier.builder().id(carrierId).build();

        route = Route.builder()
                .id(routeId)
                .carrier(carrier)
                .departurePoint("Moscow")
                .destinationPoint("SPb")
                .departureAt(LocalDateTime.now().plusDays(1))
                .destinationAt(LocalDateTime.now().plusDays(1).plusHours(2))
                .durationMinutes(120)
                .build();

        expectedResponse = new CreateRouteResponse();
    }

    // ---------- create() ----------

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("should save route and return response")
        void shouldSaveRouteAndReturnResponse() {
            CreateRouteRequest request = new CreateRouteRequest();
            request.setCarrierId(carrierId);

            when(carriersRepositoryPort.findById(carrierId)).thenReturn(Optional.of(carrier));
            when(routeMapper.toDomain(request)).thenReturn(route);
            when(routesRepositoryPort.save(route)).thenReturn(route);
            when(routeMapper.toResponse(route)).thenReturn(expectedResponse);

            CreateRouteResponse actual = routesService.create(request);

            assertThat(actual).isSameAs(expectedResponse);
            verify(carriersRepositoryPort).findById(carrierId);
            verify(routeMapper).toDomain(request);
            verify(routesRepositoryPort).save(route);
        }

        @Test
        @DisplayName("should throw CarrierNotFoundException if carrier not found")
        void shouldThrowWhenCarrierNotFound() {
            CreateRouteRequest request = new CreateRouteRequest();
            request.setCarrierId(carrierId);

            when(carriersRepositoryPort.findById(carrierId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> routesService.create(request))
                    .isInstanceOf(CarrierNotFoundException.class);

            verifyNoInteractions(routesRepositoryPort, routeMapper);
        }
    }

    // ---------- findAll() ----------

    @Nested
    @DisplayName("findAll()")
    class FindAllTests {

        @Test
        @DisplayName("should return mapped PageResult")
        void shouldReturnMappedPageResult() {
            RoutesSearchRequest search = new RoutesSearchRequest();
            PageRequest pageReq = PageRequest.builder().page(0).size(10).build();

            PageResult<Route> domainPage = PageResult.<Route>builder()
                    .content(List.of(route))
                    .totalElements(1)
                    .build();

            when(routesRepositoryPort.findAll(any(), eq(pageReq))).thenReturn(domainPage);
            when(routeMapper.toResponse(route)).thenReturn(expectedResponse);

            PageResult<CreateRouteResponse> actual = routesService.findAll(search, pageReq);

            assertThat(actual.getContent()).hasSize(1);
            assertThat(actual.getContent().get(0)).isSameAs(expectedResponse);
            assertThat(actual.getTotalElements()).isEqualTo(1);
        }
    }

    // ---------- findOne() / findOneById() ----------

    @Nested
    @DisplayName("find methods")
    class FindOneTests {

        @Test
        @DisplayName("should return response when route exists")
        void shouldReturnResponseWhenFound() {
            when(routesRepositoryPort.findById(routeId)).thenReturn(Optional.of(route));
            when(routeMapper.toResponse(route)).thenReturn(expectedResponse);

            CreateRouteResponse actual = routesService.findOne(routeId);

            assertThat(actual).isSameAs(expectedResponse);
        }

        @Test
        @DisplayName("should throw RouteNotFoundException when route does not exist")
        void shouldThrowWhenNotFound() {
            when(routesRepositoryPort.findById(routeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> routesService.findOne(routeId))
                    .isInstanceOf(RouteNotFoundException.class);

            assertThatThrownBy(() -> routesService.findOneById(routeId))
                    .isInstanceOf(RouteNotFoundException.class);

            verifyNoInteractions(routeMapper);
        }
    }

    // ---------- patch() ----------

    @Nested
    @DisplayName("patch()")
    class PatchTests {

        @Test
        @DisplayName("should update only provided fields")
        void shouldUpdateOnlyProvidedFields() {
            RoutePatchRequest patch = new RoutePatchRequest();
            patch.setDeparturePoint("Kazan");
            patch.setDestinationPoint("Sochi");

            when(routesRepositoryPort.findById(routeId)).thenReturn(Optional.of(route));
            when(routesRepositoryPort.save(any(Route.class))).thenAnswer(inv -> inv.getArgument(0));
            when(routeMapper.toResponse(any(Route.class))).thenReturn(expectedResponse);

            CreateRouteResponse actual = routesService.patch(routeId, patch);

            assertThat(actual).isSameAs(expectedResponse);
            assertThat(route.getDeparturePoint()).isEqualTo("Kazan");
            assertThat(route.getDestinationPoint()).isEqualTo("Sochi");
            assertThat(route.getUpdatedAt()).isNotNull();
            verify(carriersRepositoryPort, never()).findById(any());
        }

        @Test
        @DisplayName("should update carrier when carrierId provided")
        void shouldUpdateCarrierWhenProvided() {
            UUID newCarrierId = UUID.randomUUID();
            Carrier newCarrier = Carrier.builder().id(newCarrierId).build();

            RoutePatchRequest patch = new RoutePatchRequest();
            patch.setCarrierId(newCarrierId);

            when(routesRepositoryPort.findById(routeId)).thenReturn(Optional.of(route));
            when(carriersRepositoryPort.findById(newCarrierId)).thenReturn(Optional.of(newCarrier));
            when(routesRepositoryPort.save(any(Route.class))).thenAnswer(inv -> inv.getArgument(0));
            when(routeMapper.toResponse(any(Route.class))).thenReturn(expectedResponse);

            routesService.patch(routeId, patch);

            assertThat(route.getCarrier()).isSameAs(newCarrier);
            verify(carriersRepositoryPort).findById(newCarrierId);
        }

        @Test
        @DisplayName("should throw RouteNotFoundException when route does not exist")
        void shouldThrowWhenNotFound() {
            RoutePatchRequest patch = new RoutePatchRequest();
            when(routesRepositoryPort.findById(routeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> routesService.patch(routeId, patch))
                    .isInstanceOf(RouteNotFoundException.class);

            verify(routesRepositoryPort, never()).save(any());
        }
    }

    // ---------- findAllByIds() ----------

    @Nested
    @DisplayName("findAllByIds()")
    class FindAllByIdsTests {

        @Test
        @DisplayName("should return empty map for null or empty ids")
        void shouldReturnEmptyMapForNullOrEmpty() {
            assertThat(routesService.findAllByIds(null)).isEmpty();
            assertThat(routesService.findAllByIds(List.of())).isEmpty();
            verifyNoInteractions(routesRepositoryPort);
        }

        @Test
        @DisplayName("should return map by id")
        void shouldReturnMapById() {
            Route second = Route.builder().id(UUID.randomUUID()).build();

            when(routesRepositoryPort.findAllByIds(List.of(routeId, second.getId())))
                    .thenReturn(List.of(route, second));

            Map<UUID, Route> result = routesService.findAllByIds(List.of(routeId, second.getId()));

            assertThat(result)
                    .containsEntry(routeId, route)
                    .containsEntry(second.getId(), second);
        }
    }
}