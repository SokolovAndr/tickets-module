package com.example.ticketsmodule.impl.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutScheduler {

    private final OrdersService ordersService;

    @Scheduled(fixedDelay = 30_000)
    public void run() {
        try {
            ordersService.timeOutExpired();
        } catch (Exception e) {
            log.error("Order timeout job failed", e);
        }
    }
}