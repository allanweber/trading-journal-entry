package com.trading.journal.entry.entries;

import com.trading.journal.entry.entries.trade.Trade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ValidTradeValidatorTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @DisplayName("Trade is valid")
    @Test
    void tradeValid() {
        Trade trade = Trade.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .direction(EntryDirection.LONG)
                .symbol("MSFT")
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure("1")
                .build();
        Set<ConstraintViolation<Object>> violations = validator.validate(trade);
        assertThat(violations).hasSize(0);

        trade = Trade.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .direction(EntryDirection.LONG)
                .symbol("MSFT")
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure("1")
                .exitDate(LocalDateTime.of(2022, 9, 20, 15, 30, 51))
                .exitPrice(BigDecimal.valueOf(200))
                .build();
        violations = validator.validate(trade);
        assertThat(violations).hasSize(0);
    }

    @DisplayName("Trade is invalid")
    @Test
    void tradeInvalid() {
        Trade trade = Trade.builder()
                .price(BigDecimal.valueOf(200))
                .symbol("MSFT")
                .direction(EntryDirection.LONG)
                .size(BigDecimal.valueOf(2))
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure("1")
                .build();
        Set<ConstraintViolation<Object>> violations = validator.validate(trade);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Date is required");

        trade = Trade.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .symbol("MSFT")
                .direction(EntryDirection.LONG)
                .size(BigDecimal.valueOf(2))
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure("1")
                .build();
        violations = validator.validate(trade);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Price is required");

        trade = Trade.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .price(BigDecimal.valueOf(-200))
                .symbol("MSFT")
                .direction(EntryDirection.LONG)
                .size(BigDecimal.valueOf(2))
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure("1")
                .build();
        violations = validator.validate(trade);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Price must be positive");

        trade = Trade.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .price(BigDecimal.ZERO)
                .symbol("MSFT")
                .direction(EntryDirection.LONG)
                .size(BigDecimal.valueOf(2))
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure("1")
                .build();
        violations = validator.validate(trade);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Price must be positive");

        trade = Trade.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .price(BigDecimal.valueOf(200))
                .direction(EntryDirection.LONG)
                .size(BigDecimal.valueOf(2))
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure("1")
                .build();
        violations = validator.validate(trade);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Symbol is required");

        trade = Trade.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .price(BigDecimal.valueOf(200))
                .symbol("MSFT")
                .size(BigDecimal.valueOf(2))
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure("1")
                .build();
        violations = validator.validate(trade);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Direction is required");

        trade = Trade.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .price(BigDecimal.valueOf(200))
                .symbol("MSFT")
                .direction(EntryDirection.LONG)
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure("1")
                .build();
        violations = validator.validate(trade);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Position size is required");

        trade = Trade.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .price(BigDecimal.valueOf(200))
                .symbol("MSFT")
                .direction(EntryDirection.LONG)
                .size(BigDecimal.valueOf(-2))
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure("1")
                .build();
        violations = validator.validate(trade);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Position size must be positive");

        trade = Trade.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .price(BigDecimal.valueOf(200))
                .symbol("MSFT")
                .direction(EntryDirection.LONG)
                .size(BigDecimal.ZERO)
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure("1")
                .build();
        violations = validator.validate(trade);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Position size must be positive");
    }

    @DisplayName("Trade is valid when exiting")
    @Test
    void tradeInvalidExit() {
        Trade trade = Trade.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .direction(EntryDirection.LONG)
                .symbol("MSFT")
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .exitPrice(BigDecimal.valueOf(2))
                .build();
        Set<ConstraintViolation<Object>> violations = validator.validate(trade);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Exit Date is required");

        trade = Trade.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .direction(EntryDirection.LONG)
                .symbol("MSFT")
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .exitDate(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .build();
        violations = validator.validate(trade);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Exit Price is required");

        trade = Trade.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .direction(EntryDirection.LONG)
                .symbol("MSFT")
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .exitDate(LocalDateTime.of(2022, 9, 20, 15, 30, 49))
                .exitPrice(BigDecimal.valueOf(2))
                .build();
        violations = validator.validate(trade);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Exit date must be after trade date");
    }
}