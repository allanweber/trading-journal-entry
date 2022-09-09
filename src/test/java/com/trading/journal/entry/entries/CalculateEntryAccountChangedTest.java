package com.trading.journal.entry.entries;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

class CalculateEntryAccountChangedTest {

    @DisplayName("Do not calculate account changed because there is no exit price")
    @Test
    void noCalcNoExit() {
        Entry entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .build();
        BigDecimal balance = BigDecimal.valueOf(1000);

        Entry calculated = new CalculateEntry(entry, balance).calculate();

        assertThat(calculated.getAccountChange()).isNull();
    }

    @DisplayName("Calculate account changed for LONGs and SHORTs winning")
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
        assertThat(calculated.getAccountChange()).isEqualTo(BigDecimal.valueOf(0.0800).setScale(4, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(33.15))
                .size(BigDecimal.valueOf(7))
                .exitPrice(BigDecimal.valueOf(38.56))
                .build();
        balance = BigDecimal.valueOf(526);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountChange()).isEqualTo(BigDecimal.valueOf(0.0720).setScale(4, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .exitPrice(BigDecimal.valueOf(0.87))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountChange()).isEqualTo(BigDecimal.valueOf(0.1070).setScale(4, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(523.68))
                .size(BigDecimal.valueOf(15))
                .exitPrice(BigDecimal.valueOf(495.37))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountChange()).isEqualTo(BigDecimal.valueOf(1.2285).setScale(4, RoundingMode.HALF_EVEN));
    }

    @DisplayName("Calculate account changed for LONGs and SHORTs winning with costs")
    @Test
    void winningWithCosts() {
        Entry entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .exitPrice(BigDecimal.valueOf(240))
                .costs(BigDecimal.valueOf(5.59))
                .build();
        BigDecimal balance = BigDecimal.valueOf(1000);

        Entry calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountChange()).isEqualTo(BigDecimal.valueOf(0.0744).setScale(4, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(33.15))
                .size(BigDecimal.valueOf(7))
                .exitPrice(BigDecimal.valueOf(38.56))
                .costs(BigDecimal.valueOf(3.33))
                .build();
        balance = BigDecimal.valueOf(526);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountChange()).isEqualTo(BigDecimal.valueOf(0.0657).setScale(4, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .exitPrice(BigDecimal.valueOf(0.87))
                .costs(BigDecimal.valueOf(11.12))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountChange()).isEqualTo(BigDecimal.valueOf(0.0749).setScale(4, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(523.68))
                .size(BigDecimal.valueOf(15))
                .exitPrice(BigDecimal.valueOf(495.37))
                .costs(BigDecimal.valueOf(27.59))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountChange()).isEqualTo(BigDecimal.valueOf(1.1487).setScale(4, RoundingMode.HALF_EVEN));
    }

    @DisplayName("Calculate account changed for LONGs and SHORTs losing")
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
        assertThat(calculated.getAccountChange()).isEqualTo(BigDecimal.valueOf(-0.0500).setScale(4, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(33.15))
                .size(BigDecimal.valueOf(7))
                .exitPrice(BigDecimal.valueOf(27.83))
                .build();
        balance = BigDecimal.valueOf(526);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountChange()).isEqualTo(BigDecimal.valueOf(-0.0708).setScale(4, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .exitPrice(BigDecimal.valueOf(1.64))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountChange()).isEqualTo(BigDecimal.valueOf(-0.1157).setScale(4, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(523.68))
                .size(BigDecimal.valueOf(15))
                .exitPrice(BigDecimal.valueOf(601.09))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountChange()).isEqualTo(BigDecimal.valueOf(-3.3591).setScale(4, RoundingMode.HALF_EVEN));
    }

