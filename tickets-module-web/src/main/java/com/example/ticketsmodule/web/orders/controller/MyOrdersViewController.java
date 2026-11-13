package com.example.ticketsmodule.web.orders.controller;

import com.example.ticketsmodule.api.model.CreateOrderResponse;
import com.example.ticketsmodule.api.model.OrderStatusEnum;
import com.example.ticketsmodule.domain.order.exception.OrderExpiredException;

import com.example.ticketsmodule.impl.oauth.service.SessionCurrentUserService;
import com.example.ticketsmodule.impl.order.service.OrdersService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class MyOrdersViewController {

    private final OrdersService ordersService;
    private final SessionCurrentUserService sessionCurrentUserService;

    @GetMapping("/my-orders")
    public String list(Model model) {
        UUID userId = sessionCurrentUserService.getCurrentUserId();
        List<CreateOrderResponse> orders = ordersService.findAllMyOrders(userId);

        model.addAttribute("waiting",
                orders.stream().filter(o -> o.getStatus() == OrderStatusEnum.WAITING).toList());
        model.addAttribute("paid",
                orders.stream().filter(o -> o.getStatus() == OrderStatusEnum.PAID).toList());
        model.addAttribute("history",
                orders.stream().filter(o -> o.getStatus() != OrderStatusEnum.WAITING
                        && o.getStatus() != OrderStatusEnum.PAID).toList());
        return "my-orders";
    }

    @PostMapping("/my-orders/{id}/pay")
    public String pay(@PathVariable UUID id, RedirectAttributes ra) {
        try {
            ordersService.payOrder(id, sessionCurrentUserService.getCurrentUserId());
            ra.addFlashAttribute("successMessage", "Билет оплачен");
        } catch (OrderExpiredException e) {
            ra.addFlashAttribute("errorMessage", "Время бронирования истекло");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/my-orders";
    }

    @PostMapping("/my-orders/{id}/cancel")
    public String cancel(@PathVariable UUID id, RedirectAttributes ra) {
        try {
            ordersService.cancelOrder(id, sessionCurrentUserService.getCurrentUserId());
            ra.addFlashAttribute("successMessage", "Заказ отменён");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/my-orders";
    }
}