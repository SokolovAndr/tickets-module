package com.example.ticketsmodule.web.orders.controller;

import com.example.ticketsmodule.api.model.CreateOrderResponse;
import com.example.ticketsmodule.api.model.OrderStatusEnum;
import com.example.ticketsmodule.domain.order.exception.OrderExpiredException;
import com.example.ticketsmodule.domain.ticket.model.Ticket;
import com.example.ticketsmodule.impl.oauth.service.SessionCurrentUserService;
import com.example.ticketsmodule.impl.order.service.OrdersService;
import com.example.ticketsmodule.impl.tickets.pdf.TicketPdfGenerator;
import com.example.ticketsmodule.impl.tickets.service.TicketsService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
    private final TicketsService ticketsService; // Добавили
    private final TicketPdfGenerator ticketPdfGenerator; // Добавили
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

    @PostMapping("/my-orders/{id}/refund")
    public String refund(@PathVariable UUID id, RedirectAttributes ra) {
        try {
            ordersService.refundOrder(id, sessionCurrentUserService.getCurrentUserId());
            ra.addFlashAttribute("successMessage", "Билет возвращён");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/my-orders";
    }

    // --- НОВЫЙ МЕТОД ДЛЯ PDF ---
    @GetMapping("/my-orders/{ticketId}/pdf")
    public ResponseEntity<Resource> downloadTicketPdf(@PathVariable UUID ticketId) {
        UUID currentUserId = sessionCurrentUserService.getCurrentUserId();

        Ticket ticket = ticketsService.findById(ticketId);

        if (ticket.getUser() == null || !ticket.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Это не ваш билет");
        }

        byte[] pdfBytes = ticketPdfGenerator.generatePdf(ticket);
        ByteArrayResource resource = new ByteArrayResource(pdfBytes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ticket-" + ticketId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(resource);
    }
}