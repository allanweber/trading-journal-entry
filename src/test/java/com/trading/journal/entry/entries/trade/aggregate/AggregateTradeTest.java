package com.trading.journal.entry.entries.trade.aggregate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AggregateTradeTest {

    @DisplayName("Check the result of skip based on page X size")
    @Test
    void skip() {
        AggregateTrade aggregateTrade = new AggregateTrade(AggregateType.DAY, 0L, 10L);
        assertThat(aggregateTrade.getSkip()).isEqualTo(0L);

        aggregateTrade = new AggregateTrade(AggregateType.DAY, 1L, 10L);
        assertThat(aggregateTrade.getSkip()).isEqualTo(10L);

        aggregateTrade = new AggregateTrade(AggregateType.DAY, 3L, 25L);
        assertThat(aggregateTrade.getSkip()).isEqualTo(75L);
    }
}