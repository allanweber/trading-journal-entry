package com.trading.journal.entry;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationExceptionTest {

    @DisplayName("Given exception only with message the status must be Bad Request")
    @Test
    void badRequest() {
        ApplicationException exception = new ApplicationException("any message");

        assertThat(exception.getStatusText()).isEqualTo("any message");
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @DisplayName("Given exception with message and status the status must be Conflict")
    @Test
    void conflict() {
        ApplicationException exception = new ApplicationException(HttpStatus.CONFLICT, "any message");

        assertThat(exception.getStatusText()).isEqualTo("any message");
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}