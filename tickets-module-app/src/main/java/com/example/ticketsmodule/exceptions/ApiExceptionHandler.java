package com.example.ticketsmodule.exceptions;

import com.example.ticketsmodule.api.model.ErrorResponse;
import com.example.ticketsmodule.domain.carrier.exception.CarrierNameAlreadyExistsException;
import com.example.ticketsmodule.domain.carrier.exception.CarrierNotFoundException;
import com.example.ticketsmodule.domain.order.exception.OrderAccessDeniedException;
import com.example.ticketsmodule.domain.order.exception.OrderExpiredException;
import com.example.ticketsmodule.domain.order.exception.OrderInvalidStatusException;
import com.example.ticketsmodule.domain.order.exception.TicketNotAvailableException;
import com.example.ticketsmodule.domain.route.exception.RouteNotFoundException;
import com.example.ticketsmodule.domain.ticket.exception.TicketAlreadyPurchasedException;
import com.example.ticketsmodule.domain.ticket.exception.TicketNotFoundException;
import com.example.ticketsmodule.domain.user.exception.UserNotFoundException;
import com.example.ticketsmodule.impl.common.web.rest.exceptions.ApiException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class ApiExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public ErrorResponse handleAccessDenied(AccessDeniedException ex) {
        ErrorResponse response = new ErrorResponse();
        response.setMessage("Access Denied");
        return response;
    }

    @ExceptionHandler({
            EntityNotFoundException.class,
            CarrierNotFoundException.class,
            RouteNotFoundException.class,
            TicketNotFoundException.class,
            UserNotFoundException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorResponse handleNotFound(Exception ex) {
        log.error(ex.getMessage(), ex);
        ErrorResponse response = new ErrorResponse();
        response.setMessage(ex.getMessage());
        response.setUserMessage("Ресурс не найден");
        return response;
    }

    @ExceptionHandler(TicketAlreadyPurchasedException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    public ErrorResponse handleTicketAlreadyPurchased(TicketAlreadyPurchasedException ex) {
        ErrorResponse body = new ErrorResponse();
        body.setMessage(ex.getMessage());
        body.setUserMessage("Билет уже куплен");
        return body;
    }

    @ExceptionHandler(CarrierNameAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleDuplicateCarrier(CarrierNameAlreadyExistsException ex) {
        ErrorResponse body = new ErrorResponse();
        body.setMessage(ex.getMessage());
        body.setUserMessage("Перевозчик с таким названием уже существует");
        return body;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleDataIntegrity(DataIntegrityViolationException ex) {
        log.error(ex.getMessage(), ex);
        ErrorResponse response = new ErrorResponse();
        response.setMessage(ex.getMessage());
        response.setUserMessage("Нарушение уникальности данных");
        return response;
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ErrorResponse handleMethodArgumentNotValidException(
            HttpServletRequest request, HttpServletResponse response, Exception exception) {
        log.error(exception.getMessage(), exception);

        request.setAttribute("exception", exception);
        response.setStatus(getStatus(exception).value());

        ErrorResponse responseDto = new ErrorResponse();
        responseDto.setMessage(exception.getMessage());
        if (exception instanceof ApiException apiEx) {
            responseDto.setUserMessage(apiEx.getUserMessage());
        }
        return responseDto;
    }

    @ExceptionHandler(TicketNotAvailableException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ErrorResponse handleTicketNotAvailable(TicketNotAvailableException ex) {
        ErrorResponse body = new ErrorResponse();
        body.setMessage(ex.getMessage());
        body.setUserMessage("Билет недоступен для покупки");
        return body;
    }

    @ExceptionHandler({OrderInvalidStatusException.class, OrderExpiredException.class})
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    public ErrorResponse handleOrderInvalidState(RuntimeException ex) {
        ErrorResponse body = new ErrorResponse();
        body.setMessage(ex.getMessage());
        body.setUserMessage("Заказ нельзя обработать в текущем статусе");
        return body;
    }

    @ExceptionHandler(OrderAccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public ErrorResponse handleOrderAccessDenied(OrderAccessDeniedException ex) {
        ErrorResponse body = new ErrorResponse();
        body.setMessage(ex.getMessage());
        body.setUserMessage("Нет доступа к заказу");
        return body;
    }

    private HttpStatus getStatus(Exception exception) {
        if (exception instanceof ApiException) {
            return HttpStatus.UNPROCESSABLE_ENTITY;
        }

        if (exception instanceof HttpRequestMethodNotSupportedException) {
            return HttpStatus.METHOD_NOT_ALLOWED;
        }
        if (exception instanceof HttpMediaTypeNotSupportedException) {
            return HttpStatus.UNSUPPORTED_MEDIA_TYPE;
        }
        if (exception instanceof HttpMediaTypeNotAcceptableException) {
            return HttpStatus.NOT_ACCEPTABLE;
        }
        if (exception instanceof MissingPathVariableException) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        if (exception instanceof MissingServletRequestParameterException) {
            return HttpStatus.BAD_REQUEST;
        }
        if (exception instanceof ServletRequestBindingException) {
            return HttpStatus.BAD_REQUEST;
        }
        if (exception instanceof ConversionNotSupportedException) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        if (exception instanceof TypeMismatchException) {
            return HttpStatus.BAD_REQUEST;
        }
        if (exception instanceof HttpMessageNotReadableException) {
            return HttpStatus.BAD_REQUEST;
        }
        if (exception instanceof HttpMessageNotWritableException) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        if (exception instanceof MethodArgumentNotValidException) {
            return HttpStatus.BAD_REQUEST;
        }
        if (exception instanceof MissingServletRequestPartException) {
            return HttpStatus.BAD_REQUEST;
        }
        if (exception instanceof BindException) {
            return HttpStatus.BAD_REQUEST;
        }
        if (exception instanceof NoHandlerFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        if (exception instanceof AsyncRequestTimeoutException) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }

        if (exception instanceof IllegalArgumentException) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}