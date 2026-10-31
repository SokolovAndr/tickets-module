package com.example.ticketsmodule.impl.carriers.service;

import com.example.ticketsmodule.api.model.CarrierPatchRequest;
import com.example.ticketsmodule.api.model.CarriersSearchRequest;
import com.example.ticketsmodule.api.model.CreateCarrierRequest;
import com.example.ticketsmodule.api.model.CreateCarrierResponse;
import com.example.ticketsmodule.impl.carriers.conversion.CarrierMapper;
import com.example.ticketsmodule.impl.carriers.persistence.entity.CarrierJpaEntity;
import com.example.ticketsmodule.impl.carriers.persistence.repository.CarrierSpringDataRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CarriersService unit tests")
class CarriersServiceTest {

    @Mock private CarrierSpringDataRepository carrierSpringDataRepository;
    @Mock private CarrierMapper carrierMapper;

    @InjectMocks
    private CarriersService carriersService;

    private UUID carrierId;
    private CarrierJpaEntity carrierJpaEntity;
    private CreateCarrierResponse expectedResponse;

    @BeforeEach
    void setUp() {
        carrierId = UUID.randomUUID();

        carrierJpaEntity = new CarrierJpaEntity();
        carrierJpaEntity.setId(carrierId);
        carrierJpaEntity.setName("Acme Transport");
        carrierJpaEntity.setPhone("+7-999-000-00-00");

        expectedResponse = new CreateCarrierResponse();
    }

    // ---------- create() ----------

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("should save carrier and return converted response")
        void shouldSaveCarrierAndReturnResponse() {
            CreateCarrierRequest request = new CreateCarrierRequest();
            request.setName("Acme Transport");

            when(carrierMapper.toEntity(request)).thenReturn(carrierJpaEntity);
            when(carrierSpringDataRepository.save(carrierJpaEntity)).thenReturn(carrierJpaEntity);
            when(carrierMapper.toResponse(carrierJpaEntity)).thenReturn(expectedResponse);

            CreateCarrierResponse actual = carriersService.create(request);

            assertThat(actual).isSameAs(expectedResponse);
            verify(carrierMapper).toEntity(request);
            verify(carrierSpringDataRepository).save(carrierJpaEntity);
            verify(carrierMapper).toResponse(carrierJpaEntity);
        }

