package com.example.ticketsmodule.impl.tickets.service;

import com.example.ticketsmodule.impl.tickets.domain.TicketEntity;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FontStyle;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.util.XRLog;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketPdfService {

    private final TemplateEngine templateEngine;

    /**
     * Отключаем навязчивые красные логи
     */
    @PostConstruct
    public void init() {
        XRLog.setLoggingEnabled(false);
    }

    public byte[] generatePdf(TicketEntity ticket) {
        try {
            Context context = new Context();
            context.setVariable("ticket", ticket);
            String html = templateEngine.process("pdf/ticket", context);

            byte[] fontRegular = readResource("/fonts/NotoSans-Regular.ttf");
            byte[] fontBold = readResource("/fonts/NotoSans-Bold.ttf");

            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                PdfRendererBuilder builder = new PdfRendererBuilder();

                builder.useFont(() -> new ByteArrayInputStream(fontRegular),
                        "NotoSans", 400, FontStyle.NORMAL, true);

                builder.useFont(() -> new ByteArrayInputStream(fontBold),
                        "NotoSans", 700, FontStyle.NORMAL, true);

                builder.withHtmlContent(html, "file:///");
                builder.toStream(os);
                builder.run();

                return os.toByteArray();
            }
        } catch (Exception e) {
            log.error("Ошибка при генерации PDF для билета {}", ticket.getId(), e);
            throw new RuntimeException("Не удалось сгенерировать PDF", e);
        }
    }

    private byte[] readResource(String path) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) throw new RuntimeException("Файл не найден в classpath: " + path);
            return is.readAllBytes();
        }
    }
}