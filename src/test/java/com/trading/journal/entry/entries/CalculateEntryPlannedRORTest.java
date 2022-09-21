package com.trading.journal.entry.entries;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

class CalculateEntryPlannedRORTest {

    @DisplayName("Calculate planned ROR based on profit and loss")
    @Test
    void plannedROR() {
        Entry entry = Entry.builder()
                .direction(EntryDirection.LONG)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .profitPrice(BigDecimal.valueOf(240))
                .lossPrice(BigDecimal.valueOf(180))
                .build();
        BigDecimal balance = BigDecimal.valueOf(1000);

        Entry calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getPlannedRR()).isEqualTo(BigDecimal.valueOf(2.00).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder()
                .direction(EntryDirection.LONG)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(33.15))
                .size(BigDecimal.valueOf(7))
                .profitPrice(BigDecimal.valueOf(38.56))
                .lossPrice(BigDecimal.valueOf(29.19))
                .build();
        balance = BigDecimal.valueOf(526);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getPlannedRR()).isEqualTo(BigDecimal.valueOf(1.37).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder()
                .direction(EntryDirection.SHORT)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .profitPrice(BigDecimal.valueOf(0.87))
                .lossPrice(BigDecimal.valueOf(1.37))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getPlannedRR()).isEqualTo(BigDecimal.valueOf(2.85).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder()
                .direction(EntryDirection.SHORT)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(523.68))
                .size(BigDecimal.valueOf(15))
                .profitPrice(BigDecimal.valueOf(455.37))
                .lossPrice(BigDecimal.valueOf(578.98))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getPlannedRR()).isEqualTo(BigDecimal.valueOf(1.24).setScale(2, RoundingMode.HALF_EVEN));
    }

    @DisplayName("Do not calculate planned with profit and loss are null")
    @Test
    void plannedRORNoCalc() {
        Entry entry = Entry.builder()
                .direction(EntryDirection.LONG)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .build();
        BigDecimal balance = BigDecimal.valueOf(1000);

        Entry calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getPlannedRR()).isEqualTo(BigDecimal.valueOf(-1.00).setScale(2, RoundingMode.HALF_EVEN));
    }

    @DisplayName("Calculate only planned ROR only with profit")
    @Test
    void plannedRorOnlyProfit() {
        Entry entry = Entry.builder()
                .direction(EntryDirection.LONG)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .profitPrice(BigDecimal.valueOf(240))
                .build();
        BigDecimal balance = BigDecimal.valueOf(1000);

        Entry calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getPlannedRR()).isEqualTo(BigDecimal.valueOf(0.20).setScale(2, RoundingMode.HALF_EVEN));
    }

    @DisplayName("Calculate planned ROR only with loss")
    @Test
    void plannedRorOnlyLoss() {
        Entry entry = Entry.builder()
                .direction(EntryDirection.LONG)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .lossPrice(BigDecimal.valueOf(180))
                .build();
        BigDecimal balance = BigDecimal.valueOf(1000);

        Entry calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getPlannedRR()).isEqualTo(BigDecimal.valueOf(-10.00).setScale(2, RoundingMode.HALF_EVEN));
    }

    @DisplayName("Planned RR with ZERO Balance")
    @Test
    void rrWithZeroBalance() {
        BigDecimal balance = BigDecimal.ZERO;

        Entry entry = Entry.builder()
                .direction(EntryDirection.LONG)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(33.15))
                .size(BigDecimal.valueOf(7))
                .profitPrice(BigDecimal.valueOf(38.56))
                .lossPrice(BigDecimal.valueOf(29.19))
                .build();
        Entry calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getPlannedRR()).isEqualTo(BigDecimal.valueOf(1.37).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder()
                .direction(EntryDirection.SHORT)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .profitPrice(BigDecimal.valueOf(0.87))
                .lossPrice(BigDecimal.valueOf(1.37))
                .build();
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getPlannedRR()).isEqualTo(BigDecimal.valueOf(2.85).setScale(2, RoundingMode.HALF_EVEN));
    }

    @DisplayName("Planned RR with Negative Balance")
    @Test
    void rrWithNegativeBalance() {
        BigDecimal balance = BigDecimal.valueOf(-1000);

        Entry entry = Entry.builder()
                .direction(EntryDirection.LONG)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(33.15))
                .size(BigDecimal.valueOf(7))
                .profitPrice(BigDecimal.valueOf(38.56))
                .lossPrice(BigDecimal.valueOf(29.19))
                .build();
        Entry calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getPlannedRR()).isEqualTo(BigDecimal.valueOf(1.37).setScale(2, RoundingMode.HALF_EVEN));

        entry = Entry.builder()
                .direction(EntryDirection.SHORT)
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .profitPrice(BigDecimal.valueOf(0.87))
                .lossPrice(BigDecimal.valueOf(1.37))
                .build();
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getPlannedRR()).isEqualTo(BigDecimal.valueOf(2.85).setScale(2, RoundingMode.HALF_EVEN));
    }
}