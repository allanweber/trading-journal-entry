package com.trading.journal.entry.entries.trade;

import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.EntryDirection;
import com.trading.journal.entry.entries.EntryType;
import com.trading.journal.entry.entries.GraphType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TradeMapperTest {

    @DisplayName("Map Trade to Entry when Creating Entry")
    @Test
    void create() {
        Trade trade = Trade.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .price(BigDecimal.valueOf(200.21))
                .symbol("MSFT")
                .direction(EntryDirection.LONG)
                .size(BigDecimal.valueOf(2.56))
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure("1")
                .profitPrice(BigDecimal.valueOf(250.12))
                .lossPrice(BigDecimal.valueOf(180.23))
                .costs(BigDecimal.valueOf(1.25))
                .exitDate(LocalDateTime.of(2022, 9, 20, 15, 30, 51))
                .exitPrice(BigDecimal.valueOf(200))
                .notes("some notes")
                .build();

        Entry entry = TradeMapper.INSTANCE.toEntry(trade);

        assertThat(entry.getId()).isNull();
        assertThat(entry.getType()).isEqualTo(EntryType.TRADE);
        assertThat(entry.getDate()).isEqualTo(LocalDateTime.of(2022, 9, 20, 15, 30, 50));
        assertThat(entry.getPrice()).isEqualTo(BigDecimal.valueOf(200.21));
        assertThat(entry.getSymbol()).isEqualTo("MSFT");
        assertThat(entry.getDirection()).isEqualTo(EntryDirection.LONG);
        assertThat(entry.getSize()).isEqualTo(BigDecimal.valueOf(2.56));
        assertThat(entry.getGraphType()).isEqualTo(GraphType.CANDLESTICK);
        assertThat(entry.getGraphMeasure()).isEqualTo("1");
        assertThat(entry.getProfitPrice()).isEqualTo(BigDecimal.valueOf(250.12));
        assertThat(entry.getLossPrice()).isEqualTo(BigDecimal.valueOf(180.23));
        assertThat(entry.getCosts()).isEqualTo(BigDecimal.valueOf(1.25));
        assertThat(entry.getExitDate()).isEqualTo(LocalDateTime.of(2022, 9, 20, 15, 30, 51));
        assertThat(entry.getExitPrice()).isEqualTo(BigDecimal.valueOf(200));
        assertThat(entry.getNotes()).isEqualTo("some notes");
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


    @DisplayName("Map Trade to Entry when Updating Entry")
    @Test
    void update() {
        Trade trade = Trade.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .price(BigDecimal.valueOf(200.21))
                .symbol("MSFT")
                .direction(EntryDirection.LONG)
                .size(BigDecimal.valueOf(2.56))
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure("1")
                .profitPrice(BigDecimal.valueOf(250.12))
                .lossPrice(BigDecimal.valueOf(180.23))
                .costs(BigDecimal.valueOf(1.25))
                .exitDate(LocalDateTime.of(2022, 9, 20, 15, 30, 51))
                .exitPrice(BigDecimal.valueOf(200))
                .notes("some notes")
                .build();

        String id = UUID.randomUUID().toString();
        Entry entry = TradeMapper.INSTANCE.toEntry(trade, id);

        assertThat(entry.getId()).isEqualTo(id);
        assertThat(entry.getType()).isEqualTo(EntryType.TRADE);
        assertThat(entry.getDate()).isEqualTo(LocalDateTime.of(2022, 9, 20, 15, 30, 50));
        assertThat(entry.getPrice()).isEqualTo(BigDecimal.valueOf(200.21));
        assertThat(entry.getSymbol()).isEqualTo("MSFT");
        assertThat(entry.getDirection()).isEqualTo(EntryDirection.LONG);
        assertThat(entry.getSize()).isEqualTo(BigDecimal.valueOf(2.56));
        assertThat(entry.getGraphType()).isEqualTo(GraphType.CANDLESTICK);
        assertThat(entry.getGraphMeasure()).isEqualTo("1");
        assertThat(entry.getProfitPrice()).isEqualTo(BigDecimal.valueOf(250.12));
        assertThat(entry.getLossPrice()).isEqualTo(BigDecimal.valueOf(180.23));
        assertThat(entry.getCosts()).isEqualTo(BigDecimal.valueOf(1.25));
        assertThat(entry.getExitDate()).isEqualTo(LocalDateTime.of(2022, 9, 20, 15, 30, 51));
        assertThat(entry.getExitPrice()).isEqualTo(BigDecimal.valueOf(200));
        assertThat(entry.getNotes()).isEqualTo("some notes");
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
        assertThat(TradeMapper.INSTANCE.toEntry(null)).isNull();

        assertThat(TradeMapper.INSTANCE.toEntry(null, null)).isNull();
    }
}