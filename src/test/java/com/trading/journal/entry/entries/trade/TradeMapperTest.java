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
        assertThat(entry.getNotes()).isEqualTo("some notes");
        assertThat(entry.getExitDate()).isNull();
        assertThat(entry.getExitPrice()).isNull();
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
        assertThat(entry.getNotes()).isEqualTo("some notes");
        assertThat(entry.getExitDate()).isNull();
        assertThat(entry.getExitPrice()).isNull();
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

    @DisplayName("Map Trade to Entry when Closing Entry")
    @Test
    void close() {
        Entry trade = Entry.builder()
                .id(UUID.randomUUID().toString())
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
                .notes("some notes")
                .build();

        CloseTrade closeTrade = CloseTrade.builder()
                .exitPrice(BigDecimal.valueOf(250.31))
                .exitDate(LocalDateTime.of(2022, 9, 21, 15, 30, 50))
                .build();


        Entry entry = TradeMapper.INSTANCE.toEntryFromClose(trade, closeTrade);

        assertThat(entry.getId()).isEqualTo(trade.getId());
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
        assertThat(entry.getNotes()).isEqualTo("some notes");
        assertThat(entry.getExitDate()).isEqualTo(LocalDateTime.of(2022, 9, 21, 15, 30, 50));
        assertThat(entry.getExitPrice()).isEqualTo(BigDecimal.valueOf(250.31));
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

    @DisplayName("Given null")
    @Test
    void nullTests() {
        assertThat(TradeMapper.INSTANCE.toEntry(null)).isNull();

        assertThat(TradeMapper.INSTANCE.toEntry(null, null)).isNull();

        assertThat(TradeMapper.INSTANCE.toEntry(null, "123")).isNotNull();

        assertThat(TradeMapper.INSTANCE.toEntryFromClose(null, null)).isNull();

        assertThat(TradeMapper.INSTANCE.toEntryFromClose(Entry.builder().build(), null)).isNotNull();

        assertThat(TradeMapper.INSTANCE.toEntryFromClose(null, CloseTrade.builder().build())).isNotNull();
    }
}