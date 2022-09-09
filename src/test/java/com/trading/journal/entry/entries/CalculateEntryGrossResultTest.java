package com.trading.journal.entry.entries;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

class CalculateEntryGrossResultTest {

    @DisplayName("Do not calculate gross result because there is no exit price")
    @Test
    void noCalcNoExit() {
        Entry entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .build();
        BigDecimal balance = BigDecimal.valueOf(1000);

        Entry calculated = new CalculateEntry(entry, balance).calculate();

        assertThat(calculated.getGrossResult()).isNull();
    }

    @DisplayName("Calculate gross result for LONGs and SHORTs winning")
    @Test
    void winning() {
        Entry entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .exitPrice(BigDecimal.valueOf(240))
                .build();
        BigDecimal balance = BigDecimal.valueOf(1000);

        Entry calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getGrossResult()).isEqualTo(BigDecimal.valueOf(80.00).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(33.15))
                .size(BigDecimal.valueOf(7))
                .exitPrice(BigDecimal.valueOf(38.56))
                .build();
        balance = BigDecimal.valueOf(526);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getGrossResult()).isEqualTo(BigDecimal.valueOf(37.87).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .exitPrice(BigDecimal.valueOf(0.87))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getGrossResult()).isEqualTo(BigDecimal.valueOf(37.00).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(523.68))
                .size(BigDecimal.valueOf(15))
                .exitPrice(BigDecimal.valueOf(495.37))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getGrossResult()).isEqualTo(BigDecimal.valueOf(424.65).setScale(2, RoundingMode.HALF_EVEN));
    }

    @DisplayName("Calculate gross result for LONGs and SHORTs losing")
    @Test
    void losing() {
        Entry entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .exitPrice(BigDecimal.valueOf(175))
                .build();
        BigDecimal balance = BigDecimal.valueOf(1000);

        Entry calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getGrossResult()).isEqualTo(BigDecimal.valueOf(-50.00).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(33.15))
                .size(BigDecimal.valueOf(7))
                .exitPrice(BigDecimal.valueOf(27.83))
                .build();
        balance = BigDecimal.valueOf(526);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getGrossResult()).isEqualTo(BigDecimal.valueOf(-37.24).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .exitPrice(BigDecimal.valueOf(1.64))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getGrossResult()).isEqualTo(BigDecimal.valueOf(-40.00).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(523.68))
                .size(BigDecimal.valueOf(15))
                .exitPrice(BigDecimal.valueOf(601.09))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getGrossResult()).isEqualTo(BigDecimal.valueOf(-1161.15).setScale(2, RoundingMode.HALF_EVEN));
    }

    @DisplayName("Gross result with ZERO Balance")
    @Test
    void grossWithZeroBalance() {
        BigDecimal balance = BigDecimal.ZERO;

        Entry entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .exitPrice(BigDecimal.valueOf(240))
                .build();

        Entry calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getGrossResult()).isEqualTo(BigDecimal.valueOf(80.00).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .exitPrice(BigDecimal.valueOf(0.87))
                .build();
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getGrossResult()).isEqualTo(BigDecimal.valueOf(37.00).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(33.15))
                .size(BigDecimal.valueOf(7))
                .exitPrice(BigDecimal.valueOf(27.83))
                .build();
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getGrossResult()).isEqualTo(BigDecimal.valueOf(-37.24).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .exitPrice(BigDecimal.valueOf(1.64))
                .build();
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getGrossResult()).isEqualTo(BigDecimal.valueOf(-40.00).setScale(2, RoundingMode.HALF_EVEN));
    }

    @DisplayName("Gross result with Negative Balance")
    @Test
    void grossWithNegativeBalance() {
        BigDecimal balance = BigDecimal.valueOf(-1000);

        Entry entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .exitPrice(BigDecimal.valueOf(240))
                .build();

        Entry calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getGrossResult()).isEqualTo(BigDecimal.valueOf(80.00).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .exitPrice(BigDecimal.valueOf(0.87))
                .build();
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getGrossResult()).isEqualTo(BigDecimal.valueOf(37.00).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(33.15))
                .size(BigDecimal.valueOf(7))
                .exitPrice(BigDecimal.valueOf(27.83))
                .build();
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getGrossResult()).isEqualTo(BigDecimal.valueOf(-37.24).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .exitPrice(BigDecimal.valueOf(1.64))
                .build();
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getGrossResult()).isEqualTo(BigDecimal.valueOf(-40.00).setScale(2, RoundingMode.HALF_EVEN));
    }
}