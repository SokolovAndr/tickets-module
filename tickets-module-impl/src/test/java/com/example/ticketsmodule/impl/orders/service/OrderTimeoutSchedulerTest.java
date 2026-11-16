package com.example.ticketsmodule.impl.orders.service;

import com.example.ticketsmodule.impl.order.service.OrderTimeoutScheduler;
import com.example.ticketsmodule.impl.order.service.OrdersService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderTimeoutScheduler unit tests")
class OrderTimeoutSchedulerTest {

    @Mock
    private OrdersService ordersService;

    @InjectMocks
    private OrderTimeoutScheduler scheduler;

    @Test
    @DisplayName("run() should delegate to OrdersService.timeOutExpired()")
    void shouldDelegateToService() {
        scheduler.run();

        verify(ordersService).timeOutExpired();
    }

    @Test
    @DisplayName("run() should swallow exceptions")
    void shouldSwallowExceptions() {
        doThrow(new RuntimeException("boom")).when(ordersService).timeOutExpired();

        assertThatCode(() -> scheduler.run()).doesNotThrowAnyException();

        verify(ordersService).timeOutExpired();
    }
}