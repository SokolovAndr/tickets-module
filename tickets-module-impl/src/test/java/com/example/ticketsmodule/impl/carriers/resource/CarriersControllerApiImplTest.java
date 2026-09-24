package com.example.ticketsmodule.impl.carriers.resource;

import com.example.ticketsmodule.api.model.CarrierPatchRequest;
import com.example.ticketsmodule.api.model.CarriersSearchRequest;
import com.example.ticketsmodule.api.model.CreateCarrierRequest;
import com.example.ticketsmodule.api.model.CreateCarrierResponse;
import com.example.ticketsmodule.impl.carriers.service.CarriersService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
    }

    // ---------- createCarrier() ----------

    @Nested
    @DisplayName("createCarrier()")
    class CreateCarrierTests {

        @Test
        @DisplayName("should return 201 Created with body from service")
        void shouldReturn201Created() {
            CreateCarrierRequest request = new CreateCarrierRequest();
            when(carriersService.create(request)).thenReturn(response);

            ResponseEntity<CreateCarrierResponse> result = controller.createCarrier(request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(result.getBody()).isSameAs(response);
            verify(carriersService).create(request);
        }

        @Test
        @DisplayName("should return null body when service returns null")
        void shouldReturnNullBodyWhenServiceReturnsNull() {
            CreateCarrierRequest request = new CreateCarrierRequest();
            when(carriersService.create(request)).thenReturn(null);

            ResponseEntity<CreateCarrierResponse> result = controller.createCarrier(request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(result.getBody()).isNull();
        }
    }

    // ---------- getAllCarriers() ----------

    @Nested
    @DisplayName("getAllCarriers()")
    class GetAllCarriersTests {

        @Test
        @DisplayName("should return 200 OK with page from service")
        void shouldReturn200OkWithPage() {
            Pageable pageable = PageRequest.of(0, 10);
            CarriersSearchRequest search = new CarriersSearchRequest();
            Page<CreateCarrierResponse> page = new PageImpl<>(List.of(response));
            when(carriersService.findAll(search, pageable)).thenReturn(page);

            ResponseEntity<Page> result = controller.getAllCarriers(pageable, search);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isSameAs(page);
            verify(carriersService).findAll(search, pageable);
        }

        @Test
        @DisplayName("should return 200 OK with empty page")
        void shouldReturn200OkWithEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<CreateCarrierResponse> empty = Page.empty();
            when(carriersService.findAll(null, pageable)).thenReturn(empty);

            ResponseEntity<Page> result = controller.getAllCarriers(pageable, null);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEmpty();
        }
    }

    // ---------- getCarrier() ----------

    @Nested
    @DisplayName("getCarrier()")
    class GetCarrierTests {

        @Test
        @DisplayName("should return 200 OK with body from service")
        void shouldReturn200Ok() {
            when(carriersService.findOne(carrierId)).thenReturn(response);

            ResponseEntity<CreateCarrierResponse> result = controller.getCarrier(carrierId);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isSameAs(response);
            verify(carriersService).findOne(carrierId);
        }

        @Test
        @DisplayName("should propagate exception from service")
        void shouldPropagateExceptionFromService() {
            when(carriersService.findOne(carrierId))
                    .thenThrow(new jakarta.persistence.EntityNotFoundException("not found"));

            org.assertj.core.api.Assertions
                    .assertThatThrownBy(() -> controller.getCarrier(carrierId))
                    .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
        }
    }

    // ---------- patchCarrier() ----------

    @Nested
    @DisplayName("patchCarrier()")
    class PatchCarrierTests {

        @Test
        @DisplayName("should return 200 OK with patched body")
        void shouldReturn200OkWithPatchedBody() {
            CarrierPatchRequest patch = new CarrierPatchRequest();
            when(carriersService.patch(carrierId, patch)).thenReturn(response);

            ResponseEntity<CreateCarrierResponse> result = controller.patchCarrier(carrierId, patch);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isSameAs(response);
            verify(carriersService).patch(carrierId, patch);
        }

        @Test
        @DisplayName("should propagate exception from service")
        void shouldPropagateExceptionFromService() {
            CarrierPatchRequest patch = new CarrierPatchRequest();
            when(carriersService.patch(carrierId, patch))
                    .thenThrow(new jakarta.persistence.EntityNotFoundException("not found"));

            org.assertj.core.api.Assertions
                    .assertThatThrownBy(() -> controller.patchCarrier(carrierId, patch))
                    .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
        }
    }
}