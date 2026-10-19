package com.example.ticketsmodule.impl.tickets.service;

import com.example.ticketsmodule.impl.tickets.domain.TicketEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TicketPdfService unit tests")
class TicketPdfServiceTest {

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private TicketPdfService ticketPdfService;

    private TicketEntity testTicket;

    @BeforeEach
    void setUp() {
        testTicket = TicketEntity.builder()
                .id(UUID.randomUUID())
                .seatNumber(1)
                .build();
    }

    @Test
    @DisplayName("generatePdf() should return valid PDF bytes on success")
    void generatePdf_shouldReturnPdfBytes() {
        // given: мокаем Thymeleaf, чтобы он вернул простой HTML
        String mockHtml = "<html><body><h1>Test Ticket</h1></body></html>";
        when(templateEngine.process(eq("pdf/ticket"), any(Context.class)))
                .thenReturn(mockHtml);

        // when: вызываем реальный метод, который будет использовать openhtmltopdf
        byte[] pdfBytes = ticketPdfService.generatePdf(testTicket);

        // then: проверяем, что массив не пустой
        assertThat(pdfBytes).isNotNull().isNotEmpty();

        // Проверяем, что это действительно PDF (первые 4 байта файла PDF всегда "%PDF")
        String pdfHeader = new String(pdfBytes, 0, 4);
        assertThat(pdfHeader).isEqualTo("%PDF");
    }

    @Test
    @DisplayName("generatePdf() should throw RuntimeException when template engine fails")
    void generatePdf_shouldThrowException_whenTemplateFails() {
        // given: мокаем Thymeleaf, чтобы он выбросил ошибку (например, шаблон не найден)
        when(templateEngine.process(eq("pdf/ticket"), any(Context.class)))
                .thenThrow(new RuntimeException("Template error"));

        // when & then: проверяем, что сервис обернул ошибку в RuntimeException
        assertThatThrownBy(() -> ticketPdfService.generatePdf(testTicket))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Не удалось сгенерировать PDF");
    }
}