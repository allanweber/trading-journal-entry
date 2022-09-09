package com.trading.journal.entry.entries;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

class CalculateEntryAccountBalanceTest {

    @DisplayName("Do not calculate account balance because there is no exit price")
    @Test
    void noCalcNoExit() {
        Entry entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .build();
        BigDecimal balance = BigDecimal.valueOf(1000);

        Entry calculated = new CalculateEntry(entry, balance).calculate();

        assertThat(calculated.getAccountBalance()).isNull();
    }

    @DisplayName("Calculate account balance for LONGs and SHORTs winning")
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
        assertThat(calculated.getAccountBalance()).isEqualTo(BigDecimal.valueOf(1080.00).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(33.15))
                .size(BigDecimal.valueOf(7))
                .exitPrice(BigDecimal.valueOf(38.56))
                .build();
        balance = BigDecimal.valueOf(526);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountBalance()).isEqualTo(BigDecimal.valueOf(563.87).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .exitPrice(BigDecimal.valueOf(0.87))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountBalance()).isEqualTo(BigDecimal.valueOf(382.67).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(523.68))
                .size(BigDecimal.valueOf(15))
                .exitPrice(BigDecimal.valueOf(495.37))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountBalance()).isEqualTo(BigDecimal.valueOf(770.32).setScale(2, RoundingMode.HALF_EVEN));
    }

    @DisplayName("Calculate account balance for LONGs and SHORTs winning with costs")
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
        assertThat(calculated.getAccountBalance()).isEqualTo(BigDecimal.valueOf(1074.41).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(33.15))
                .size(BigDecimal.valueOf(7))
                .exitPrice(BigDecimal.valueOf(38.56))
                .costs(BigDecimal.valueOf(3.33))
                .build();
        balance = BigDecimal.valueOf(526);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountBalance()).isEqualTo(BigDecimal.valueOf(560.54).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .exitPrice(BigDecimal.valueOf(0.87))
                .costs(BigDecimal.valueOf(11.12))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountBalance()).isEqualTo(BigDecimal.valueOf(371.55).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(523.68))
                .size(BigDecimal.valueOf(15))
                .exitPrice(BigDecimal.valueOf(495.37))
                .costs(BigDecimal.valueOf(27.59))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountBalance()).isEqualTo(BigDecimal.valueOf(742.73).setScale(2, RoundingMode.HALF_EVEN));
    }

    @DisplayName("Calculate account balance for LONGs and SHORTs losing")
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
        assertThat(calculated.getAccountBalance()).isEqualTo(BigDecimal.valueOf(950).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(33.15))
                .size(BigDecimal.valueOf(7))
                .exitPrice(BigDecimal.valueOf(27.83))
                .build();
        balance = BigDecimal.valueOf(526);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountBalance()).isEqualTo(BigDecimal.valueOf(488.76).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .exitPrice(BigDecimal.valueOf(1.64))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountBalance()).isEqualTo(BigDecimal.valueOf(305.67).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(523.68))
                .size(BigDecimal.valueOf(15))
                .exitPrice(BigDecimal.valueOf(601.09))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountBalance()).isEqualTo(BigDecimal.valueOf(-815.48).setScale(2, RoundingMode.HALF_EVEN));
    }

    @DisplayName("Calculate account balance for LONGs and SHORTs losing with costs")
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
        assertThat(calculated.getAccountBalance()).isEqualTo(BigDecimal.valueOf(944.41).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(33.15))
                .size(BigDecimal.valueOf(7))
                .exitPrice(BigDecimal.valueOf(27.83))
                .costs(BigDecimal.valueOf(3.33))
                .build();
        balance = BigDecimal.valueOf(526);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountBalance()).isEqualTo(BigDecimal.valueOf(485.43).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .exitPrice(BigDecimal.valueOf(1.64))
                .costs(BigDecimal.valueOf(11.12))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountBalance()).isEqualTo(BigDecimal.valueOf(294.55).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(523.68))
                .size(BigDecimal.valueOf(15))
                .exitPrice(BigDecimal.valueOf(601.09))
                .costs(BigDecimal.valueOf(27.59))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountBalance()).isEqualTo(BigDecimal.valueOf(-843.07).setScale(2, RoundingMode.HALF_EVEN));
    }

    @DisplayName("Calculate account balance for LONGs and SHORTs with ZERO result with costs")
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
        assertThat(calculated.getAccountBalance()).isEqualTo(BigDecimal.valueOf(994.41).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(33.15))
                .size(BigDecimal.valueOf(7))
                .exitPrice(BigDecimal.valueOf(33.15))
                .costs(BigDecimal.valueOf(3.33))
                .build();
        balance = BigDecimal.valueOf(526);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountBalance()).isEqualTo(BigDecimal.valueOf(522.67).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .exitPrice(BigDecimal.valueOf(1.24))
                .costs(BigDecimal.valueOf(11.12))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountBalance()).isEqualTo(BigDecimal.valueOf(334.55).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(523.68))
                .size(BigDecimal.valueOf(15))
                .exitPrice(BigDecimal.valueOf(523.68))
                .costs(BigDecimal.valueOf(27.59))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountBalance()).isEqualTo(BigDecimal.valueOf(318.08).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(523.68))
                .size(BigDecimal.valueOf(15))
                .exitPrice(BigDecimal.valueOf(523.68))
                .costs(BigDecimal.valueOf(27.59))
                .build();
        balance = BigDecimal.valueOf(-999.99);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountBalance()).isEqualTo(BigDecimal.valueOf(-1027.58).setScale(2, RoundingMode.HALF_EVEN));
    }

    @DisplayName("Account balance with ZERO Balance")
    @Test
    void balanceWithZeroBalance() {
        BigDecimal balance = BigDecimal.ZERO;

        Entry entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .exitPrice(BigDecimal.valueOf(240))
                .build();
        Entry calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountBalance()).isEqualTo(BigDecimal.valueOf(80.00).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .exitPrice(BigDecimal.valueOf(0.87))
                .build();
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountBalance()).isEqualTo(BigDecimal.valueOf(37.00).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(33.15))
                .size(BigDecimal.valueOf(7))
                .exitPrice(BigDecimal.valueOf(27.83))
                .build();
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountBalance()).isEqualTo(BigDecimal.valueOf(-37.24).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .exitPrice(BigDecimal.valueOf(1.64))
                .build();
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountBalance()).isEqualTo(BigDecimal.valueOf(-40.00).setScale(2, RoundingMode.HALF_EVEN));
    }

    @DisplayName("Account balance with Negative Balance")
    @Test
    void balanceWithNegativeBalance() {
        BigDecimal balance = BigDecimal.valueOf(-1000);

        Entry entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .exitPrice(BigDecimal.valueOf(240))
                .build();
        Entry calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountBalance()).isEqualTo(BigDecimal.valueOf(-920.00).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .exitPrice(BigDecimal.valueOf(0.87))
                .build();
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountBalance()).isEqualTo(BigDecimal.valueOf(-963).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(33.15))
                .size(BigDecimal.valueOf(7))
                .exitPrice(BigDecimal.valueOf(27.83))
                .build();
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountBalance()).isEqualTo(BigDecimal.valueOf(-1037.24).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .exitPrice(BigDecimal.valueOf(1.64))
                .build();
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountBalance()).isEqualTo(BigDecimal.valueOf(-1040.00).setScale(2, RoundingMode.HALF_EVEN));
    }
}