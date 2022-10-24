package com.trading.journal.entry.entries;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EntryByTypeValidatorTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @DisplayName("Entry is valid for TRADE type")
    @Test
    void tradeValid() {
        Entry entry = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .direction(EntryDirection.LONG)
                .type(EntryType.TRADE)
                .symbol("MSFT")
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure(1)
                .build();
        Set<ConstraintViolation<Object>> violations = validator.validate(entry);
        assertThat(violations).hasSize(0);
    }

    @DisplayName("Entry is invalid for TRADE type")
    @Test
    void tradeInvalid() {
        Entry entry = Entry.builder()
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(200))
                .symbol("MSFT")
                .direction(EntryDirection.LONG)
                .size(BigDecimal.valueOf(2))
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure(1)
                .build();
        Set<ConstraintViolation<Object>> violations = validator.validate(entry);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Date is required");

        entry = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .price(BigDecimal.valueOf(200))
                .symbol("MSFT")
                .direction(EntryDirection.LONG)
                .size(BigDecimal.valueOf(2))
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure(1)
                .build();
        violations = validator.validate(entry);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Entry type is required");

        entry = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .type(EntryType.TRADE)
                .symbol("MSFT")
                .direction(EntryDirection.LONG)
                .size(BigDecimal.valueOf(2))
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure(1)
                .build();
        violations = validator.validate(entry);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Price is required");

        entry = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(-200))
                .symbol("MSFT")
                .direction(EntryDirection.LONG)
                .size(BigDecimal.valueOf(2))
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure(1)
                .build();
        violations = validator.validate(entry);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Price must be positive");

        entry = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .type(EntryType.TRADE)
                .price(BigDecimal.ZERO)
                .symbol("MSFT")
                .direction(EntryDirection.LONG)
                .size(BigDecimal.valueOf(2))
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure(1)
                .build();
        violations = validator.validate(entry);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Price must be positive");

        entry = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(200))
                .direction(EntryDirection.LONG)
                .size(BigDecimal.valueOf(2))
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure(1)
                .build();
        violations = validator.validate(entry);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Symbol is required");

        entry = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(200))
                .symbol("MSFT")
                .size(BigDecimal.valueOf(2))
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure(1)
                .build();
        violations = validator.validate(entry);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Direction is required");

        entry = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(200))
                .symbol("MSFT")
                .direction(EntryDirection.LONG)
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure(1)
                .build();
        violations = validator.validate(entry);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Position size is required");

        entry = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(200))
                .symbol("MSFT")
                .direction(EntryDirection.LONG)
                .size(BigDecimal.valueOf(-2))
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure(1)
                .build();
        violations = validator.validate(entry);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Position size must be positive");

        entry = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(200))
                .symbol("MSFT")
                .direction(EntryDirection.LONG)
                .size(BigDecimal.ZERO)
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure(1)
                .build();
        violations = validator.validate(entry);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Position size must be positive");

        entry = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(200))
                .symbol("MSFT")
                .direction(EntryDirection.LONG)
                .size(BigDecimal.valueOf(200))
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure(-1)
                .build();
        violations = validator.validate(entry);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Graph Measure must be positive");
    }

    @DisplayName("Entry is valid for Withdrawal, Deposit and Taxes type")
    @Test
    void otherValid() {
        Entry entry = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .price(BigDecimal.valueOf(200))
                .type(EntryType.WITHDRAWAL)
                .build();
        Set<ConstraintViolation<Object>> violations = validator.validate(entry);
        assertThat(violations).hasSize(0);

        entry = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .price(BigDecimal.valueOf(200))
                .type(EntryType.DEPOSIT)
                .build();
        violations = validator.validate(entry);
        assertThat(violations).hasSize(0);

        entry = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .price(BigDecimal.valueOf(200))
                .type(EntryType.TAXES)
                .build();
        violations = validator.validate(entry);
        assertThat(violations).hasSize(0);
    }

    @DisplayName("Entry is invalid for Withdrawal type")
    @Test
    void invalidWithdrawal() {
        Entry entry = Entry.builder()
                .price(BigDecimal.valueOf(200))
                .type(EntryType.WITHDRAWAL)
                .build();
        Set<ConstraintViolation<Object>> violations = validator.validate(entry);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Date is required");

        entry = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .type(EntryType.WITHDRAWAL)
                .build();
        violations = validator.validate(entry);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Price is required");

        entry = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .price(BigDecimal.valueOf(-200))
                .type(EntryType.WITHDRAWAL)
                .build();
        violations = validator.validate(entry);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Price must be positive");

        entry = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .price(BigDecimal.ZERO)
                .type(EntryType.WITHDRAWAL)
                .build();
        violations = validator.validate(entry);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Price must be positive");
    }

    @DisplayName("Entry is invalid for Deposit type")
    @Test
    void invalidDeposit() {
        Entry entry = Entry.builder()
                .price(BigDecimal.valueOf(200))
                .type(EntryType.DEPOSIT)
                .build();
        Set<ConstraintViolation<Object>> violations = validator.validate(entry);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Date is required");

        entry = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .type(EntryType.DEPOSIT)
                .build();
        violations = validator.validate(entry);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Price is required");

        entry = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .price(BigDecimal.valueOf(-200))
                .type(EntryType.DEPOSIT)
                .build();
        violations = validator.validate(entry);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Price must be positive");

        entry = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .price(BigDecimal.ZERO)
                .type(EntryType.DEPOSIT)
                .build();
        violations = validator.validate(entry);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Price must be positive");
    }

    @DisplayName("Entry is invalid for Taxes type")
    @Test
    void invalidTaxes() {
        Entry entry = Entry.builder()
                .price(BigDecimal.valueOf(200))
                .type(EntryType.TAXES)
                .build();
        Set<ConstraintViolation<Object>> violations = validator.validate(entry);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Date is required");

        entry = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .type(EntryType.TAXES)
                .build();
        violations = validator.validate(entry);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Price is required");

        entry = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .price(BigDecimal.valueOf(-200))
                .type(EntryType.TAXES)
                .build();
        violations = validator.validate(entry);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Price must be positive");

        entry = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .price(BigDecimal.ZERO)
                .type(EntryType.TAXES)
                .build();
        violations = validator.validate(entry);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Price must be positive");
    }
}