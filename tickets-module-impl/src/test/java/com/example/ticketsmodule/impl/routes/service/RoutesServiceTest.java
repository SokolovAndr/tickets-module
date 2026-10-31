package com.example.ticketsmodule.impl.routes.service;

import com.example.ticketsmodule.api.model.CreateRouteRequest;
import com.example.ticketsmodule.api.model.CreateRouteResponse;
import com.example.ticketsmodule.api.model.RoutePatchRequest;
import com.example.ticketsmodule.api.model.RoutesSearchRequest;
import com.example.ticketsmodule.impl.carriers.persistence.entity.CarrierJpaEntity;
import com.example.ticketsmodule.impl.carriers.service.CarriersService;
import com.example.ticketsmodule.impl.routes.conversion.RouteMapper;
import com.example.ticketsmodule.impl.routes.persistence.entity.RouteJpaEntity;
import com.example.ticketsmodule.impl.routes.persistence.repository.RouteSpringDataRepository;
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

    @Mock private RouteSpringDataRepository routeSpringDataRepository;
    @Mock private RouteMapper routeMapper;
    @Mock private CarriersService carriersService;

    @InjectMocks
    private RoutesService routesService;

    private UUID routeId;
    private UUID carrierId;
    private RouteJpaEntity routeJpaEntity;
    private CarrierJpaEntity carrierJpaEntity;
    private CreateRouteResponse expectedResponse;

    @BeforeEach
    void setUp() {
        routeId = UUID.randomUUID();
        carrierId = UUID.randomUUID();

        carrierJpaEntity = new CarrierJpaEntity();
        carrierJpaEntity.setId(carrierId);

        routeJpaEntity = new RouteJpaEntity();
        routeJpaEntity.setId(routeId);
        routeJpaEntity.setCarrier(carrierJpaEntity);
        routeJpaEntity.setDeparturePoint("Moscow");
        routeJpaEntity.setDestinationPoint("SPb");
        routeJpaEntity.setDepartureAt(LocalDateTime.now().plusDays(1));
        routeJpaEntity.setDestinationAt(LocalDateTime.now().plusDays(1).plusHours(2));
        routeJpaEntity.setDurationMinutes(120);

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

            when(carriersService.findOneById(carrierId)).thenReturn(carrierJpaEntity);
            when(routeMapper.toEntity(request, carrierJpaEntity)).thenReturn(routeJpaEntity);
            when(routeSpringDataRepository.save(routeJpaEntity)).thenReturn(routeJpaEntity);
            when(routeMapper.toResponse(routeJpaEntity)).thenReturn(expectedResponse);

            CreateRouteResponse actual = routesService.create(request);

            assertThat(actual).isSameAs(expectedResponse);
            verify(carriersService).findOneById(carrierId);
            verify(routeMapper).toEntity(request, carrierJpaEntity);
            verify(routeSpringDataRepository).save(routeJpaEntity);
            verify(routeMapper).toResponse(routeJpaEntity);
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

            verifyNoInteractions(routeSpringDataRepository, routeMapper);
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

            Page<RouteJpaEntity> entityPage = new PageImpl<>(List.of(routeJpaEntity));
            when(routeSpringDataRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(entityPage);
            when(routeMapper.toResponse(routeJpaEntity)).thenReturn(expectedResponse);

            Page<CreateRouteResponse> actual = routesService.findAll(search, pageable);

            assertThat(actual).hasSize(1);
            assertThat(actual.getContent()).containsExactly(expectedResponse);
            verify(routeSpringDataRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("should return empty page when no routes")
        void shouldReturnEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            when(routeSpringDataRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(Page.empty());

            Page<CreateRouteResponse> actual = routesService.findAll(null, pageable);

            assertThat(actual).isEmpty();
            verifyNoInteractions(routeMapper);
        }
    }

    // ---------- findOne() ----------

    @Nested
    @DisplayName("findOne()")
    class FindOneTests {

        @Test
        @DisplayName("should return response when route exists")
        void shouldReturnResponseWhenFound() {
            when(routeSpringDataRepository.findById(routeId)).thenReturn(Optional.of(routeJpaEntity));
            when(routeMapper.toResponse(routeJpaEntity)).thenReturn(expectedResponse);

            CreateRouteResponse actual = routesService.findOne(routeId);

            assertThat(actual).isSameAs(expectedResponse);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when route does not exist")
        void shouldThrowWhenNotFound() {
            when(routeSpringDataRepository.findById(routeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> routesService.findOne(routeId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(routeId.toString());

            verifyNoInteractions(routeMapper);
        }
    }

    // ---------- findOneById() ----------

    @Nested
    @DisplayName("findOneById()")
    class FindOneByIdTests {

        @Test
        @DisplayName("should return entity when route exists")
        void shouldReturnEntityWhenFound() {
            when(routeSpringDataRepository.findById(routeId)).thenReturn(Optional.of(routeJpaEntity));

            RouteJpaEntity actual = routesService.findOneById(routeId);

            assertThat(actual).isSameAs(routeJpaEntity);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when route does not exist")
        void shouldThrowWhenNotFound() {
            when(routeSpringDataRepository.findById(routeId)).thenReturn(Optional.empty());

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

            when(routeSpringDataRepository.findById(routeId)).thenReturn(Optional.of(routeJpaEntity));
            when(routeSpringDataRepository.save(any(RouteJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));
            when(routeMapper.toResponse(any(RouteJpaEntity.class))).thenReturn(expectedResponse);

            CreateRouteResponse actual = routesService.patch(routeId, patch);

            assertThat(actual).isSameAs(expectedResponse);
            assertThat(routeJpaEntity.getDeparturePoint()).isEqualTo("Kazan");
            assertThat(routeJpaEntity.getDestinationPoint()).isEqualTo("Sochi");
            assertThat(routeJpaEntity.getUpdatedAt()).isNotNull();
            verify(carriersService, never()).findOneById(any());
        }

        @Test
        @DisplayName("should update carrier when carrierId provided")
        void shouldUpdateCarrierWhenProvided() {
            UUID newCarrierId = UUID.randomUUID();
            CarrierJpaEntity newCarrier = new CarrierJpaEntity();
            newCarrier.setId(newCarrierId);

            RoutePatchRequest patch = new RoutePatchRequest();
            patch.setCarrierId(newCarrierId);

            when(routeSpringDataRepository.findById(routeId)).thenReturn(Optional.of(routeJpaEntity));
            when(carriersService.findOneById(newCarrierId)).thenReturn(newCarrier);
            when(routeSpringDataRepository.save(any(RouteJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));
            when(routeMapper.toResponse(any(RouteJpaEntity.class))).thenReturn(expectedResponse);

            routesService.patch(routeId, patch);

            assertThat(routeJpaEntity.getCarrier()).isSameAs(newCarrier);
            verify(carriersService).findOneById(newCarrierId);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when route does not exist")
        void shouldThrowWhenNotFound() {
            RoutePatchRequest patch = new RoutePatchRequest();
            when(routeSpringDataRepository.findById(routeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> routesService.patch(routeId, patch))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(routeSpringDataRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("findAllByIds()")
    class FindAllByIdsTests {

        @Test
        @DisplayName("should return empty map for null or empty ids")
        void shouldReturnEmptyMapForNullOrEmpty() {
            assertThat(routesService.findAllByIds(null)).isEmpty();
            assertThat(routesService.findAllByIds(List.of())).isEmpty();
            verifyNoInteractions(routeSpringDataRepository);
        }

        @Test
        @DisplayName("should return map by id")
        void shouldReturnMapById() {
            RouteJpaEntity second = new RouteJpaEntity();
            second.setId(UUID.randomUUID());

            when(routeSpringDataRepository.findAllById(List.of(routeId, second.getId())))
                    .thenReturn(List.of(routeJpaEntity, second));

            Map<UUID, RouteJpaEntity> result =
                    routesService.findAllByIds(List.of(routeId, second.getId()));

            assertThat(result)
                    .containsEntry(routeId, routeJpaEntity)
                    .containsEntry(second.getId(), second);
        }
    }
}