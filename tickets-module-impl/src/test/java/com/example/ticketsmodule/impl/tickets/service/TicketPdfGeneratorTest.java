package com.example.ticketsmodule.impl.tickets.service;

import com.example.ticketsmodule.domain.ticket.model.Ticket;
import com.example.ticketsmodule.impl.tickets.pdf.TicketPdfGenerator;
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
class TicketPdfGeneratorTest {

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private TicketPdfGenerator ticketPdfGenerator;

    private Ticket testTicket;

    @BeforeEach
    void setUp() {
        testTicket = Ticket.builder()
                .id(UUID.randomUUID())
                .seatNumber(1)
                .build();
    }

    @Test
    @DisplayName("generatePdf() should return valid PDF bytes on success")
    void generatePdf_shouldReturnPdfBytes() {
        String mockHtml = "<html><body><h1>Test Ticket</h1></body></html>";
        when(templateEngine.process(eq("pdf/ticket"), any(Context.class)))
                .thenReturn(mockHtml);

        byte[] pdfBytes = ticketPdfGenerator.generatePdf(testTicket);

        assertThat(pdfBytes).isNotNull().isNotEmpty();

        String pdfHeader = new String(pdfBytes, 0, 4);
        assertThat(pdfHeader).isEqualTo("%PDF");
    }

    @Test
    @DisplayName("generatePdf() should throw RuntimeException when template engine fails")
    void generatePdf_shouldThrowException_whenTemplateFails() {
        when(templateEngine.process(eq("pdf/ticket"), any(Context.class)))
                .thenThrow(new RuntimeException("Template error"));

        assertThatThrownBy(() -> ticketPdfGenerator.generatePdf(testTicket))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Не удалось сгенерировать PDF");
    }
}