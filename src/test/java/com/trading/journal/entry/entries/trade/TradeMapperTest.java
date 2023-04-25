package com.trading.journal.entry.entries.trade;

import com.trading.journal.entry.entries.*;
import com.trading.journal.entry.strategy.Strategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class TradeMapperTest {

    @DisplayName("Map Trade to Entry when Creating Entry")
    @Test
    void create() {
        String journalId = UUID.randomUUID().toString();
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

        Entry entry = TradeMapper.INSTANCE.toEntry(trade, journalId);

        assertThat(entry.getId()).isNull();
        assertThat(entry.getJournalId()).isEqualTo(journalId);
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
        assertThat(entry.isFinished()).isFalse();
    }

    @DisplayName("Map Trade to Entry when Creating Entry with strategy")
    @Test
    void createWithStrategy() {
        String journalId = UUID.randomUUID().toString();

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
                .strategyIds(asList("1", "2"))
                .build();

        Entry entry = TradeMapper.INSTANCE.toEntry(trade, journalId);

        assertThat(entry.getId()).isNull();
        assertThat(entry.getJournalId()).isEqualTo(journalId);
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
        assertThat(entry.getStrategyIds()).containsExactlyInAnyOrder("1", "2");
        assertThat(entry.getExitDate()).isNull();
        assertThat(entry.getExitPrice()).isNull();
        assertThat(entry.getAccountRisked()).isNull();
        assertThat(entry.getPlannedRR()).isNull();
        assertThat(entry.getGrossResult()).isNull();
        assertThat(entry.getNetResult()).isNull();
        assertThat(entry.getAccountChange()).isNull();
        assertThat(entry.getAccountBalance()).isNull();
        assertThat(entry.isFinished()).isFalse();
    }

    @DisplayName("Map Trade to Entry when Updating Entry")
    @Test
    void update() {
        String journalId = UUID.randomUUID().toString();
        String id = UUID.randomUUID().toString();

        Entry originalEntry = Entry.builder()
                .id(id)
                .journalId(journalId)
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

        Entry entry = TradeMapper.INSTANCE.toEditEntry(originalEntry, trade);

        assertThat(entry.getId()).isEqualTo(id);
        assertThat(entry.getJournalId()).isEqualTo(journalId);
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
        assertThat(entry.isFinished()).isFalse();
    }

    @DisplayName("Map Trade to Entry when Updating Entry with strategy")
    @Test
    void updateWithStrategy() {
        String journalId = UUID.randomUUID().toString();
        String id = UUID.randomUUID().toString();

        Entry originalEntry = Entry.builder()
                .id(id)
                .journalId(journalId)
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
                .strategyIds(asList("1", "2"))
                .build();

        Entry entry = TradeMapper.INSTANCE.toEditEntry(originalEntry, trade);

        assertThat(entry.getId()).isEqualTo(id);
        assertThat(entry.getJournalId()).isEqualTo(journalId);
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
        assertThat(entry.getStrategyIds()).containsExactlyInAnyOrder("1", "2");
        assertThat(entry.getExitDate()).isNull();
        assertThat(entry.getExitPrice()).isNull();
        assertThat(entry.getAccountRisked()).isNull();
        assertThat(entry.getPlannedRR()).isNull();
        assertThat(entry.getGrossResult()).isNull();
        assertThat(entry.getNetResult()).isNull();
        assertThat(entry.getAccountChange()).isNull();
        assertThat(entry.getAccountBalance()).isNull();
        assertThat(entry.isFinished()).isFalse();
    }

    @DisplayName("Map Trade to Entry when Updating Entry change some values but keep others")
    @Test
    void updateChanging() {
        String journalId = UUID.randomUUID().toString();
        String id = UUID.randomUUID().toString();

        Entry originalEntry = Entry.builder()
                .id(id)
                .journalId(journalId)
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
                .strategies(singletonList(Strategy.builder().color("red").build()))
                .images(List.of(new EntryImage("1", "image-1", "1.jpg"), new EntryImage("2", "image-2", "2.jpg")))
                .build();

        Trade trade = Trade.builder()
                .date(LocalDateTime.of(2022, 9, 10, 15, 30, 50))
                .price(BigDecimal.valueOf(200.21))
                .symbol("MSFT-2")
                .direction(EntryDirection.LONG)
                .size(BigDecimal.valueOf(2.56))
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure("1")
                .profitPrice(BigDecimal.valueOf(250.12))
                .lossPrice(BigDecimal.valueOf(180.23))
                .costs(BigDecimal.valueOf(1.25))
                .notes("some notes")
                .strategyIds(asList("1", "2"))
                .build();

        Entry entry = TradeMapper.INSTANCE.toEditEntry(originalEntry, trade);

        assertThat(entry.getId()).isEqualTo(id);
        assertThat(entry.getJournalId()).isEqualTo(journalId);
        assertThat(entry.getType()).isEqualTo(EntryType.TRADE);
        assertThat(entry.getDate()).isEqualTo(LocalDateTime.of(2022, 9, 10, 15, 30, 50));
        assertThat(entry.getPrice()).isEqualTo(BigDecimal.valueOf(200.21));
        assertThat(entry.getSymbol()).isEqualTo("MSFT-2");
        assertThat(entry.getDirection()).isEqualTo(EntryDirection.LONG);
        assertThat(entry.getSize()).isEqualTo(BigDecimal.valueOf(2.56));
        assertThat(entry.getGraphType()).isEqualTo(GraphType.CANDLESTICK);
        assertThat(entry.getGraphMeasure()).isEqualTo("1");
        assertThat(entry.getProfitPrice()).isEqualTo(BigDecimal.valueOf(250.12));
        assertThat(entry.getLossPrice()).isEqualTo(BigDecimal.valueOf(180.23));
        assertThat(entry.getCosts()).isEqualTo(BigDecimal.valueOf(1.25));
        assertThat(entry.getNotes()).isEqualTo("some notes");
        assertThat(entry.getStrategyIds()).containsExactlyInAnyOrder("1", "2");
        assertThat(entry.getStrategies()).extracting(Strategy::getColor).containsExactly("red");
        assertThat(entry.getImages()).containsExactly(new EntryImage("1", "image-1", "1.jpg"), new EntryImage("2", "image-2", "2.jpg"));
        assertThat(entry.getExitDate()).isNull();
        assertThat(entry.getExitPrice()).isNull();
        assertThat(entry.getAccountRisked()).isNull();
        assertThat(entry.getPlannedRR()).isNull();
        assertThat(entry.getGrossResult()).isNull();
        assertThat(entry.getNetResult()).isNull();
        assertThat(entry.getAccountChange()).isNull();
        assertThat(entry.getAccountBalance()).isNull();
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
        assertThat(entry.isFinished()).isFalse();
    }

    @DisplayName("Map Trade to Entry when Closing Entry with strategy")
    @Test
    void closeWithStrategy() {
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
                .strategies(asList(Strategy.builder().id("1").name("ST1").build(), Strategy.builder().id("2").name("ST2").build()))
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
        assertThat(entry.getStrategies()).extracting(Strategy::getId).containsExactlyInAnyOrder("1", "2");
        assertThat(entry.getStrategies()).extracting(Strategy::getName).containsExactlyInAnyOrder("ST1", "ST2");
        assertThat(entry.getExitDate()).isEqualTo(LocalDateTime.of(2022, 9, 21, 15, 30, 50));
        assertThat(entry.getExitPrice()).isEqualTo(BigDecimal.valueOf(250.31));
        assertThat(entry.getAccountRisked()).isNull();
        assertThat(entry.getPlannedRR()).isNull();
        assertThat(entry.getGrossResult()).isNull();
        assertThat(entry.getNetResult()).isNull();
        assertThat(entry.getAccountChange()).isNull();
        assertThat(entry.getAccountBalance()).isNull();
        assertThat(entry.isFinished()).isFalse();
    }

    @DisplayName("Given null")
    @Test
    void nullTests() {
        assertThat(TradeMapper.INSTANCE.toEntry(null, null)).isNull();

        assertThat(TradeMapper.INSTANCE.toEditEntry(null, null)).isNull();

        assertThat(TradeMapper.INSTANCE.toEditEntry(null, null)).isNull();

        assertThat(TradeMapper.INSTANCE.toEntryFromClose(null, null)).isNull();

        assertThat(TradeMapper.INSTANCE.toEntryFromClose(Entry.builder().build(), null)).isNotNull();

        assertThat(TradeMapper.INSTANCE.toEntryFromClose(null, CloseTrade.builder().build())).isNotNull();
    }
}