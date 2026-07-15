package com.example.ticketsmoduleimpl.common.web.rest.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import tickets.model.ErrorResponse;

@Slf4j
@RestControllerAdvice
public class GlobalControllerExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleConversion(RuntimeException ex) {
        ErrorResponse response = new ErrorResponse();
        response.setMessage(ex.getMessage());
        log.error(ex.getMessage(), ex);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public ErrorResponse handleAccessDenied(AccessDeniedException ex) {
        ErrorResponse response = new ErrorResponse();
        response.setMessage("Access Denied");
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
        if (exception instanceof ApiException) {
            responseDto.setUserMessage(((ApiException) exception).getUserMessage());
        }
        return responseDto;
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