    @DisplayName("Calculate account changed for LONGs and SHORTs losing with costs")
    @Test
    void losingWithCosts() {
        Entry entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .exitPrice(BigDecimal.valueOf(175))
                .costs(BigDecimal.valueOf(5.59))
                .build();
        BigDecimal balance = BigDecimal.valueOf(1000);

        Entry calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountChange()).isEqualTo(BigDecimal.valueOf(-0.0556).setScale(4, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(33.15))
                .size(BigDecimal.valueOf(7))
                .exitPrice(BigDecimal.valueOf(27.83))
                .costs(BigDecimal.valueOf(3.33))
                .build();
        balance = BigDecimal.valueOf(526);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountChange()).isEqualTo(BigDecimal.valueOf(-0.0771).setScale(4, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .exitPrice(BigDecimal.valueOf(1.64))
                .costs(BigDecimal.valueOf(11.12))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountChange()).isEqualTo(BigDecimal.valueOf(-0.1479).setScale(4, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(523.68))
                .size(BigDecimal.valueOf(15))
                .exitPrice(BigDecimal.valueOf(601.09))
                .costs(BigDecimal.valueOf(27.59))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountChange()).isEqualTo(BigDecimal.valueOf(-3.4389).setScale(4, RoundingMode.HALF_EVEN));
    }

    @DisplayName("Calculate account changed for LONGs and SHORTs with ZERO result with costs")
    @Test
    void zeroGrossWithCosts() {
        Entry entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .exitPrice(BigDecimal.valueOf(200))
                .costs(BigDecimal.valueOf(5.59))
                .build();
        BigDecimal balance = BigDecimal.valueOf(1000);

        Entry calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountChange()).isEqualTo(BigDecimal.valueOf(-0.0056).setScale(4, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(33.15))
                .size(BigDecimal.valueOf(7))
                .exitPrice(BigDecimal.valueOf(33.15))
                .costs(BigDecimal.valueOf(3.33))
                .build();
        balance = BigDecimal.valueOf(526);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountChange()).isEqualTo(BigDecimal.valueOf(-0.0063).setScale(4, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .exitPrice(BigDecimal.valueOf(1.24))
                .costs(BigDecimal.valueOf(11.12))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountChange()).isEqualTo(BigDecimal.valueOf(-0.0322).setScale(4, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(523.68))
                .size(BigDecimal.valueOf(15))
                .exitPrice(BigDecimal.valueOf(523.68))
                .costs(BigDecimal.valueOf(27.59))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountChange()).isEqualTo(BigDecimal.valueOf(-0.0798).setScale(4, RoundingMode.HALF_EVEN));
    }

    @DisplayName("Account change with ZERO Balance")
    @Test
    void changeWithZeroBalance() {
        BigDecimal balance = BigDecimal.ZERO;

        Entry entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .exitPrice(BigDecimal.valueOf(240))
                .costs(BigDecimal.valueOf(5.59))
                .build();
        Entry calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountChange()).isEqualTo(BigDecimal.valueOf(1.0000).setScale(4, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .exitPrice(BigDecimal.valueOf(0.87))
                .build();
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountChange()).isEqualTo(BigDecimal.valueOf(1.0000).setScale(4, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(33.15))
                .size(BigDecimal.valueOf(7))
                .exitPrice(BigDecimal.valueOf(27.83))
                .build();
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountChange()).isEqualTo(BigDecimal.valueOf(1.0000).setScale(4, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .exitPrice(BigDecimal.valueOf(1.64))
                .build();
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountChange()).isEqualTo(BigDecimal.valueOf(1.0000).setScale(4, RoundingMode.HALF_EVEN));
    }

    @DisplayName("Account change with Negative Balance")
    @Test
    void changeWithNegativeBalance() {
        BigDecimal balance = BigDecimal.valueOf(-1000);

        Entry entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .exitPrice(BigDecimal.valueOf(240))
                .costs(BigDecimal.valueOf(5.59))
                .build();
        Entry calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountChange()).isEqualTo(BigDecimal.valueOf(-0.0744).setScale(4, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .exitPrice(BigDecimal.valueOf(0.87))
                .build();
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountChange()).isEqualTo(BigDecimal.valueOf(-0.0370).setScale(4, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(33.15))
                .size(BigDecimal.valueOf(7))
                .exitPrice(BigDecimal.valueOf(27.83))
                .build();
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountChange()).isEqualTo(BigDecimal.valueOf(-0.0372).setScale(4, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .exitPrice(BigDecimal.valueOf(1.64))
                .build();
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountChange()).isEqualTo(BigDecimal.valueOf(-0.0400).setScale(4, RoundingMode.HALF_EVEN));
    }
}