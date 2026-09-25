package com.example.ticketsmodule.impl.routes.resource;

import com.example.ticketsmodule.api.model.CreateRouteRequest;
import com.example.ticketsmodule.api.model.CreateRouteResponse;
import com.example.ticketsmodule.api.model.RoutePatchRequest;
import com.example.ticketsmodule.api.model.RoutesSearchRequest;
import com.example.ticketsmodule.impl.routes.service.RoutesService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoutesControllerApiImpl unit tests")
class RoutesControllerApiImplTest {

    @Mock private RoutesService routesService;

    @InjectMocks
    private RoutesControllerApiImpl controller;

    private UUID routeId;
    private CreateRouteResponse response;

    @BeforeEach
    void setUp() {
        routeId = UUID.randomUUID();
        response = new CreateRouteResponse();
    }

    // ---------- createRoute() ----------

    @Nested
    @DisplayName("createRoute()")
    class CreateRouteTests {

        @Test
        @DisplayName("should return 201 Created with body from service")
        void shouldReturn201Created() {
            CreateRouteRequest request = new CreateRouteRequest();
            when(routesService.create(request)).thenReturn(response);

            ResponseEntity<CreateRouteResponse> result = controller.createRoute(request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(result.getBody()).isSameAs(response);
            verify(routesService).create(request);
        }

        @Test
        @DisplayName("should return null body when service returns null")
        void shouldReturnNullBodyWhenServiceReturnsNull() {
            CreateRouteRequest request = new CreateRouteRequest();
            when(routesService.create(request)).thenReturn(null);

            ResponseEntity<CreateRouteResponse> result = controller.createRoute(request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(result.getBody()).isNull();
        }
    }

    // ---------- getAllRoutes() ----------

    @Nested
    @DisplayName("getAllRoutes()")
    class GetAllRoutesTests {

        @Test
        @DisplayName("should return 200 OK with page from service")
        void shouldReturn200OkWithPage() {
            Pageable pageable = PageRequest.of(0, 10);
            RoutesSearchRequest search = new RoutesSearchRequest();
            Page<CreateRouteResponse> page = new PageImpl<>(List.of(response));
            when(routesService.findAll(search, pageable)).thenReturn(page);

            ResponseEntity<Page> result = controller.getAllRoutes(pageable, search);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isSameAs(page);
            verify(routesService).findAll(search, pageable);
        }

        @Test
        @DisplayName("should return 200 OK with empty page")
        void shouldReturn200OkWithEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<CreateRouteResponse> empty = Page.empty();
            when(routesService.findAll(null, pageable)).thenReturn(empty);

            ResponseEntity<Page> result = controller.getAllRoutes(pageable, null);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEmpty();
        }
    }

    // ---------- getRoute() ----------

    @Nested
    @DisplayName("getRoute()")
    class GetRouteTests {

        @Test
        @DisplayName("should return 200 OK with body from service")
        void shouldReturn200Ok() {
            when(routesService.findOne(routeId)).thenReturn(response);

            ResponseEntity<CreateRouteResponse> result = controller.getRoute(routeId);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isSameAs(response);
            verify(routesService).findOne(routeId);
        }

        @Test
        @DisplayName("should propagate EntityNotFoundException from service")
        void shouldPropagateException() {
            when(routesService.findOne(routeId))
                    .thenThrow(new EntityNotFoundException("Route not found"));

            assertThatThrownBy(() -> controller.getRoute(routeId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Route not found");
        }
    }

    // ---------- patchRoute() ----------

    @Nested
    @DisplayName("patchRoute()")
    class PatchRouteTests {

        @Test
        @DisplayName("should return 200 OK with patched body")
        void shouldReturn200OkWithPatchedBody() {
            RoutePatchRequest patch = new RoutePatchRequest();
            when(routesService.patch(routeId, patch)).thenReturn(response);

            ResponseEntity<CreateRouteResponse> result = controller.patchRoute(routeId, patch);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isSameAs(response);
            verify(routesService).patch(routeId, patch);
        }

        @Test
        @DisplayName("should propagate EntityNotFoundException from service")
        void shouldPropagateException() {
            RoutePatchRequest patch = new RoutePatchRequest();
            when(routesService.patch(routeId, patch))
                    .thenThrow(new EntityNotFoundException("Route not found"));

            assertThatThrownBy(() -> controller.patchRoute(routeId, patch))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}