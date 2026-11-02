package com.example.ticketsmodule.impl.routes.resource;

import com.example.ticketsmodule.api.model.CreateRouteRequest;
import com.example.ticketsmodule.api.model.CreateRouteResponse;
import com.example.ticketsmodule.api.model.RoutePatchRequest;
import com.example.ticketsmodule.api.model.RoutesSearchRequest;
import com.example.ticketsmodule.domain.common.PageRequest;
import com.example.ticketsmodule.domain.common.PageResult;
import com.example.ticketsmodule.domain.route.exception.RouteNotFoundException;
import com.example.ticketsmodule.impl.routes.service.RoutesService;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
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

    @Nested
    @DisplayName("createRoute()")
    class CreateRouteTests {
        @Test
        @DisplayName("should return 201 Created")
        void shouldReturn201Created() {
            CreateRouteRequest request = new CreateRouteRequest();
            when(routesService.create(request)).thenReturn(response);

            ResponseEntity<CreateRouteResponse> result = controller.createRoute(request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(result.getBody()).isSameAs(response);
        }
    }

    @Nested
    @DisplayName("getAllRoutes()")
    class GetAllRoutesTests {
        @Test
        @DisplayName("should convert Pageable to PageRequest")
        void shouldConvertPageableToDomain() {
            Pageable springPageable = org.springframework.data.domain.PageRequest.of(1, 5);
            RoutesSearchRequest search = new RoutesSearchRequest();
            PageResult<CreateRouteResponse> domainResult = PageResult.<CreateRouteResponse>builder()
                    .content(List.of(response))
                    .totalElements(100)
                    .build();

            ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
            when(routesService.findAll(eq(search), captor.capture())).thenReturn(domainResult);

            ResponseEntity<Page> result = controller.getAllRoutes(springPageable, search);

            assertThat(captor.getValue().getPage()).isEqualTo(1);
            assertThat(captor.getValue().getSize()).isEqualTo(5);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().getTotalElements()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("getRoute() / patchRoute()")
    class SingleRouteTests {

        @Test
        @DisplayName("should return 200 OK")
        void shouldReturn200Ok() {
            when(routesService.findOne(routeId)).thenReturn(response);
            ResponseEntity<CreateRouteResponse> result = controller.getRoute(routeId);
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isSameAs(response);
        }

        @Test
        @DisplayName("should propagate RouteNotFoundException")
        void shouldPropagateException() {
            when(routesService.findOne(routeId)).thenThrow(new RouteNotFoundException(routeId));

            assertThatThrownBy(() -> controller.getRoute(routeId))
                    .isInstanceOf(RouteNotFoundException.class);
        }

        @Test
        @DisplayName("should return 200 OK for patch")
        void shouldReturn200OkForPatch() {
            RoutePatchRequest patch = new RoutePatchRequest();
            when(routesService.patch(routeId, patch)).thenReturn(response);

            ResponseEntity<CreateRouteResponse> result = controller.patchRoute(routeId, patch);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isSameAs(response);
        }
    }
}