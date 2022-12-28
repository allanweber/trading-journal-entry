package com.trading.journal.entry.entries.trade.impl;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.entries.*;
import com.trading.journal.entry.entries.trade.Symbol;
import com.trading.journal.entry.entries.trade.Trade;
import com.trading.journal.entry.journal.Journal;
import com.trading.journal.entry.journal.JournalService;
import com.trading.journal.entry.queries.CollectionName;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class TradeServiceImplTest {

    private static final AccessTokenInfo ACCESS_TOKEN = new AccessTokenInfo("subject", 1L, "TENANCY", emptyList());
    private static final String JOURNAL_ID = UUID.randomUUID().toString();

    private static CollectionName collectionName;

    @Mock
    EntryService entryService;

    @Mock
    EntryRepository repository;

    @Mock
    JournalService journalService;

    @InjectMocks
    TradeServiceImpl tradeService;

    @BeforeAll
    static void setUp() {
        collectionName = new CollectionName(ACCESS_TOKEN, "my-journal");
    }

    @DisplayName("Create a entry from a Trade")
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

        Entry entry = Entry.builder()
                .type(EntryType.TRADE)
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

        when(entryService.save(ACCESS_TOKEN, JOURNAL_ID, entry)).thenReturn(entry);

        Entry entryCreated = tradeService.create(ACCESS_TOKEN, JOURNAL_ID, trade);

        assertThat(entryCreated).isNotNull();
    }

    @DisplayName("Update a entry from a Trade")
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

        String entryId = UUID.randomUUID().toString();
        Entry entry = Entry.builder()
                .id(entryId)
                .type(EntryType.TRADE)
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

        when(entryService.save(ACCESS_TOKEN, JOURNAL_ID, entry)).thenReturn(entry);

        Entry entryCreated = tradeService.update(ACCESS_TOKEN, JOURNAL_ID, entryId, trade);

        assertThat(entryCreated).isNotNull();
    }

    @DisplayName("Count open trades")
    @Test
    void countOpen() {
        Query query = Query.query(new Criteria("type").is(EntryType.TRADE).and("netResult").exists(false));

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(repository.count(query, collectionName)).thenReturn(1L);

        long open = tradeService.countOpen(ACCESS_TOKEN, JOURNAL_ID);
        assertThat(open).isEqualTo(1L);
    }

    @DisplayName("Distinct Symbols")
    @Test
    void symbols() {
        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(repository.distinct("symbol", collectionName)).thenReturn(asList("A", "b"));

        List<Symbol> symbols = tradeService.symbols(ACCESS_TOKEN, JOURNAL_ID);
        assertThat(symbols).extracting(Symbol::getName).containsExactly("A", "b");
    }
}