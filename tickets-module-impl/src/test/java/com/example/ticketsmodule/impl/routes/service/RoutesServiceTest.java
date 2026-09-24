package com.example.ticketsmodule.impl.routes.service;

import com.example.ticketsmodule.api.model.CreateRouteRequest;
import com.example.ticketsmodule.api.model.CreateRouteResponse;
import com.example.ticketsmodule.api.model.RoutePatchRequest;
import com.example.ticketsmodule.api.model.RoutesSearchRequest;
import com.example.ticketsmodule.impl.carriers.domain.CarrierEntity;
import com.example.ticketsmodule.impl.carriers.service.CarriersService;
import com.example.ticketsmodule.impl.routes.conversion.RouteFromEntityConverter;
import com.example.ticketsmodule.impl.routes.conversion.RouteToEntityConverter;
import com.example.ticketsmodule.impl.routes.domain.RouteEntity;
import com.example.ticketsmodule.impl.routes.repository.RoutesRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
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

    @Mock private RoutesRepository routesRepository;
    @Mock private RouteFromEntityConverter fromEntityConverter;
    @Mock private RouteToEntityConverter toEntityConverter;
    @Mock private CarriersService carriersService;

    @InjectMocks
    private RoutesService routesService;

    private UUID routeId;
    private UUID carrierId;
    private RouteEntity routeEntity;
    private CarrierEntity carrierEntity;
    private CreateRouteResponse expectedResponse;

    @BeforeEach
    void setUp() {
        routeId = UUID.randomUUID();
        carrierId = UUID.randomUUID();

        carrierEntity = new CarrierEntity();
        carrierEntity.setId(carrierId);

        routeEntity = new RouteEntity();
        routeEntity.setId(routeId);
        routeEntity.setCarrier(carrierEntity);
        routeEntity.setDeparturePoint("Moscow");
        routeEntity.setDestinationPoint("SPb");
        routeEntity.setDepartureAt(LocalDateTime.now().plusDays(1));
        routeEntity.setDestinationAt(LocalDateTime.now().plusDays(1).plusHours(2));
        routeEntity.setDurationMinutes(120);

        expectedResponse = new CreateRouteResponse();
    }

    // ---------- create() ----------

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("should save route and return converted response")
        void shouldSaveRouteAndReturnResponse() {
            CreateRouteRequest request = new CreateRouteRequest();
            request.setCarrierId(carrierId);

            when(carriersService.findOneById(carrierId)).thenReturn(carrierEntity);
            when(toEntityConverter.convert(request, carrierEntity)).thenReturn(routeEntity);
            when(routesRepository.save(routeEntity)).thenReturn(routeEntity);
            when(fromEntityConverter.convert(routeEntity)).thenReturn(expectedResponse);

            CreateRouteResponse actual = routesService.create(request);

            assertThat(actual).isSameAs(expectedResponse);
            verify(carriersService).findOneById(carrierId);
            verify(toEntityConverter).convert(request, carrierEntity);
            verify(routesRepository).save(routeEntity);
            verify(fromEntityConverter).convert(routeEntity);
        }

        @Test
        @DisplayName("should propagate exception when carrier not found")
        void shouldPropagateExceptionWhenCarrierNotFound() {
            CreateRouteRequest request = new CreateRouteRequest();
            request.setCarrierId(carrierId);

            when(carriersService.findOneById(carrierId))
                    .thenThrow(new EntityNotFoundException("Carrier not found"));

            assertThatThrownBy(() -> routesService.create(request))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Carrier not found");

            verifyNoInteractions(routesRepository, toEntityConverter, fromEntityConverter);
        }
    }

    // ---------- findAll() ----------

    @Nested
    @DisplayName("findAll()")
    class FindAllTests {

        @Test
        @DisplayName("should return mapped page of responses")
        void shouldReturnMappedPage() {
            RoutesSearchRequest search = new RoutesSearchRequest();
            Pageable pageable = PageRequest.of(0, 10);

            Page<RouteEntity> entityPage = new PageImpl<>(List.of(routeEntity));
            when(routesRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(entityPage);
            when(fromEntityConverter.convert(routeEntity)).thenReturn(expectedResponse);

            Page<CreateRouteResponse> actual = routesService.findAll(search, pageable);

            assertThat(actual).hasSize(1);
            assertThat(actual.getContent()).containsExactly(expectedResponse);
            verify(routesRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("should return empty page when no routes")
        void shouldReturnEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            when(routesRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(Page.empty());

            Page<CreateRouteResponse> actual = routesService.findAll(null, pageable);

            assertThat(actual).isEmpty();
            verifyNoInteractions(fromEntityConverter);
        }
    }

    // ---------- findOne() ----------

    @Nested
    @DisplayName("findOne()")
    class FindOneTests {

        @Test
        @DisplayName("should return response when route exists")
        void shouldReturnResponseWhenFound() {
            when(routesRepository.findById(routeId)).thenReturn(Optional.of(routeEntity));
            when(fromEntityConverter.convert(routeEntity)).thenReturn(expectedResponse);

            CreateRouteResponse actual = routesService.findOne(routeId);

            assertThat(actual).isSameAs(expectedResponse);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when route does not exist")
        void shouldThrowWhenNotFound() {
            when(routesRepository.findById(routeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> routesService.findOne(routeId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(routeId.toString());

            verifyNoInteractions(fromEntityConverter);
        }
    }

    // ---------- findOneById() ----------

    @Nested
    @DisplayName("findOneById()")
    class FindOneByIdTests {

        @Test
        @DisplayName("should return entity when route exists")
        void shouldReturnEntityWhenFound() {
            when(routesRepository.findById(routeId)).thenReturn(Optional.of(routeEntity));

            RouteEntity actual = routesService.findOneById(routeId);

            assertThat(actual).isSameAs(routeEntity);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when route does not exist")
        void shouldThrowWhenNotFound() {
            when(routesRepository.findById(routeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> routesService.findOneById(routeId))
                    .isInstanceOf(EntityNotFoundException.class);
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

            when(routesRepository.findById(routeId)).thenReturn(Optional.of(routeEntity));
            when(routesRepository.save(any(RouteEntity.class))).thenAnswer(inv -> inv.getArgument(0));
            when(fromEntityConverter.convert(any(RouteEntity.class))).thenReturn(expectedResponse);

            CreateRouteResponse actual = routesService.patch(routeId, patch);

            assertThat(actual).isSameAs(expectedResponse);
            assertThat(routeEntity.getDeparturePoint()).isEqualTo("Kazan");
            assertThat(routeEntity.getDestinationPoint()).isEqualTo("Sochi");
            assertThat(routeEntity.getUpdatedAt()).isNotNull();
            verify(carriersService, never()).findOneById(any());
        }

        @Test
        @DisplayName("should update carrier when carrierId provided")
        void shouldUpdateCarrierWhenProvided() {
            UUID newCarrierId = UUID.randomUUID();
            CarrierEntity newCarrier = new CarrierEntity();
            newCarrier.setId(newCarrierId);

            RoutePatchRequest patch = new RoutePatchRequest();
            patch.setCarrierId(newCarrierId);

            when(routesRepository.findById(routeId)).thenReturn(Optional.of(routeEntity));
            when(carriersService.findOneById(newCarrierId)).thenReturn(newCarrier);
            when(routesRepository.save(any(RouteEntity.class))).thenAnswer(inv -> inv.getArgument(0));
            when(fromEntityConverter.convert(any(RouteEntity.class))).thenReturn(expectedResponse);

            routesService.patch(routeId, patch);

            assertThat(routeEntity.getCarrier()).isSameAs(newCarrier);
            verify(carriersService).findOneById(newCarrierId);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when route does not exist")
        void shouldThrowWhenNotFound() {
            RoutePatchRequest patch = new RoutePatchRequest();
            when(routesRepository.findById(routeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> routesService.patch(routeId, patch))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(routesRepository, never()).save(any());
        }
    }
}