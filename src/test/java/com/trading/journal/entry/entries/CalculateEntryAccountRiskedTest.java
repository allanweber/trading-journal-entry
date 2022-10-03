package com.trading.journal.entry.entries;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CalculateEntryAccountRiskedTest {

    @DisplayName("Account Risked for Long")
    @Test
    void longAccountRisked() {
        Entry entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .profitPrice(BigDecimal.valueOf(240))
                .lossPrice(BigDecimal.valueOf(180))
                .build();
        BigDecimal balance = BigDecimal.valueOf(1000);
        Entry calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountRisked()).isEqualTo(BigDecimal.valueOf(0.0400).setScale(4, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(33.15))
                .size(BigDecimal.valueOf(7))
                .profitPrice(BigDecimal.valueOf(38.56))
                .lossPrice(BigDecimal.valueOf(29.19))
                .build();
        balance = BigDecimal.valueOf(526);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountRisked()).isEqualTo(BigDecimal.valueOf(0.0527));
    }

    @DisplayName("Do not change account risked when trade finish")
    @Test
    void notChangeRisk() {
        Entry entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .profitPrice(BigDecimal.valueOf(240))
                .lossPrice(BigDecimal.valueOf(180))
                .accountRisked(BigDecimal.valueOf(0.0400).setScale(4, RoundingMode.HALF_EVEN))
                .exitPrice(BigDecimal.valueOf(240))
                .date(LocalDateTime.now())
                .build();

        BigDecimal balance = BigDecimal.valueOf(50000);
        Entry calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountRisked()).isEqualTo(BigDecimal.valueOf(0.0400).setScale(4, RoundingMode.HALF_EVEN));
    }

    @DisplayName("Account Risked for Short")
    @Test
    void shortAccountRisked() {
        Entry entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(100))
                .size(BigDecimal.valueOf(1))
                .profitPrice(BigDecimal.valueOf(80))
                .lossPrice(BigDecimal.valueOf(110))
                .build();
        BigDecimal balance = BigDecimal.valueOf(500);
        Entry calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountRisked()).isEqualTo(BigDecimal.valueOf(0.0200).setScale(4, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .profitPrice(BigDecimal.valueOf(0.87))
                .lossPrice(BigDecimal.valueOf(1.37))
                .build();
        balance = BigDecimal.valueOf(345.67);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountRisked()).isEqualTo(BigDecimal.valueOf(0.0376).setScale(4, RoundingMode.HALF_EVEN));
    }

    @DisplayName("Account Risked without loss")
    @Test
    void riskPercentNoLoss() {
        Entry entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .profitPrice(BigDecimal.valueOf(240))
                .build();
        BigDecimal balance = BigDecimal.valueOf(1000);
        Entry calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountRisked()).isEqualTo(BigDecimal.valueOf(0.4000).setScale(4, RoundingMode.HALF_EVEN));

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .profitPrice(BigDecimal.valueOf(240))
                .build();
        balance = BigDecimal.valueOf(1000);
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountRisked()).isEqualTo(BigDecimal.valueOf(0.4000).setScale(4, RoundingMode.HALF_EVEN));
    }

    @DisplayName("Account Risked With Zero Balance")
    @Test
    void accountRiskedWithZeroBalance() {
        BigDecimal balance = BigDecimal.ZERO;

        Entry entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .profitPrice(BigDecimal.valueOf(240))
                .lossPrice(BigDecimal.valueOf(180))
                .build();
        Entry calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountRisked()).isNull();

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .profitPrice(BigDecimal.valueOf(0.87))
                .lossPrice(BigDecimal.valueOf(1.37))
                .build();
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountRisked()).isNull();
    }

    @DisplayName("Account Risked With Negative Balance")
    @Test
    void accountRiskedWithNegativeBalance() {
        BigDecimal balance = BigDecimal.valueOf(-1000);

        Entry entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .profitPrice(BigDecimal.valueOf(240))
                .lossPrice(BigDecimal.valueOf(180))
                .build();
        Entry calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountRisked()).isNull();

        entry = Entry.builder().type(EntryType.TRADE)
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(1.24))
                .size(BigDecimal.valueOf(100))
                .profitPrice(BigDecimal.valueOf(0.87))
                .lossPrice(BigDecimal.valueOf(1.37))
                .build();
        calculated = new CalculateEntry(entry, balance).calculate();
        assertThat(calculated.getAccountRisked()).isNull();
    }
}