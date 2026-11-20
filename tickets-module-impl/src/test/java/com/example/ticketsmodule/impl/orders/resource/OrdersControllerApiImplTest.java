package com.example.ticketsmodule.impl.orders.resource;

import com.example.ticketsmodule.api.model.CreateOrderRequest;
import com.example.ticketsmodule.api.model.CreateOrderResponse;
import com.example.ticketsmodule.api.model.OrdersSearchRequest;
import com.example.ticketsmodule.domain.common.PageRequest;
import com.example.ticketsmodule.domain.common.PageResult;
import com.example.ticketsmodule.domain.order.exception.OrderAccessDeniedException;
import com.example.ticketsmodule.domain.order.exception.OrderNotFoundException;
import com.example.ticketsmodule.impl.oauth.service.JwtCurrentUserService;
import com.example.ticketsmodule.impl.order.resource.OrdersControllerApiImpl;
import com.example.ticketsmodule.impl.order.service.OrdersService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrdersControllerApiImpl unit tests")
class OrdersControllerApiImplTest {

    @Mock private OrdersService ordersService;
    @Mock private JwtCurrentUserService jwtCurrentUserService;

    @InjectMocks
    private OrdersControllerApiImpl controller;

    private UUID orderId;
    private UUID userId;
    private CreateOrderResponse response;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        userId = UUID.randomUUID();
        response = new CreateOrderResponse();
    }

    @Nested
    @DisplayName("createOrder()")
    class CreateOrderTests {

        @Test
        @DisplayName("should return 201 Created")
        void shouldReturn201() {
            CreateOrderRequest request = new CreateOrderRequest();
            when(jwtCurrentUserService.getCurrentUserId()).thenReturn(userId);
            when(ordersService.create(request, userId)).thenReturn(response);

            ResponseEntity<CreateOrderResponse> result = controller.createOrder(request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(result.getBody()).isSameAs(response);
        }
    }

    @Nested
    @DisplayName("cancelOrder()")
    class CancelOrderTests {

        @Test
        @DisplayName("should return 200 OK")
        void shouldReturn200() {
            when(jwtCurrentUserService.getCurrentUserId()).thenReturn(userId);
            when(ordersService.cancelOrder(orderId, userId)).thenReturn(response);

            ResponseEntity<CreateOrderResponse> result = controller.cancelOrder(orderId);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isSameAs(response);
        }
    }

    @Nested
    @DisplayName("payOrder()")
    class PayOrderTests {

        @Test
        @DisplayName("should return 200 OK")
        void shouldReturn200() {
            when(jwtCurrentUserService.getCurrentUserId()).thenReturn(userId);
            when(ordersService.payOrder(orderId, userId)).thenReturn(response);

            ResponseEntity<CreateOrderResponse> result = controller.payOrder(orderId);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isSameAs(response);
        }
    }

    @Nested
    @DisplayName("refundOrder()")
    class RefundOrderTests {

        @Test
        @DisplayName("should return 200 OK")
        void shouldReturn200() {
            when(jwtCurrentUserService.getCurrentUserId()).thenReturn(userId);
            when(ordersService.refundOrder(orderId, userId)).thenReturn(response);

            ResponseEntity<CreateOrderResponse> result = controller.refundOrder(orderId);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isSameAs(response);
        }
    }

    @Nested
    @DisplayName("getAllMyOrders()")
    class GetAllMyOrdersTests {

        @Test
        @DisplayName("should return 200 OK with list")
        void shouldReturnList() {
            when(jwtCurrentUserService.getCurrentUserId()).thenReturn(userId);
            when(ordersService.findAllMyOrders(userId)).thenReturn(List.of(response));

            ResponseEntity<List<CreateOrderResponse>> result = controller.getAllMyOrders();

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).containsExactly(response);
        }
    }

    @Nested
    @DisplayName("getAllOrders()")
    class GetAllOrdersTests {

        @Test
        @DisplayName("should convert pageable and return Spring Page")
        void shouldConvertPageable() {
            Pageable springPageable = org.springframework.data.domain.PageRequest.of(1, 5);
            OrdersSearchRequest search = new OrdersSearchRequest();
            PageResult<CreateOrderResponse> domainResult = PageResult.<CreateOrderResponse>builder()
                    .content(List.of(response))
                    .totalElements(100)
                    .build();

            ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
            when(ordersService.findAll(eq(search), captor.capture())).thenReturn(domainResult);

            ResponseEntity<Page> result = controller.getAllOrders(springPageable, search);

            assertThat(captor.getValue().getPage()).isEqualTo(1);
            assertThat(captor.getValue().getSize()).isEqualTo(5);
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().getTotalElements()).isEqualTo(100);
            assertThat(result.getBody().getSize()).isEqualTo(5);
        }

        @Test
        @DisplayName("should use default page (0/20) when pageable is null")
        void shouldUseDefaultPageableWhenNull() {
            OrdersSearchRequest search = new OrdersSearchRequest();
            PageResult<CreateOrderResponse> domainResult = PageResult.<CreateOrderResponse>builder()
                    .content(List.of(response))
                    .totalElements(1)
                    .build();

            ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
            when(ordersService.findAll(eq(search), captor.capture())).thenReturn(domainResult);

            ResponseEntity<Page> result = controller.getAllOrders(null, search);

            assertThat(captor.getValue().getPage()).isEqualTo(0);
            assertThat(captor.getValue().getSize()).isEqualTo(20);
            assertThat(result.getBody()).isNotNull();
        }

        @Test
        @DisplayName("should accept null searchParam")
        void shouldAcceptNullSearch() {
            Pageable springPageable = org.springframework.data.domain.PageRequest.of(0, 10);
            PageResult<CreateOrderResponse> domainResult = PageResult.<CreateOrderResponse>builder()
                    .content(List.of())
                    .totalElements(0)
                    .build();

            when(ordersService.findAll(isNull(), any(PageRequest.class))).thenReturn(domainResult);

            ResponseEntity<Page> result = controller.getAllOrders(springPageable, null);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody().getTotalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("getOrder()")
    class GetOrderTests {

        @Test
        @DisplayName("should return 200 OK")
        void shouldReturn200() {
            when(jwtCurrentUserService.getCurrentUserId()).thenReturn(userId);
            when(ordersService.findResponseById(orderId, userId)).thenReturn(response);

            ResponseEntity<CreateOrderResponse> result = controller.getOrder(orderId);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isSameAs(response);
        }

        @Test
        @DisplayName("should propagate OrderNotFoundException")
        void shouldPropagateNotFound() {
            when(jwtCurrentUserService.getCurrentUserId()).thenReturn(userId);
            when(ordersService.findResponseById(orderId, userId))
                    .thenThrow(new OrderNotFoundException(orderId));

            assertThatThrownBy(() -> controller.getOrder(orderId))
                    .isInstanceOf(OrderNotFoundException.class);
        }

        @Test
        @DisplayName("should propagate OrderAccessDeniedException")
        void shouldPropagateAccessDenied() {
            when(jwtCurrentUserService.getCurrentUserId()).thenReturn(userId);
            when(ordersService.findResponseById(orderId, userId))
                    .thenThrow(new OrderAccessDeniedException(orderId, userId));

            assertThatThrownBy(() -> controller.getOrder(orderId))
                    .isInstanceOf(OrderAccessDeniedException.class);
        }
    }
}