        @Test
        @DisplayName("should propagate exception when converter returns null")
        void shouldPropagateWhenConverterReturnsNull() {
            CreateCarrierRequest request = new CreateCarrierRequest();

            when(carrierMapper.toEntity(request)).thenReturn(null);

            // assert carrierEntity != null — выбросит AssertionError при -ea
            assertThatThrownBy(() -> carriersService.create(request))
                    .isInstanceOf(IllegalStateException.class);

            verify(carrierSpringDataRepository, never()).save(any());
        }
    }

    // ---------- findOne() ----------

    @Nested
    @DisplayName("findOne()")
    class FindOneTests {

        @Test
        @DisplayName("should return response when carrier exists")
        void shouldReturnResponseWhenFound() {
            when(carrierSpringDataRepository.findById(carrierId)).thenReturn(Optional.of(carrierJpaEntity));
            when(carrierMapper.toResponse(carrierJpaEntity)).thenReturn(expectedResponse);

            CreateCarrierResponse actual = carriersService.findOne(carrierId);

            assertThat(actual).isSameAs(expectedResponse);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when carrier does not exist")
        void shouldThrowWhenNotFound() {
            when(carrierSpringDataRepository.findById(carrierId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> carriersService.findOne(carrierId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(carrierId.toString());

            verifyNoInteractions(carrierMapper);
        }
    }

    // ---------- findOneById() ----------

    @Nested
    @DisplayName("findOneById()")
    class FindOneByIdTests {

        @Test
        @DisplayName("should return entity when carrier exists")
        void shouldReturnEntityWhenFound() {
            when(carrierSpringDataRepository.findById(carrierId)).thenReturn(Optional.of(carrierJpaEntity));

            CarrierJpaEntity actual = carriersService.findOneById(carrierId);

            assertThat(actual).isSameAs(carrierJpaEntity);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when carrier does not exist")
        void shouldThrowWhenNotFound() {
            when(carrierSpringDataRepository.findById(carrierId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> carriersService.findOneById(carrierId))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // ---------- findAll() ----------

    @Nested
    @DisplayName("findAll()")
    class FindAllTests {

        @Test
        @DisplayName("should return mapped page")
        void shouldReturnMappedPage() {
            CarriersSearchRequest search = new CarriersSearchRequest();
            Pageable pageable = PageRequest.of(0, 10);

            when(carrierSpringDataRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(new PageImpl<>(List.of(carrierJpaEntity)));
            when(carrierMapper.toResponse(carrierJpaEntity)).thenReturn(expectedResponse);

            Page<CreateCarrierResponse> actual = carriersService.findAll(search, pageable);

            assertThat(actual).hasSize(1);
            assertThat(actual.getContent()).containsExactly(expectedResponse);
        }

        @Test
        @DisplayName("should return empty page when no carriers")
        void shouldReturnEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            when(carrierSpringDataRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(Page.empty());

            Page<CreateCarrierResponse> actual = carriersService.findAll(null, pageable);

            assertThat(actual).isEmpty();
            verifyNoInteractions(carrierMapper);
        }
    }

    // ---------- patch() ----------

    @Nested
    @DisplayName("patch()")
    class PatchTests {

        @Test
        @DisplayName("should update only provided fields")
        void shouldUpdateOnlyProvidedFields() {
            CarrierPatchRequest patch = new CarrierPatchRequest();
            patch.setName("New Name");

            when(carrierSpringDataRepository.findById(carrierId)).thenReturn(Optional.of(carrierJpaEntity));
            when(carrierSpringDataRepository.save(any(CarrierJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));
            when(carrierMapper.toResponse(any(CarrierJpaEntity.class))).thenReturn(expectedResponse);

            CreateCarrierResponse actual = carriersService.patch(carrierId, patch);

            assertThat(actual).isSameAs(expectedResponse);
            assertThat(carrierJpaEntity.getName()).isEqualTo("New Name");
            assertThat(carrierJpaEntity.getPhone()).isEqualTo("+7-999-000-00-00"); // не тронуто
            assertThat(carrierJpaEntity.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should update phone when provided")
        void shouldUpdatePhoneWhenProvided() {
            CarrierPatchRequest patch = new CarrierPatchRequest();
            patch.setPhone("+7-111-222-33-33");

            when(carrierSpringDataRepository.findById(carrierId)).thenReturn(Optional.of(carrierJpaEntity));
            when(carrierSpringDataRepository.save(any(CarrierJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));
            when(carrierMapper.toResponse(any(CarrierJpaEntity.class))).thenReturn(expectedResponse);

            carriersService.patch(carrierId, patch);

            assertThat(carrierJpaEntity.getPhone()).isEqualTo("+7-111-222-33-33");
            assertThat(carrierJpaEntity.getName()).isEqualTo("Acme Transport"); // не тронуто
        }

        @Test
        @DisplayName("should not update anything when patch is empty")
        void shouldNotUpdateAnythingWhenPatchIsEmpty() {
            CarrierPatchRequest patch = new CarrierPatchRequest();

            when(carrierSpringDataRepository.findById(carrierId)).thenReturn(Optional.of(carrierJpaEntity));
            when(carrierSpringDataRepository.save(any(CarrierJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));
            when(carrierMapper.toResponse(any(CarrierJpaEntity.class))).thenReturn(expectedResponse);

            carriersService.patch(carrierId, patch);

            assertThat(carrierJpaEntity.getName()).isEqualTo("Acme Transport");
            assertThat(carrierJpaEntity.getPhone()).isEqualTo("+7-999-000-00-00");
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when carrier does not exist")
        void shouldThrowWhenNotFound() {
            CarrierPatchRequest patch = new CarrierPatchRequest();
            when(carrierSpringDataRepository.findById(carrierId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> carriersService.patch(carrierId, patch))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(carrierSpringDataRepository, never()).save(any());
        }
    }
}