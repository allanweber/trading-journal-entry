package com.trading.journal.entry.api;

import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.EntryDirection;
import com.trading.journal.entry.entries.EntryType;
import com.trading.journal.entry.entries.GraphType;
import com.trading.journal.entry.entries.trade.CloseTrade;
import com.trading.journal.entry.entries.trade.OpenTrades;
import com.trading.journal.entry.entries.trade.Symbol;
import com.trading.journal.entry.entries.trade.Trade;
import com.trading.journal.entry.journal.Currency;
import com.trading.journal.entry.journal.Journal;
import com.trading.journal.entry.strategy.Strategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import tooling.IntegratedTestWithJournal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TradeControllerTest extends IntegratedTestWithJournal {

    private static final String entryCollection = "TestTenancy_entries";

    private static final String strategyCollection = "TestTenancy_strategies";

    @BeforeEach
    public void beforeEach() {
        mongoTemplate.dropCollection(entryCollection);
        mongoTemplate.dropCollection(strategyCollection);
    }

    @DisplayName("Create a new Trade entry")
    @Test
    void createTrade() {
        Trade trade = Trade.builder()
                .date(LocalDateTime.of(2022, 9, 1, 17, 35, 59))
                .symbol("USD/EUR")
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(1.1234))
                .size(BigDecimal.valueOf(500.00))
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure("1M")
                .build();

        AtomicReference<String> entryId = new AtomicReference<>();
        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/trade")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(trade)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectHeader()
                .exists("Location")
                .expectBody(Entry.class)
                .value(response -> {
                    assertThat(response.getId()).isNotNull();
                    entryId.set(response.getId());
                    assertThat(response.getDate()).isEqualTo(LocalDateTime.of(2022, 9, 1, 17, 35, 59));
                    assertThat(response.getType()).isEqualTo(EntryType.TRADE);
                    assertThat(response.getSymbol()).isEqualTo("USD/EUR");
                    assertThat(response.getDirection()).isEqualTo(EntryDirection.LONG);
                    assertThat(response.getPrice()).isEqualTo(BigDecimal.valueOf(1.1234));
                    assertThat(response.getSize()).isEqualTo(BigDecimal.valueOf(500.00));
                    assertThat(response.getPlannedRR()).isEqualTo(BigDecimal.valueOf(-1.00).setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountRisked()).isEqualTo(BigDecimal.valueOf(0.0562).setScale(4, RoundingMode.HALF_EVEN));

                    assertThat(response.getProfitPrice()).isNull();
                    assertThat(response.getLossPrice()).isNull();
                    assertThat(response.getGrossResult()).isNull();
                    assertThat(response.getNetResult()).isNull();
                    assertThat(response.getAccountChange()).isNull();
                    assertThat(response.getAccountBalance()).isNull();
                });

        Trade updateEntry = Trade.builder()
                .date(LocalDateTime.of(2022, 9, 1, 17, 35, 59))
                .symbol("USD/EUR")
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(1.1234))
                .size(BigDecimal.valueOf(500.00))
                .profitPrice(BigDecimal.valueOf(1.2345))
                .lossPrice(BigDecimal.valueOf(1.009))
                .costs(BigDecimal.valueOf(2.34))
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure("1M")
                .build();
        webTestClient
                .patch()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/trade/{trade-id}")
                        .build(journalId, entryId.get()))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(updateEntry)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Entry.class)
                .value(response -> {
                    assertThat(response.getId()).isEqualTo(entryId.get());
                    assertThat(response.getDate()).isEqualTo(LocalDateTime.of(2022, 9, 1, 17, 35, 59));
                    assertThat(response.getType()).isEqualTo(EntryType.TRADE);
                    assertThat(response.getSymbol()).isEqualTo("USD/EUR");
                    assertThat(response.getDirection()).isEqualTo(EntryDirection.LONG);
                    assertThat(response.getPrice()).isEqualTo(BigDecimal.valueOf(1.1234));
                    assertThat(response.getSize()).isEqualTo(BigDecimal.valueOf(500.00));
                    assertThat(response.getProfitPrice()).isEqualTo(BigDecimal.valueOf(1.2345));
                    assertThat(response.getLossPrice()).isEqualTo(BigDecimal.valueOf(1.009));

                    assertThat(response.getPlannedRR()).isEqualTo(BigDecimal.valueOf(0.97));
                    assertThat(response.getAccountRisked()).isEqualTo(BigDecimal.valueOf(0.0057).setScale(4, RoundingMode.HALF_EVEN));
                });

        CloseTrade closeEntry = CloseTrade.builder()
                .exitPrice(BigDecimal.valueOf(1.2345))
                .exitDate(LocalDateTime.of(2022, 9, 1, 18, 35, 59))
                .build();
        webTestClient
                .patch()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/trade/{trade-id}/close")
                        .build(journalId, entryId.get()))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(closeEntry)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Entry.class)
                .value(response -> {
                    assertThat(response.getId()).isEqualTo(entryId.get());
                    assertThat(response.getDate()).isEqualTo(LocalDateTime.of(2022, 9, 1, 17, 35, 59));
                    assertThat(response.getType()).isEqualTo(EntryType.TRADE);
                    assertThat(response.getSymbol()).isEqualTo("USD/EUR");
                    assertThat(response.getDirection()).isEqualTo(EntryDirection.LONG);
                    assertThat(response.getPrice()).isEqualTo(BigDecimal.valueOf(1.1234));
                    assertThat(response.getSize()).isEqualTo(BigDecimal.valueOf(500.00));
                    assertThat(response.getProfitPrice()).isEqualTo(BigDecimal.valueOf(1.2345));
                    assertThat(response.getLossPrice()).isEqualTo(BigDecimal.valueOf(1.009));

                    assertThat(response.getPlannedRR()).isEqualTo(BigDecimal.valueOf(0.97));
                    assertThat(response.getAccountRisked()).isEqualTo(BigDecimal.valueOf(0.0057).setScale(4, RoundingMode.HALF_EVEN));
                    assertThat(response.getGrossResult()).isEqualTo(BigDecimal.valueOf(55.55).setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(response.getNetResult()).isEqualTo(BigDecimal.valueOf(53.21).setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountChange()).isEqualTo(BigDecimal.valueOf(0.0053).setScale(4, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountBalance()).isEqualTo(BigDecimal.valueOf(10053.21).setScale(2, RoundingMode.HALF_EVEN));
                });

        List<Entry> all = mongoTemplate.findAll(Entry.class, entryCollection);
        assertThat(all).hasSize(1);

        List<Strategy> strategies = mongoTemplate.findAll(Strategy.class, strategyCollection);
        assertThat(strategies).isEmpty();
    }

    @DisplayName("Create a new Trade entry with strategies")
    @Test
    void createTradeWithStrategies() {

        Strategy strategy1 = mongoTemplate.save(Strategy.builder().name("Strategy1").build(), strategyCollection);
        Strategy strategy2 = mongoTemplate.save(Strategy.builder().name("Strategy2").build(), strategyCollection);

        Trade trade = Trade.builder()
                .date(LocalDateTime.of(2022, 9, 1, 17, 35, 59))
                .symbol("USD/EUR")
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(1.1234))
                .size(BigDecimal.valueOf(500.00))
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure("1M")
                .strategyIds(asList(strategy1.getId(), strategy2.getId()))
                .build();

        AtomicReference<String> entryId = new AtomicReference<>();
        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/trade")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(trade)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectHeader()
                .exists("Location")
                .expectBody(Entry.class)
                .value(response -> {
                    assertThat(response.getId()).isNotNull();
                    entryId.set(response.getId());
                    assertThat(response.getDate()).isEqualTo(LocalDateTime.of(2022, 9, 1, 17, 35, 59));
                    assertThat(response.getType()).isEqualTo(EntryType.TRADE);
                    assertThat(response.getSymbol()).isEqualTo("USD/EUR");
                    assertThat(response.getDirection()).isEqualTo(EntryDirection.LONG);
                    assertThat(response.getPrice()).isEqualTo(BigDecimal.valueOf(1.1234));
                    assertThat(response.getSize()).isEqualTo(BigDecimal.valueOf(500.00));
                    assertThat(response.getPlannedRR()).isEqualTo(BigDecimal.valueOf(-1.00).setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(response.getStrategies()).extracting(Strategy::getId).containsExactlyInAnyOrder(strategy1.getId(), strategy2.getId());
                    assertThat(response.getStrategies()).extracting(Strategy::getName).containsExactlyInAnyOrder(strategy1.getName(), strategy2.getName());

                    assertThat(response.getProfitPrice()).isNull();
                    assertThat(response.getLossPrice()).isNull();
                    assertThat(response.getGrossResult()).isNull();
                    assertThat(response.getNetResult()).isNull();
                    assertThat(response.getAccountChange()).isNull();
                    assertThat(response.getAccountBalance()).isNull();
                });

        Trade updateEntry = Trade.builder()
                .date(LocalDateTime.of(2022, 9, 1, 17, 35, 59))
                .symbol("USD/EUR")
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(1.1234))
                .size(BigDecimal.valueOf(500.00))
                .profitPrice(BigDecimal.valueOf(1.2345))
                .lossPrice(BigDecimal.valueOf(1.009))
                .costs(BigDecimal.valueOf(2.34))
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure("1M")
                .build();
        webTestClient
                .patch()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/trade/{trade-id}")
                        .build(journalId, entryId.get()))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(updateEntry)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Entry.class)
                .value(response -> {
                    assertThat(response.getId()).isEqualTo(entryId.get());
                    assertThat(response.getDate()).isEqualTo(LocalDateTime.of(2022, 9, 1, 17, 35, 59));
                    assertThat(response.getType()).isEqualTo(EntryType.TRADE);
                    assertThat(response.getSymbol()).isEqualTo("USD/EUR");
                    assertThat(response.getDirection()).isEqualTo(EntryDirection.LONG);
                    assertThat(response.getPrice()).isEqualTo(BigDecimal.valueOf(1.1234));
                    assertThat(response.getSize()).isEqualTo(BigDecimal.valueOf(500.00));
                    assertThat(response.getProfitPrice()).isEqualTo(BigDecimal.valueOf(1.2345));
                    assertThat(response.getLossPrice()).isEqualTo(BigDecimal.valueOf(1.009));
                });

        CloseTrade closeEntry = CloseTrade.builder()
                .exitPrice(BigDecimal.valueOf(1.2345))
                .exitDate(LocalDateTime.of(2022, 9, 1, 18, 35, 59))
                .build();
        webTestClient
                .patch()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/trade/{trade-id}/close")
                        .build(journalId, entryId.get()))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(closeEntry)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Entry.class)
                .value(response -> {
                    assertThat(response.getId()).isEqualTo(entryId.get());
                    assertThat(response.getDate()).isEqualTo(LocalDateTime.of(2022, 9, 1, 17, 35, 59));
                    assertThat(response.getType()).isEqualTo(EntryType.TRADE);
                    assertThat(response.getSymbol()).isEqualTo("USD/EUR");
                    assertThat(response.getDirection()).isEqualTo(EntryDirection.LONG);
                    assertThat(response.getPrice()).isEqualTo(BigDecimal.valueOf(1.1234));
                    assertThat(response.getSize()).isEqualTo(BigDecimal.valueOf(500.00));
                    assertThat(response.getProfitPrice()).isEqualTo(BigDecimal.valueOf(1.2345));
                    assertThat(response.getLossPrice()).isEqualTo(BigDecimal.valueOf(1.009));

                    assertThat(response.getGrossResult()).isEqualTo(BigDecimal.valueOf(55.55).setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(response.getNetResult()).isEqualTo(BigDecimal.valueOf(53.21).setScale(2, RoundingMode.HALF_EVEN));
                });

        List<Entry> all = mongoTemplate.findAll(Entry.class, entryCollection);
        assertThat(all).hasSize(1);
    }

    @DisplayName("Create a new Trade entry with invalid strategy")
    @Test
    void createTradeWithInvalidStrategies() {
        String strategyId = UUID.randomUUID().toString();
        Trade trade = Trade.builder()
                .date(LocalDateTime.of(2022, 9, 1, 17, 35, 59))
                .symbol("USD/EUR")
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(1.1234))
                .size(BigDecimal.valueOf(500.00))
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure("1M")
                .strategyIds(singletonList(strategyId))
                .build();

        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/trade")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(trade)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response -> assertThat(response.get("error")).isEqualTo("Invalid Strategy " + strategyId));
    }

    @DisplayName("Try to create an invalid Trade entry")
    @Test
    void invalidTrade() {
        Trade trade = Trade.builder()
                .profitPrice(BigDecimal.valueOf(1.2345))
                .lossPrice(BigDecimal.valueOf(1.009))
                .costs(BigDecimal.valueOf(2.34))
                .build();

        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/trade")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(trade)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(new ParameterizedTypeReference<Map<String, List<String>>>() {
                })
                .value(response -> {
                    assertThat(response.get("errors")).hasSize(5);
                    assertThat(response.get("errors")).contains("Date is required");
                    assertThat(response.get("errors")).contains("Symbol is required");
                    assertThat(response.get("errors")).contains("Position size is required");
                    assertThat(response.get("errors")).contains("Price is required");
                    assertThat(response.get("errors")).contains("Direction is required");
                });
    }

    @DisplayName("Create a new entry and delete it")
    @Test
    void deleteEntry() {
        Trade trade = Trade.builder()
                .date(LocalDateTime.of(2022, 9, 1, 17, 35, 59))
                .symbol("USD/EUR")
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(1.1234))
                .size(BigDecimal.valueOf(500.00))
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure("1M")
                .build();

        AtomicReference<String> entryId = new AtomicReference<>();
        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/trade")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(trade)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectHeader()
                .exists("Location")
                .expectBody(Entry.class)
                .value(response -> {
                    assertThat(response.getId()).isNotNull();
                    entryId.set(response.getId());
                });

        assertThat(mongoTemplate.findAll(Entry.class, entryCollection)).hasSize(1);

        webTestClient
                .delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/{entry-id}")
                        .build(journalId, entryId.get()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk();

        assertThat(mongoTemplate.findAll(Entry.class, entryCollection)).isEmpty();
    }

    @DisplayName("Count Open trades")
    @Test
    void open() {
        mongoTemplate.save(
                Entry.builder()
                        .journalId(journalId)
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("CLOSED")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .journalId(journalId)
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("OPEN")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .journalId(journalId)
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("OPEN")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .journalId(journalId)
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("CLOSED")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .exitDate(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .netResult(BigDecimal.valueOf(10.00))
                        .build(),
                entryCollection);

        Journal anotherJournal = Journal.builder().name("ANOTHER-JOURNAL").startBalance(BigDecimal.valueOf(100.00)).startJournal(LocalDateTime.now()).currency(Currency.DOLLAR).build();
        mongoTemplate.save(anotherJournal, journalCollection);

        mongoTemplate.save(
                Entry.builder()
                        .journalId(anotherJournal.getId())
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("OPEN")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .journalId(anotherJournal.getId())
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("CLOSED")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .exitDate(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .netResult(BigDecimal.valueOf(10.00))
                        .build(),
                entryCollection);

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/trade/open")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(OpenTrades.class)
                .value(response ->
                        assertThat(response.getTrades()).isEqualTo(2L)
                );

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/trade/open")
                        .build(anotherJournal.getId()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(OpenTrades.class)
                .value(response ->
                        assertThat(response.getTrades()).isEqualTo(1L)
                );
    }

    @DisplayName("Get symbols")
    @Test
    void symbols() {
        mongoTemplate.save(
                Entry.builder()
                        .journalId(journalId)
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("MSFT")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .journalId(journalId)
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("MSFT")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .journalId(journalId)
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("MSFT")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .journalId(journalId)
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("TSLA")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .journalId(journalId)
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("APPL")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);

        Journal anotherJournal = Journal.builder().name("ANOTHER-JOURNAL").startBalance(BigDecimal.valueOf(100.00)).startJournal(LocalDateTime.now()).currency(Currency.DOLLAR).build();
        mongoTemplate.save(anotherJournal, journalCollection);

        mongoTemplate.save(
                Entry.builder()
                        .journalId(anotherJournal.getId())
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("APPL")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .journalId(anotherJournal.getId())
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("TSLA")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/trade/symbols")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<Symbol>>() {
                })
                .value(response -> {
                    assertThat(response).hasSize(3);
                    assertThat(response).extracting(Symbol::getName)
                            .containsExactlyInAnyOrder("MSFT", "TSLA", "APPL");
                });

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/trade/symbols")
                        .build(anotherJournal.getId()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<Symbol>>() {
                })
                .value(response -> {
                    assertThat(response).hasSize(2);
                    assertThat(response).extracting(Symbol::getName)
                            .containsExactlyInAnyOrder("TSLA", "APPL");
                });
    }
}