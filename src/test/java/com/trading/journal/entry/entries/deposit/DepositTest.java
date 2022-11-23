package com.trading.journal.entry.entries.deposit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DepositTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @DisplayName("Deposit is valid")
    @Test
    void valid() {
        Deposit withdrawal = Deposit.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .price(BigDecimal.valueOf(200))
                .build();
        Set<ConstraintViolation<Object>> violations = validator.validate(withdrawal);
        assertThat(violations).hasSize(0);
    }

    @DisplayName("Deposit is invalid")
    @Test
    void invalid() {
        Deposit withdrawal = Deposit.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .build();
        Set<ConstraintViolation<Object>> violations = validator.validate(withdrawal);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Price is required");

        withdrawal = Deposit.builder()
                .price(BigDecimal.valueOf(200))
                .build();
        violations = validator.validate(withdrawal);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Date is required");
    }
}