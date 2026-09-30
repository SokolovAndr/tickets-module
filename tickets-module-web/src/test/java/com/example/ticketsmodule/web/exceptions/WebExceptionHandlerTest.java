package com.example.ticketsmodule.web.exceptions;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit-тесты для {@link WebExceptionHandler}.
 *
 * <p>Обработчик простой — тестируем напрямую через {@code new},
 * без Spring-контекста.
 */
class WebExceptionHandlerTest {

    private final WebExceptionHandler handler = new WebExceptionHandler();
    private final Model model = new ExtendedModelMap();

    @Test
    @DisplayName("EntityNotFoundException → view 'error/404' + сообщение в модели")
    void handleNotFound_ShouldReturn404View() {
        EntityNotFoundException ex = new EntityNotFoundException("Test");

        String view = handler.handleNotFound(ex, model);

        assertEquals("error/404", view);
        assertEquals("Ресурс не найден", model.getAttribute("message"));
    }

    @Test
    @DisplayName("RuntimeException → view 'error/500' + сообщение в модели")
    void handleGeneric_ShouldReturn500View() {
        RuntimeException ex = new RuntimeException("boom");

        String view = handler.handleGeneric(ex, model);

        assertEquals("error/500", view);
        assertEquals("Что-то пошло не так", model.getAttribute("message"));
    }
}