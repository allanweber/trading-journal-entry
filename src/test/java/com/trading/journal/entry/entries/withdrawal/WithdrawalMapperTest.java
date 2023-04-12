package com.trading.journal.entry.entries.withdrawal;

import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.EntryType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WithdrawalMapperTest {

    @DisplayName("Map Withdrawal to Entry when Creating Entry")
    @Test
    void create() {
        String journalId = UUID.randomUUID().toString();

        Withdrawal withdrawal = Withdrawal.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .price(BigDecimal.valueOf(200.21))
                .build();

        Entry entry = WithdrawalMapper.INSTANCE.toEntry(withdrawal, journalId);

        assertThat(entry.getId()).isNull();
        assertThat(entry.getDate()).isEqualTo(LocalDateTime.of(2022, 9, 20, 15, 30, 50));
        assertThat(entry.getPrice()).isEqualTo(BigDecimal.valueOf(200.21));
        assertThat(entry.getType()).isEqualTo(EntryType.WITHDRAWAL);
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
        assertThat(entry.isFinished()).isFalse();
    }

    @DisplayName("Given null return Null")
    @Test
    void nullReturn() {
        assertThat(WithdrawalMapper.INSTANCE.toEntry(null, null)).isNull();
        assertThat(WithdrawalMapper.INSTANCE.toEntry(null, "")).isNotNull();
    }
}