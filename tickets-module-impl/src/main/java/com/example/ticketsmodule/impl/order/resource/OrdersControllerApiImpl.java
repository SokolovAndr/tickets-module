package com.example.ticketsmodule.impl.order.resource;

import com.example.ticketsmodule.api.controller.OrdersControllerApi;
import com.example.ticketsmodule.api.model.CreateCarrierResponse;
import com.example.ticketsmodule.api.model.CreateOrderRequest;
import com.example.ticketsmodule.api.model.CreateOrderResponse;
import com.example.ticketsmodule.api.model.OrdersSearchRequest;
import com.example.ticketsmodule.impl.carriers.service.CarriersService;
import com.example.ticketsmodule.impl.oauth.service.JwtCurrentUserService;
import com.example.ticketsmodule.impl.order.service.OrdersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OrdersControllerApiImpl implements OrdersControllerApi {

    private final OrdersService ordersService;
    private final JwtCurrentUserService jwtCurrentUserService;

    /**
     * Метод создания заказа
     *
     * @param createOrderRequest (required)
     * @return Заказ создан, билет зарезервирован (status code 201)
     */
    @Override
    public ResponseEntity<CreateOrderResponse> createOrder(@RequestBody CreateOrderRequest createOrderRequest) {
        CreateOrderResponse result = ordersService.create(createOrderRequest, getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Метод отмены заказа по id (до оплаты) и возврата билета
     *
     * @param id Идентификатор заказа (required)
     * @return Операция обработана успешно, результат предоставлен (status code 200)
     */
    @Override
    public ResponseEntity<CreateOrderResponse> cancelOrder(UUID id) {
        CreateOrderResponse result = ordersService.cancelOrder(id, getCurrentUserId());
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    /**
     * Метод оплаты заказа по id
     *
     * @param id Идентификатор заказа (required)
     * @return Операция обработана успешно, результат предоставлен (status code 200)
     */
    @Override
    public ResponseEntity<CreateOrderResponse> payOrder(UUID id) {
        CreateOrderResponse result = ordersService.payOrder(id, getCurrentUserId());
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    /**
     * Метод возврата заказа по id (после оплаты) и возврата билета
     *
     * @param id Идентификатор заказа (required)
     * @return Операция обработана успешно, результат предоставлен (status code 200)
     */
    @Override
    public ResponseEntity<CreateOrderResponse> refundOrder(UUID id) {
        CreateOrderResponse result = ordersService.refundOrder(id, getCurrentUserId());
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    /**
     * Метод получения заказов пользователя
     *
     * @return Список заказов (status code 200)
     */
    @Override
    public ResponseEntity<List<CreateOrderResponse>> getAllMyOrders() {
        return ResponseEntity.ok(ordersService.findAllMyTickets(getCurrentUserId()));
    }

    /**
     * Метод предоставления списка заказов
     *
     * @param pageable    (optional)
     * @param searchParam (optional)
     * @return Поиск выполнен успешно (status code 200)
     */
    @Override
    public ResponseEntity<Page> getAllOrders(@Nullable Pageable pageable, @Nullable OrdersSearchRequest searchParam) {
        return null;
    }

    /**
     * Метод предоставления заказа по id
     *
     * @param id Идентификатор заказа (required)
     * @return Операция обработана успешно, результат предоставлен (status code 200)
     */
    @Override
    public ResponseEntity<CreateOrderResponse> getOrder(UUID id) {
        CreateOrderResponse result = ordersService.findResponseById(id);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    private UUID getCurrentUserId() {
       return jwtCurrentUserService.getCurrentUserId();
    }
}
