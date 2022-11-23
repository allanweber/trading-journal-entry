package com.trading.journal.entry.entries.trade.impl;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.entries.*;
import com.trading.journal.entry.entries.trade.Trade;
import com.trading.journal.entry.entries.trade.impl.TradeServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class TradeServiceImplTest {

    private static final AccessTokenInfo ACCESS_TOKEN = new AccessTokenInfo("subject", 1L, "TENANCY", emptyList());
    private static final String JOURNAL_ID = UUID.randomUUID().toString();

    @Mock
    EntryService entryService;

    @InjectMocks
    TradeServiceImpl tradeService;

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
}