package com.trading.journal.entry.configuration;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.ResponseEntity.status;

@RestControllerAdvice
@Slf4j
@NoArgsConstructor
public class ApiExceptionHandler {
    private static final String CLIENT_EXCEPTION_HAPPENED = "Client Exception happened";
    private static final String UNEXPECTED_EXCEPTION_HAPPENED = "Unexpected Exception happened";

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Map<String, String>> handleClientException(final HttpClientErrorException ex) {
        log.error(CLIENT_EXCEPTION_HAPPENED, ex);
        final Map<String, String> errors = new ConcurrentHashMap<>();
        errors.put("error", ex.getStatusText());
        return status(ex.getStatusCode()).body(errors);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(final MaxUploadSizeExceededException ex) {
        log.error(CLIENT_EXCEPTION_HAPPENED, ex);
        final Map<String, String> errors = extractMessage(ex);
        return status(BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthenticationException(final AuthenticationException ex) {
        return status(UNAUTHORIZED).body(extractMessage(ex));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<Map<String, List<String>>> handleWebExchangeBindException(final WebExchangeBindException ex) {
        return status(BAD_REQUEST).body(getBindingResult(ex.getBindingResult()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, List<String>>> handleMethodArgumentNotValidException(final MethodArgumentNotValidException ex) {
        return status(BAD_REQUEST).body(getBindingResult(ex.getBindingResult()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(final Exception ex) {
        return status(INTERNAL_SERVER_ERROR).body(extractMessage(ex));
    }

    private Map<String, List<String>> getBindingResult(final BindingResult bindingResult) {
        final List<String> errors = new ArrayList<>();
        for (final ObjectError error : bindingResult.getAllErrors()) {
            errors.add(error.getDefaultMessage());
        }
        final Map<String, List<String>> errorsResponse = new ConcurrentHashMap<>();
        errorsResponse.put("errors", errors);
        return errorsResponse;
    }

    private Map<String, String> extractMessage(Exception exception) {
        final String message = ExceptionUtils.getRootCause(exception).getMessage();
        log.error(UNEXPECTED_EXCEPTION_HAPPENED, exception);
        final Map<String, String> errors = new ConcurrentHashMap<>();
        errors.put("error", Optional.ofNullable(message).orElse(UNEXPECTED_EXCEPTION_HAPPENED));
        return errors;
    }
}
