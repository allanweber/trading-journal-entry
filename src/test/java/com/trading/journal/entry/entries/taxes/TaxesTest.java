package com.trading.journal.entry.entries.taxes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TaxesTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @DisplayName("Taxes is valid")
    @Test
    void valid() {
        Taxes taxes = Taxes.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .price(BigDecimal.valueOf(200))
                .build();
        Set<ConstraintViolation<Object>> violations = validator.validate(taxes);
        assertThat(violations).hasSize(0);
    }

    @DisplayName("Taxes is invalid")
    @Test
    void invalid() {
        Taxes taxes = Taxes.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .build();
        Set<ConstraintViolation<Object>> violations = validator.validate(taxes);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Price is required");

        taxes = Taxes.builder()
                .price(BigDecimal.valueOf(200))
                .build();
        violations = validator.validate(taxes);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Date is required");
    }
}