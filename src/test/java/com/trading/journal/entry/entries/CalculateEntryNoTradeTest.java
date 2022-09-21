package com.trading.journal.entry.entries;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CalculateEntryNoTradeTest {

    @DisplayName("When entry is WITHDRAWAL, DEPOSIT or TAXES keep only Date, Type and Price values")
    @Test
    void plainValues() {
        BigDecimal balance = BigDecimal.valueOf(1000);

        Entry entry = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .type(EntryType.WITHDRAWAL)
                .price(BigDecimal.valueOf(123.45))
                .build();
        Entry calculated = new CalculateEntry(entry, balance).calculate();

        assertThat(calculated.getGrossResult()).isNull();
        assertThat(calculated.getDate()).isEqualTo(LocalDateTime.of(2022, 9, 20, 15, 30, 50));
        assertThat(calculated.getType()).isEqualTo(EntryType.WITHDRAWAL);
        assertThat(calculated.getPrice()).isEqualTo(BigDecimal.valueOf(123.45));
        assertThat(calculated.getNetResult()).isEqualTo(BigDecimal.valueOf(-123.45));
        assertThat(calculated.getAccountChange()).isEqualTo(BigDecimal.valueOf(-0.1234));
        assertThat(calculated.getAccountBalance()).isEqualTo(BigDecimal.valueOf(876.55));
        assertThat(calculated.getSymbol()).isNull();
        assertThat(calculated.getCosts()).isNull();
        assertThat(calculated.getDirection()).isNull();
        assertThat(calculated.getSize()).isNull();
        assertThat(calculated.getProfitPrice()).isNull();
        assertThat(calculated.getLossPrice()).isNull();
        assertThat(calculated.getAccountRisked()).isNull();
        assertThat(calculated.getPlannedRR()).isNull();
        assertThat(calculated.getExitPrice()).isNull();
        assertThat(calculated.getGrossResult()).isNull();

        entry = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .type(EntryType.DEPOSIT)
                .price(BigDecimal.valueOf(123.45))
                .build();
        calculated = new CalculateEntry(entry, balance).calculate();

        assertThat(calculated.getGrossResult()).isNull();
        assertThat(calculated.getDate()).isEqualTo(LocalDateTime.of(2022, 9, 20, 15, 30, 50));
        assertThat(calculated.getType()).isEqualTo(EntryType.DEPOSIT);
        assertThat(calculated.getPrice()).isEqualTo(BigDecimal.valueOf(123.45));
        assertThat(calculated.getNetResult()).isEqualTo(BigDecimal.valueOf(123.45));
        assertThat(calculated.getAccountChange()).isEqualTo(BigDecimal.valueOf(0.1234));
        assertThat(calculated.getAccountBalance()).isEqualTo(BigDecimal.valueOf(1123.45));
        assertThat(calculated.getSymbol()).isNull();
        assertThat(calculated.getCosts()).isNull();
        assertThat(calculated.getDirection()).isNull();
        assertThat(calculated.getSize()).isNull();
        assertThat(calculated.getProfitPrice()).isNull();
        assertThat(calculated.getLossPrice()).isNull();
        assertThat(calculated.getAccountRisked()).isNull();
        assertThat(calculated.getPlannedRR()).isNull();
        assertThat(calculated.getExitPrice()).isNull();
        assertThat(calculated.getGrossResult()).isNull();

        entry = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .type(EntryType.TAXES)
                .price(BigDecimal.valueOf(123.45))
                .build();
        calculated = new CalculateEntry(entry, balance).calculate();

        assertThat(calculated.getGrossResult()).isNull();
        assertThat(calculated.getDate()).isEqualTo(LocalDateTime.of(2022, 9, 20, 15, 30, 50));
        assertThat(calculated.getType()).isEqualTo(EntryType.TAXES);
        assertThat(calculated.getPrice()).isEqualTo(BigDecimal.valueOf(123.45));
        assertThat(calculated.getNetResult()).isEqualTo(BigDecimal.valueOf(-123.45));
        assertThat(calculated.getAccountChange()).isEqualTo(BigDecimal.valueOf(-0.1234));
        assertThat(calculated.getAccountBalance()).isEqualTo(BigDecimal.valueOf(876.55));
        assertThat(calculated.getSymbol()).isNull();
        assertThat(calculated.getCosts()).isNull();
        assertThat(calculated.getDirection()).isNull();
        assertThat(calculated.getSize()).isNull();
        assertThat(calculated.getProfitPrice()).isNull();
        assertThat(calculated.getLossPrice()).isNull();
        assertThat(calculated.getAccountRisked()).isNull();
        assertThat(calculated.getPlannedRR()).isNull();
        assertThat(calculated.getExitPrice()).isNull();
        assertThat(calculated.getGrossResult()).isNull();
    }

    @DisplayName("When entry is WITHDRAWAL, DEPOSIT or TAXES with extra info keep only Date, Type and Price values")
    @Test
    void extraValues() {
        BigDecimal balance = BigDecimal.valueOf(1000);

        Entry entry = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .type(EntryType.WITHDRAWAL)
                .price(BigDecimal.valueOf(123.45))
                .direction(EntryDirection.LONG)
                .exitPrice(BigDecimal.valueOf(0.87))
                .costs(BigDecimal.valueOf(5.59))
                .size(BigDecimal.valueOf(15))
                .profitPrice(BigDecimal.valueOf(38.56))
                .lossPrice(BigDecimal.valueOf(29.19))
                .build();
        Entry calculated = new CalculateEntry(entry, balance).calculate();

        assertThat(calculated.getGrossResult()).isNull();
        assertThat(calculated.getDate()).isEqualTo(LocalDateTime.of(2022, 9, 20, 15, 30, 50));
        assertThat(calculated.getType()).isEqualTo(EntryType.WITHDRAWAL);
        assertThat(calculated.getPrice()).isEqualTo(BigDecimal.valueOf(123.45));
        assertThat(calculated.getNetResult()).isEqualTo(BigDecimal.valueOf(-123.45));
        assertThat(calculated.getAccountChange()).isEqualTo(BigDecimal.valueOf(-0.1234));
        assertThat(calculated.getAccountBalance()).isEqualTo(BigDecimal.valueOf(876.55));
        assertThat(calculated.getSymbol()).isNull();
        assertThat(calculated.getCosts()).isNull();
        assertThat(calculated.getDirection()).isNull();
        assertThat(calculated.getSize()).isNull();
        assertThat(calculated.getProfitPrice()).isNull();
        assertThat(calculated.getLossPrice()).isNull();
        assertThat(calculated.getAccountRisked()).isNull();
        assertThat(calculated.getPlannedRR()).isNull();
        assertThat(calculated.getExitPrice()).isNull();
        assertThat(calculated.getGrossResult()).isNull();

        entry = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .type(EntryType.DEPOSIT)
                .price(BigDecimal.valueOf(123.45))
                .direction(EntryDirection.LONG)
                .exitPrice(BigDecimal.valueOf(0.87))
                .costs(BigDecimal.valueOf(5.59))
                .size(BigDecimal.valueOf(15))
                .profitPrice(BigDecimal.valueOf(38.56))
                .lossPrice(BigDecimal.valueOf(29.19))
                .build();
        calculated = new CalculateEntry(entry, balance).calculate();

        assertThat(calculated.getGrossResult()).isNull();
        assertThat(calculated.getDate()).isEqualTo(LocalDateTime.of(2022, 9, 20, 15, 30, 50));
        assertThat(calculated.getType()).isEqualTo(EntryType.DEPOSIT);
        assertThat(calculated.getPrice()).isEqualTo(BigDecimal.valueOf(123.45));
        assertThat(calculated.getNetResult()).isEqualTo(BigDecimal.valueOf(123.45));
        assertThat(calculated.getAccountChange()).isEqualTo(BigDecimal.valueOf(0.1234));
        assertThat(calculated.getAccountBalance()).isEqualTo(BigDecimal.valueOf(1123.45));
        assertThat(calculated.getSymbol()).isNull();
        assertThat(calculated.getCosts()).isNull();
        assertThat(calculated.getDirection()).isNull();
        assertThat(calculated.getSize()).isNull();
        assertThat(calculated.getProfitPrice()).isNull();
        assertThat(calculated.getLossPrice()).isNull();
        assertThat(calculated.getAccountRisked()).isNull();
        assertThat(calculated.getPlannedRR()).isNull();
        assertThat(calculated.getExitPrice()).isNull();
        assertThat(calculated.getGrossResult()).isNull();

        entry = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .type(EntryType.TAXES)
                .price(BigDecimal.valueOf(123.45))
                .direction(EntryDirection.LONG)
                .exitPrice(BigDecimal.valueOf(0.87))
                .costs(BigDecimal.valueOf(5.59))
                .size(BigDecimal.valueOf(15))
                .profitPrice(BigDecimal.valueOf(38.56))
                .lossPrice(BigDecimal.valueOf(29.19))
                .build();
        calculated = new CalculateEntry(entry, balance).calculate();

        assertThat(calculated.getGrossResult()).isNull();
        assertThat(calculated.getDate()).isEqualTo(LocalDateTime.of(2022, 9, 20, 15, 30, 50));
        assertThat(calculated.getType()).isEqualTo(EntryType.TAXES);
        assertThat(calculated.getPrice()).isEqualTo(BigDecimal.valueOf(123.45));
        assertThat(calculated.getNetResult()).isEqualTo(BigDecimal.valueOf(-123.45));
        assertThat(calculated.getAccountChange()).isEqualTo(BigDecimal.valueOf(-0.1234));
        assertThat(calculated.getAccountBalance()).isEqualTo(BigDecimal.valueOf(876.55));
        assertThat(calculated.getSymbol()).isNull();
        assertThat(calculated.getCosts()).isNull();
        assertThat(calculated.getDirection()).isNull();
        assertThat(calculated.getSize()).isNull();
        assertThat(calculated.getProfitPrice()).isNull();
        assertThat(calculated.getLossPrice()).isNull();
        assertThat(calculated.getAccountRisked()).isNull();
        assertThat(calculated.getPlannedRR()).isNull();
        assertThat(calculated.getExitPrice()).isNull();
        assertThat(calculated.getGrossResult()).isNull();
    }
}