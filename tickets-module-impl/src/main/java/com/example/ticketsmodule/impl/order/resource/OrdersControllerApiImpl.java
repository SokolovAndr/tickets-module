package com.example.ticketsmodule.impl.order.resource;

import com.example.ticketsmodule.api.controller.OrdersControllerApi;
import com.example.ticketsmodule.api.model.CreateOrderRequest;
import com.example.ticketsmodule.api.model.CreateOrderResponse;
import com.example.ticketsmodule.api.model.OrdersSearchRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public class OrdersControllerApiImpl implements OrdersControllerApi {

    /**
     * Метод создания заказа
     *
     * @param createOrderRequest (required)
     * @return Заказ создан, билет зарезервирован (status code 201)
     */
    @Override
    public ResponseEntity<CreateOrderResponse> createOrder(CreateOrderRequest createOrderRequest) {
        return null;
    }

    /**
     * Метод отмены заказа по id (до оплаты) и возврата билета
     *
     * @param id Идентификатор заказа (required)
     * @return Операция обработана успешно, результат предоставлен (status code 200)
     */
    @Override
    public ResponseEntity<CreateOrderResponse> cancelOrder(UUID id) {
        return null;
    }


    /**
     * Метод оплаты заказа по id
     *
     * @param id Идентификатор заказа (required)
     * @return Операция обработана успешно, результат предоставлен (status code 200)
     */
    @Override
    public ResponseEntity<CreateOrderResponse> payOrder(UUID id) {
        return null;
    }

    /**
     * Метод возврата заказа по id (после оплаты) и возврата билета
     *
     * @param id Идентификатор заказа (required)
     * @return Операция обработана успешно, результат предоставлен (status code 200)
     */
    @Override
    public ResponseEntity<CreateOrderResponse> refundOrder(UUID id) {
        return null;
    }

    /**
     * Метод получения заказов пользователя
     *
     * @return Список заказов (status code 200)
     */
    @Override
    public ResponseEntity<List<CreateOrderResponse>> getAllMyOrders() {
        return null;
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
        return null;
    }
}
