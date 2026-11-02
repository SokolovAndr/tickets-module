package com.example.ticketsmodule.exceptions;

import com.example.ticketsmodule.api.model.ErrorResponse;
import com.example.ticketsmodule.domain.carrier.exception.CarrierNameAlreadyExistsException;
import com.example.ticketsmodule.domain.carrier.exception.CarrierNotFoundException;
import com.example.ticketsmodule.domain.ticket.exception.TicketAlreadyPurchasedException;
import com.example.ticketsmodule.impl.common.web.rest.exceptions.ApiException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private ApiExceptionHandler handler;

    @Test
    void handleAccessDenied_ShouldReturnForbidden() {
        ErrorResponse result = handler.handleAccessDenied(new AccessDeniedException("denied"));
        assertEquals("Access Denied", result.getMessage());
    }

    @Test
    void handleNotFound_ShouldReturnNotFound() {
        ErrorResponse result1 = handler.handleNotFound(new EntityNotFoundException("not found"));
        assertEquals("not found", result1.getMessage());
        assertEquals("Ресурс не найден", result1.getUserMessage());

        ErrorResponse result2 = handler.handleNotFound(new CarrierNotFoundException(UUID.randomUUID()));
        assertEquals("Ресурс не найден", result2.getUserMessage());
    }

    @Test
    void handleDataIntegrity_ShouldReturnBadRequest() {
        ErrorResponse result = handler.handleDataIntegrity(new DataIntegrityViolationException("violation"));
        assertEquals("violation", result.getMessage());
        assertEquals("Нарушение уникальности данных", result.getUserMessage());
    }

    @Test
    void handleDuplicateCarrier_ShouldReturnBadRequest() {
        ErrorResponse result = handler.handleDuplicateCarrier(new CarrierNameAlreadyExistsException("Test"));
        assertEquals("Перевозчик с таким названием уже существует", result.getUserMessage());
    }

    @Test
    void handleTicketAlreadyPurchased_ShouldReturnUnprocessableEntity() {
        ErrorResponse result = handler.handleTicketAlreadyPurchased(new TicketAlreadyPurchasedException(UUID.randomUUID()));
        assertEquals("Билет уже куплен", result.getUserMessage());
    }

    @Test
    void handleMethodArgumentNotValidException_WithApiException_ShouldSetCustomUserMessageAnd422() {
        ApiException ex = new ApiException("Server error", "User message");

        ErrorResponse result = handler.handleMethodArgumentNotValidException(request, response, ex);

        assertEquals("Server error", result.getMessage());
        assertEquals("User message", result.getUserMessage());
        verify(response).setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }

    @Test
    void handleMethodArgumentNotValidException_WithStandardException_ShouldSet500() {
        Exception ex = new Exception("Generic error");

        ErrorResponse result = handler.handleMethodArgumentNotValidException(request, response, ex);

        assertEquals("Generic error", result.getMessage());
        verify(response).setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    void getStatus_ShouldCoverAllBranches() {
        // Проверяем все ветки метода getStatus через основной обработчик
        Stream.of(
                mock(org.springframework.web.HttpRequestMethodNotSupportedException.class),
                mock(org.springframework.web.HttpMediaTypeNotSupportedException.class),
                mock(org.springframework.web.HttpMediaTypeNotAcceptableException.class),
                mock(org.springframework.web.bind.MissingPathVariableException.class),
                mock(org.springframework.web.bind.MissingServletRequestParameterException.class),
                mock(org.springframework.web.bind.ServletRequestBindingException.class),
                mock(org.springframework.beans.ConversionNotSupportedException.class),
                mock(org.springframework.beans.TypeMismatchException.class),
                mock(org.springframework.http.converter.HttpMessageNotReadableException.class),
                mock(org.springframework.http.converter.HttpMessageNotWritableException.class),
                mock(org.springframework.web.bind.MethodArgumentNotValidException.class),
                mock(org.springframework.web.multipart.support.MissingServletRequestPartException.class),
                mock(org.springframework.validation.BindException.class),
                mock(org.springframework.web.servlet.NoHandlerFoundException.class),
                mock(org.springframework.web.context.request.async.AsyncRequestTimeoutException.class),
                mock(IllegalArgumentException.class)
        ).forEach(ex -> {
            reset(response);
            handler.handleMethodArgumentNotValidException(request, response, ex);
        });
    }
}