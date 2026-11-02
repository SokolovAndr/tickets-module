package com.example.ticketsmodule.impl.carriers.resource;

import com.example.ticketsmodule.api.model.CarrierPatchRequest;
import com.example.ticketsmodule.api.model.CarriersSearchRequest;
import com.example.ticketsmodule.api.model.CreateCarrierRequest;
import com.example.ticketsmodule.api.model.CreateCarrierResponse;
import com.example.ticketsmodule.domain.carrier.exception.CarrierNotFoundException;
import com.example.ticketsmodule.domain.common.PageRequest;
import com.example.ticketsmodule.domain.common.PageResult;
import com.example.ticketsmodule.impl.carriers.service.CarriersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CarriersControllerApiImpl unit tests")
class CarriersControllerApiImplTest {

    @Mock private CarriersService carriersService;

    @InjectMocks
    private CarriersControllerApiImpl controller;

    private UUID carrierId;
    private CreateCarrierResponse response;

    @BeforeEach
    void setUp() {
        carrierId = UUID.randomUUID();
        response = new CreateCarrierResponse();
        response.setId(carrierId);
    }

    @Nested
    @DisplayName("createCarrier()")
    class CreateCarrierTests {
        @Test
        @DisplayName("should return 201 Created")
        void shouldReturn201Created() {
            CreateCarrierRequest request = new CreateCarrierRequest();
            when(carriersService.create(request)).thenReturn(response);

            ResponseEntity<CreateCarrierResponse> result = controller.createCarrier(request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(result.getBody()).isSameAs(response);
        }
    }

    @Nested
    @DisplayName("getAllCarriers()")
    class GetAllCarriersTests {

        @Test
        @DisplayName("should convert Pageable to PageRequest and return Spring Page")
        void shouldConvertPageableToDomainAndBack() {
            Pageable springPageable = org.springframework.data.domain.PageRequest.of(2, 15);
            CarriersSearchRequest search = new CarriersSearchRequest();

            PageResult<CreateCarrierResponse> domainResult = PageResult.<CreateCarrierResponse>builder()
                    .content(List.of(response))
                    .totalElements(31)
                    .build();

            // Перехватываем то, что контроллер передает в сервис
            ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
            when(carriersService.findAll(eq(search), pageRequestCaptor.capture())).thenReturn(domainResult);

            ResponseEntity<Page> result = controller.getAllCarriers(springPageable, search);

            // Проверяем, что доменный PageRequest сформирован верно
            PageRequest capturedRequest = pageRequestCaptor.getValue();
            assertThat(capturedRequest.getPage()).isEqualTo(2);
            assertThat(capturedRequest.getSize()).isEqualTo(15);

            // Проверяем, что на выходе Spring Page
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().getTotalElements()).isEqualTo(31);
            assertThat(result.getBody().getContent()).containsExactly(response);
        }
    }

    @Nested
    @DisplayName("getCarrier() / patchCarrier()")
    class SingleCarrierTests {

        @Test
        @DisplayName("should return 200 OK for getCarrier")
        void shouldReturn200Ok() {
            when(carriersService.findOne(carrierId)).thenReturn(response);
            ResponseEntity<CreateCarrierResponse> result = controller.getCarrier(carrierId);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isSameAs(response);
        }

        @Test
        @DisplayName("should propagate CarrierNotFoundException")
        void shouldPropagateException() {
            when(carriersService.findOne(carrierId)).thenThrow(new CarrierNotFoundException(carrierId));

            assertThatThrownBy(() -> controller.getCarrier(carrierId))
                    .isInstanceOf(CarrierNotFoundException.class);
        }

        @Test
        @DisplayName("should return 200 OK for patchCarrier")
        void shouldReturn200OkForPatch() {
            CarrierPatchRequest patch = new CarrierPatchRequest();
            when(carriersService.patch(carrierId, patch)).thenReturn(response);

            ResponseEntity<CreateCarrierResponse> result = controller.patchCarrier(carrierId, patch);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isSameAs(response);
        }
    }
}