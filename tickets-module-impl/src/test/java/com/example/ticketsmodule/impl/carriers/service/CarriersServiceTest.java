package com.example.ticketsmodule.impl.carriers.service;

import com.example.ticketsmodule.api.model.CarrierPatchRequest;
import com.example.ticketsmodule.api.model.CarriersSearchRequest;
import com.example.ticketsmodule.api.model.CreateCarrierRequest;
import com.example.ticketsmodule.api.model.CreateCarrierResponse;
import com.example.ticketsmodule.domain.carrier.exception.CarrierNameAlreadyExistsException;
import com.example.ticketsmodule.domain.carrier.exception.CarrierNotFoundException;
import com.example.ticketsmodule.domain.carrier.model.Carrier;
import com.example.ticketsmodule.domain.carrier.repository.CarriersRepositoryPort;
import com.example.ticketsmodule.domain.common.PageRequest;
import com.example.ticketsmodule.domain.common.PageResult;
import com.example.ticketsmodule.impl.carriers.conversion.CarrierMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @Mock private CarriersRepositoryPort carriersRepositoryPort; // <-- Мокаем порт
    @Mock private CarrierMapper carrierMapper;

    @InjectMocks
    private CarriersService carriersService;

    private UUID carrierId;
    private Carrier carrier;
    private CreateCarrierResponse expectedResponse;

    @BeforeEach
    void setUp() {
        carrierId = UUID.randomUUID();

        carrier = Carrier.builder()
                .id(carrierId)
                .name("Acme Transport")
                .phone("+7-999-000-00-00")
                .build();

        expectedResponse = new CreateCarrierResponse();
        expectedResponse.setId(carrierId);
        expectedResponse.setName("Acme Transport");
        expectedResponse.setPhone("+7-999-000-00-00");
    }

    // ---------- create() ----------

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("should save carrier and return response")
        void shouldSaveCarrierAndReturnResponse() {
            CreateCarrierRequest request = new CreateCarrierRequest();
            request.setName("Acme Transport");

            when(carriersRepositoryPort.existsByName(request.getName())).thenReturn(false);
            when(carrierMapper.toDomain(request)).thenReturn(carrier);
            when(carriersRepositoryPort.save(any(Carrier.class))).thenReturn(carrier);
            when(carrierMapper.toResponse(any(Carrier.class))).thenReturn(expectedResponse);

            CreateCarrierResponse actual = carriersService.create(request);

            assertThat(actual).isSameAs(expectedResponse);
            verify(carrierMapper).toDomain(request);
            verify(carriersRepositoryPort).save(any(Carrier.class));
            verify(carrierMapper).toResponse(any(Carrier.class));
        }

        @Test
        @DisplayName("should throw CarrierNameAlreadyExistsException if name exists")
        void shouldThrowIfNameExists() {
            CreateCarrierRequest request = new CreateCarrierRequest();
            request.setName("Acme Transport");

            when(carriersRepositoryPort.existsByName(request.getName())).thenReturn(true);

            assertThatThrownBy(() -> carriersService.create(request))
                    .isInstanceOf(CarrierNameAlreadyExistsException.class);

            verify(carrierMapper, never()).toDomain(any());
            verify(carriersRepositoryPort, never()).save(any());
        }
    }

    // ---------- findOne() / findOneById() ----------

    @Nested
    @DisplayName("find methods")
    class FindTests {

        @Test
        @DisplayName("findOne() should return response when carrier exists")
        void shouldReturnResponseWhenFound() {
            when(carriersRepositoryPort.findById(carrierId)).thenReturn(Optional.of(carrier));
            when(carrierMapper.toResponse(carrier)).thenReturn(expectedResponse);

            CreateCarrierResponse actual = carriersService.findOne(carrierId);

            assertThat(actual).isSameAs(expectedResponse);
        }

        @Test
        @DisplayName("findOneById() should return domain carrier when exists")
        void shouldReturnDomainWhenFound() {
            when(carriersRepositoryPort.findById(carrierId)).thenReturn(Optional.of(carrier));

            Carrier actual = carriersService.findOneById(carrierId);

            assertThat(actual).isSameAs(carrier);
        }

        @Test
        @DisplayName("should throw CarrierNotFoundException when carrier does not exist")
        void shouldThrowWhenNotFound() {
            when(carriersRepositoryPort.findById(carrierId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> carriersService.findOne(carrierId))
                    .isInstanceOf(CarrierNotFoundException.class);

            assertThatThrownBy(() -> carriersService.findOneById(carrierId))
                    .isInstanceOf(CarrierNotFoundException.class);
        }
    }

    // ---------- findAll() ----------

    @Nested
    @DisplayName("findAll()")
    class FindAllTests {

        @Test
        @DisplayName("should return mapped PageResult")
        void shouldReturnMappedPageResult() {
            CarriersSearchRequest search = new CarriersSearchRequest();
            PageRequest pageReq = PageRequest.builder().page(0).size(10).build();

            PageResult<Carrier> domainPage = PageResult.<Carrier>builder()
                    .content(List.of(carrier))
                    .totalElements(1)
                    .build();

            when(carriersRepositoryPort.findAll(any(), eq(pageReq))).thenReturn(domainPage);
            when(carrierMapper.toResponse(carrier)).thenReturn(expectedResponse);

            PageResult<CreateCarrierResponse> actual = carriersService.findAll(search, pageReq);

            assertThat(actual.getContent()).hasSize(1);
            assertThat(actual.getContent().get(0)).isSameAs(expectedResponse);
            assertThat(actual.getTotalElements()).isEqualTo(1);
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

            when(carriersRepositoryPort.findById(carrierId)).thenReturn(Optional.of(carrier));
            when(carriersRepositoryPort.save(any(Carrier.class))).thenAnswer(inv -> inv.getArgument(0));
            when(carrierMapper.toResponse(any(Carrier.class))).thenReturn(expectedResponse);

            carriersService.patch(carrierId, patch);

            assertThat(carrier.getName()).isEqualTo("New Name");
            assertThat(carrier.getPhone()).isEqualTo("+7-999-000-00-00"); // не тронуто
            assertThat(carrier.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should throw CarrierNotFoundException when carrier does not exist")
        void shouldThrowWhenNotFound() {
            CarrierPatchRequest patch = new CarrierPatchRequest();
            when(carriersRepositoryPort.findById(carrierId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> carriersService.patch(carrierId, patch))
                    .isInstanceOf(CarrierNotFoundException.class);

            verify(carriersRepositoryPort, never()).save(any());
        }
    }
}