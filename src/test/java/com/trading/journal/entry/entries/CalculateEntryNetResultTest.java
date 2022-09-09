package com.trading.journal.entry.entries;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

class CalculateEntryNetResultTest {

    @DisplayName("Do not calculate net result because there is no exit price")
    @Test
    void noCalcNoExit() {
        Entry entry = Entry.builder()
                .direction(EntryDirection.LONG)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .build();
        BigDecimal balance = BigDecimal.valueOf(1000);

        Entry calculated = new CalculateEntry(entry, balance).calculate();

        assertThat(calculated.getGrossResult()).isNull();
    }

    @DisplayName("Calculate net result for LONGs and SHORTs winning")
    @Test
    void winning() {
        Entry entry = Entry.builder()
                .direction(EntryDirection.LONG)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .exitPrice(BigDecimal.valueOf(240))
                .build();
        BigDecimal balance = BigDecimal.valueOf(1000);

        Entry calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getNetResult()).isEqualTo(BigDecimal.valueOf(80.00).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder()
                .direction(EntryDirection.LONG)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(33.15))
                .size(BigDecimal.valueOf(7))
                .exitPrice(BigDecimal.valueOf(38.56))
                .build();
        balance = BigDecimal.valueOf(526);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getNetResult()).isEqualTo(BigDecimal.valueOf(37.87).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder()
                .direction(EntryDirection.SHORT)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .exitPrice(BigDecimal.valueOf(0.87))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getNetResult()).isEqualTo(BigDecimal.valueOf(37.00).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder()
                .direction(EntryDirection.SHORT)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(523.68))
                .size(BigDecimal.valueOf(15))
                .exitPrice(BigDecimal.valueOf(495.37))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getNetResult()).isEqualTo(BigDecimal.valueOf(424.65).setScale(2, RoundingMode.HALF_EVEN));
    }

    @DisplayName("Calculate net result for LONGs and SHORTs winning with costs")
    @Test
    void winningWithCosts() {
        Entry entry = Entry.builder()
                .direction(EntryDirection.LONG)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .exitPrice(BigDecimal.valueOf(240))
                .costs(BigDecimal.valueOf(5.59))
                .build();
        BigDecimal balance = BigDecimal.valueOf(1000);

        Entry calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getNetResult()).isEqualTo(BigDecimal.valueOf(74.41).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder()
                .direction(EntryDirection.LONG)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(33.15))
                .size(BigDecimal.valueOf(7))
                .exitPrice(BigDecimal.valueOf(38.56))
                .costs(BigDecimal.valueOf(3.33))
                .build();
        balance = BigDecimal.valueOf(526);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getNetResult()).isEqualTo(BigDecimal.valueOf(34.54).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder()
                .direction(EntryDirection.SHORT)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .exitPrice(BigDecimal.valueOf(0.87))
                .costs(BigDecimal.valueOf(11.12))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getNetResult()).isEqualTo(BigDecimal.valueOf(25.88).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder()
                .direction(EntryDirection.SHORT)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(523.68))
                .size(BigDecimal.valueOf(15))
                .exitPrice(BigDecimal.valueOf(495.37))
                .costs(BigDecimal.valueOf(27.59))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getNetResult()).isEqualTo(BigDecimal.valueOf(397.06).setScale(2, RoundingMode.HALF_EVEN));
    }

    @DisplayName("Calculate net result for LONGs and SHORTs losing")
    @Test
    void losing() {
        Entry entry = Entry.builder()
                .direction(EntryDirection.LONG)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .exitPrice(BigDecimal.valueOf(175))
                .build();
        BigDecimal balance = BigDecimal.valueOf(1000);

        Entry calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getNetResult()).isEqualTo(BigDecimal.valueOf(-50.00).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder()
                .direction(EntryDirection.LONG)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(33.15))
                .size(BigDecimal.valueOf(7))
                .exitPrice(BigDecimal.valueOf(27.83))
                .build();
        balance = BigDecimal.valueOf(526);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getNetResult()).isEqualTo(BigDecimal.valueOf(-37.24).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder()
                .direction(EntryDirection.SHORT)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .exitPrice(BigDecimal.valueOf(1.64))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getNetResult()).isEqualTo(BigDecimal.valueOf(-40.00).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder()
                .direction(EntryDirection.SHORT)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(523.68))
                .size(BigDecimal.valueOf(15))
                .exitPrice(BigDecimal.valueOf(601.09))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getNetResult()).isEqualTo(BigDecimal.valueOf(-1161.15).setScale(2, RoundingMode.HALF_EVEN));
    }

    @DisplayName("Calculate net result for LONGs and SHORTs losing with costs")
    @Test
    void losingWithCosts() {
        Entry entry = Entry.builder()
                .direction(EntryDirection.LONG)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .exitPrice(BigDecimal.valueOf(175))
                .costs(BigDecimal.valueOf(5.59))
                .build();
        BigDecimal balance = BigDecimal.valueOf(1000);

        Entry calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getNetResult()).isEqualTo(BigDecimal.valueOf(-55.59).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder()
                .direction(EntryDirection.LONG)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(33.15))
                .size(BigDecimal.valueOf(7))
                .exitPrice(BigDecimal.valueOf(27.83))
                .costs(BigDecimal.valueOf(3.33))
                .build();
        balance = BigDecimal.valueOf(526);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getNetResult()).isEqualTo(BigDecimal.valueOf(-40.57).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder()
                .direction(EntryDirection.SHORT)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .exitPrice(BigDecimal.valueOf(1.64))
                .costs(BigDecimal.valueOf(11.12))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getNetResult()).isEqualTo(BigDecimal.valueOf(-51.12).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder()
                .direction(EntryDirection.SHORT)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(523.68))
                .size(BigDecimal.valueOf(15))
                .exitPrice(BigDecimal.valueOf(601.09))
                .costs(BigDecimal.valueOf(27.59))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getNetResult()).isEqualTo(BigDecimal.valueOf(-1188.74).setScale(2, RoundingMode.HALF_EVEN));
    }

    @DisplayName("Calculate net result for LONGs and SHORTs with ZERO result with costs")
    @Test
    void zeroGrossWithCosts() {
        Entry entry = Entry.builder()
                .direction(EntryDirection.LONG)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .exitPrice(BigDecimal.valueOf(200))
                .costs(BigDecimal.valueOf(5.59))
                .build();
        BigDecimal balance = BigDecimal.valueOf(1000);

        Entry calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getNetResult()).isEqualTo(BigDecimal.valueOf(-5.59).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder()
                .direction(EntryDirection.LONG)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(33.15))
                .size(BigDecimal.valueOf(7))
                .exitPrice(BigDecimal.valueOf(33.15))
                .costs(BigDecimal.valueOf(3.33))
                .build();
        balance = BigDecimal.valueOf(526);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getNetResult()).isEqualTo(BigDecimal.valueOf(-3.33).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder()
                .direction(EntryDirection.SHORT)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .exitPrice(BigDecimal.valueOf(1.24))
                .costs(BigDecimal.valueOf(11.12))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getNetResult()).isEqualTo(BigDecimal.valueOf(-11.12).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder()
                .direction(EntryDirection.SHORT)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(523.68))
                .size(BigDecimal.valueOf(15))
                .exitPrice(BigDecimal.valueOf(523.68))
                .costs(BigDecimal.valueOf(27.59))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getNetResult()).isEqualTo(BigDecimal.valueOf(-27.59).setScale(2, RoundingMode.HALF_EVEN));
    }

    @DisplayName("Net result with ZERO Balance")
    @Test
    void netWithZeroBalance() {
        BigDecimal balance = BigDecimal.ZERO;

        Entry entry = Entry.builder()
                .direction(EntryDirection.LONG)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .exitPrice(BigDecimal.valueOf(240))
                .build();
        Entry calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getNetResult()).isEqualTo(BigDecimal.valueOf(80.00).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder()
                .direction(EntryDirection.SHORT)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .exitPrice(BigDecimal.valueOf(0.87))
                .build();
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getNetResult()).isEqualTo(BigDecimal.valueOf(37.00).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder()
                .direction(EntryDirection.LONG)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(33.15))
                .size(BigDecimal.valueOf(7))
                .exitPrice(BigDecimal.valueOf(27.83))
                .costs(BigDecimal.valueOf(3.33))
                .build();
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getNetResult()).isEqualTo(BigDecimal.valueOf(-40.57).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder()
                .direction(EntryDirection.SHORT)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .exitPrice(BigDecimal.valueOf(1.64))
                .costs(BigDecimal.valueOf(11.12))
                .build();
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getNetResult()).isEqualTo(BigDecimal.valueOf(-51.12).setScale(2, RoundingMode.HALF_EVEN));
    }

    @DisplayName("Net result with Negative Balance")
    @Test
    void netWithNegativeBalance() {
        BigDecimal balance = BigDecimal.valueOf(-1000);

        Entry entry = Entry.builder()
                .direction(EntryDirection.LONG)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .exitPrice(BigDecimal.valueOf(240))
                .build();
        Entry calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getNetResult()).isEqualTo(BigDecimal.valueOf(80.00).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder()
                .direction(EntryDirection.SHORT)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .exitPrice(BigDecimal.valueOf(0.87))
                .build();
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getNetResult()).isEqualTo(BigDecimal.valueOf(37.00).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder()
                .direction(EntryDirection.LONG)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(33.15))
                .size(BigDecimal.valueOf(7))
                .exitPrice(BigDecimal.valueOf(27.83))
                .costs(BigDecimal.valueOf(3.33))
                .build();
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getNetResult()).isEqualTo(BigDecimal.valueOf(-40.57).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder()
                .direction(EntryDirection.SHORT)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .exitPrice(BigDecimal.valueOf(1.64))
                .costs(BigDecimal.valueOf(11.12))
                .build();
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getNetResult()).isEqualTo(BigDecimal.valueOf(-51.12).setScale(2, RoundingMode.HALF_EVEN));
    }
}