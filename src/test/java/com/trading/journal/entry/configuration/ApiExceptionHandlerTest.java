package com.trading.journal.entry.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.client.HttpClientErrorException;

import java.lang.reflect.Executable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApiExceptionHandlerTest {
    final ApiExceptionHandler apiExceptionHandler = new ApiExceptionHandler();

    @Test
    @DisplayName("When handle HttpClientErrorException with no message, return status as message")
    void handleClientExceptionNoMessage() {
        ResponseEntity<Map<String, String>> response = apiExceptionHandler.handleClientException(
                new HttpClientErrorException(HttpStatus.NOT_FOUND));
        assertEquals(404, response.getStatusCodeValue());
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey("error"));
        assertEquals("NOT_FOUND", Objects.requireNonNull(response.getBody()).get("error"));
    }

    @Test
    @DisplayName("When handle HttpClientErrorException message, return message")
    void handleClientExceptionWithMessage() {
        ResponseEntity<Map<String, String>> response = apiExceptionHandler.handleClientException(
                new HttpClientErrorException(HttpStatus.BAD_REQUEST, "any message"));
        assertEquals(400, response.getStatusCodeValue());
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey("error"));
        assertEquals("any message", Objects.requireNonNull(response.getBody()).get("error"));
    }

    @Test
    @DisplayName("When handle Exception, return message and status 500")
    void handleException() {
        ResponseEntity<Map<String, String>> response = apiExceptionHandler
                .handleException(new RuntimeException("any message"));
        assertEquals(500, response.getStatusCodeValue());
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey("error"));
        assertEquals("any message", Objects.requireNonNull(response.getBody()).get("error"));
    }

    @Test
    @DisplayName("When handle Exception with cause, return cause as message and status 500")
    void handleExceptionWithCause() {
        RuntimeException causeMessage = new RuntimeException("cause message");
        ResponseEntity<Map<String, String>> response = apiExceptionHandler
                .handleException(new RuntimeException("any message", causeMessage));
        assertEquals(500, response.getStatusCodeValue());
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey("error"));
        assertEquals("cause message", Objects.requireNonNull(response.getBody()).get("error"));
    }

    @Test
    @DisplayName("When handle AuthenticationException return message and status 401")
    void handleAuthenticationException() {
        AuthenticationException exception = new AuthenticationCredentialsNotFoundException("message");
        ResponseEntity<Map<String, String>> response = apiExceptionHandler.handleAuthenticationException(exception);
        assertEquals(401, response.getStatusCodeValue());
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey("error"));
        assertEquals("message", Objects.requireNonNull(response.getBody()).get("error"));
    }

    @Test
    @DisplayName("When handle MethodArgumentNotValidException, return message for FieldError")
    void handleMethodArgumentNotValidExceptionWithFieldError() {
        FieldError fieldError = mock(FieldError.class);
        when(fieldError.getField()).thenReturn("name");
        when(fieldError.getDefaultMessage()).thenReturn("default message");
        when(fieldError.getCodes()).thenReturn(new String[]{"message"});

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(fieldError));

        MethodParameter methodParameter = mock(MethodParameter.class);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<Map<String, List<String>>> response = apiExceptionHandler.handleMethodArgumentNotValidException(ex);

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey("errors"));
        assertEquals("default message", Objects.requireNonNull(response.getBody()).get("errors").get(0));
    }
}