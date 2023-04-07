package com.trading.journal.entry.entries.taxes;

import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.EntryType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TaxesMapperTest {

    @DisplayName("Map Taxes to Entry when Creating Entry")
    @Test
    void create() {
        String journalId = UUID.randomUUID().toString();

        Taxes taxes = Taxes.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .price(BigDecimal.valueOf(200.21))
                .build();

        Entry entry = TaxesMapper.INSTANCE.toEntry(taxes, journalId);

        assertThat(entry.getId()).isNull();
        assertThat(entry.getJournalId()).isEqualTo(journalId);
        assertThat(entry.getDate()).isEqualTo(LocalDateTime.of(2022, 9, 20, 15, 30, 50));
        assertThat(entry.getPrice()).isEqualTo(BigDecimal.valueOf(200.21));
        assertThat(entry.getType()).isEqualTo(EntryType.TAXES);
        assertThat(entry.getSymbol()).isNull();
        assertThat(entry.getDirection()).isNull();
        assertThat(entry.getSize()).isNull();
        assertThat(entry.getGraphType()).isNull();
        assertThat(entry.getGraphMeasure()).isNull();
        assertThat(entry.getProfitPrice()).isNull();
        assertThat(entry.getLossPrice()).isNull();
        assertThat(entry.getCosts()).isNull();
        assertThat(entry.getExitDate()).isNull();
        assertThat(entry.getExitPrice()).isNull();
        assertThat(entry.getNotes()).isNull();
        assertThat(entry.getAccountRisked()).isNull();
        assertThat(entry.getPlannedRR()).isNull();
        assertThat(entry.getGrossResult()).isNull();
        assertThat(entry.getNetResult()).isNull();
        assertThat(entry.getAccountChange()).isNull();
        assertThat(entry.getAccountBalance()).isNull();
        assertThat(entry.getScreenshotBefore()).isNull();
        assertThat(entry.getScreenshotAfter()).isNull();
        assertThat(entry.isFinished()).isFalse();
    }

    @DisplayName("Given null return Null")
    @Test
    void nullReturn() {
        assertThat(TaxesMapper.INSTANCE.toEntry(null, null)).isNull();
        assertThat(TaxesMapper.INSTANCE.toEntry(null, "")).isNotNull();
    }
}