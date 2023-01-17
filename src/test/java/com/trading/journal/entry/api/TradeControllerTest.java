package com.trading.journal.entry.api;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.allanweber.jwttoken.service.JwtResolveToken;
import com.allanweber.jwttoken.service.JwtTokenReader;
import com.trading.journal.entry.MongoDbContainerInitializer;
import com.trading.journal.entry.WithCustomMockUser;
import com.trading.journal.entry.balance.Balance;
import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.EntryDirection;
import com.trading.journal.entry.entries.EntryType;
import com.trading.journal.entry.entries.GraphType;
import com.trading.journal.entry.entries.trade.CloseTrade;
import com.trading.journal.entry.entries.trade.OpenTrades;
import com.trading.journal.entry.entries.trade.Symbol;
import com.trading.journal.entry.entries.trade.Trade;
import com.trading.journal.entry.entries.trade.aggregate.AggregateType;
import com.trading.journal.entry.entries.trade.aggregate.PeriodAggregated;
import com.trading.journal.entry.entries.trade.aggregate.PeriodAggregatedResult;
import com.trading.journal.entry.journal.Journal;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = MongoDbContainerInitializer.class)
@WithCustomMockUser(tenancyName = "paging-tenancy")
class TradeControllerTest {

    private static String journalId;

    private static String journalCollection;

    private static String entryCollection;

    @MockBean
    JwtTokenReader tokenReader;

    @MockBean
    JwtResolveToken resolveToken;

    @Autowired
    MongoTemplate mongoTemplate;

    private static WebTestClient webTestClient;

    @BeforeAll
    public static void setUp(@Autowired WebApplicationContext applicationContext, @Autowired MongoTemplate mongoTemplate) {
        webTestClient = MockMvcWebTestClient.bindToApplicationContext(applicationContext).build();

        journalCollection = "PagingTenancy_journals";
        entryCollection = "PagingTenancy_JOURNAL-1_entries";

        Journal journal = mongoTemplate.save(Journal.builder().name("JOURNAL-1").startBalance(BigDecimal.valueOf(100))
                .currentBalance(
                        Balance.builder()
                                .accountBalance(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN))
                                .taxes(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                                .withdrawals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                                .deposits(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                                .closedPositions(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                                .build()
                )
                .build(), journalCollection);
        journalId = journal.getId();
    }

    @AfterAll
    public static void shutDown(@Autowired MongoTemplate mongoTemplate) {
        mongoTemplate.dropCollection(journalCollection);
    }

    @AfterEach
    public void afterEach() {
        mongoTemplate.dropCollection(entryCollection);
    }

    @BeforeEach
    public void mockAccessTokenInfo() {
        when(resolveToken.resolve(any())).thenReturn("token");
        when(tokenReader.getAccessTokenInfo(anyString()))
                .thenReturn(new AccessTokenInfo("user", 1L, "Paging-Tenancy", singletonList("ROLE_USER")));
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
                    assertThat(response.getAccountRisked()).isEqualTo(BigDecimal.valueOf(5.6170).setScale(4, RoundingMode.HALF_EVEN));

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
                    assertThat(response.getAccountRisked()).isEqualTo(BigDecimal.valueOf(0.5720).setScale(4, RoundingMode.HALF_EVEN));
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
                    assertThat(response.getAccountRisked()).isEqualTo(BigDecimal.valueOf(0.5720).setScale(4, RoundingMode.HALF_EVEN));
                    assertThat(response.getGrossResult()).isEqualTo(BigDecimal.valueOf(55.55).setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(response.getNetResult()).isEqualTo(BigDecimal.valueOf(53.21).setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountChange()).isEqualTo(BigDecimal.valueOf(0.5321).setScale(4, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountBalance()).isEqualTo(BigDecimal.valueOf(153.21).setScale(2, RoundingMode.HALF_EVEN));
                });

        List<Entry> all = mongoTemplate.findAll(Entry.class, entryCollection);
        assertThat(all).hasSize(1);
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
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("OPEN")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("OPEN")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
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
    }

    @DisplayName("Get symbols")
    @Test
    void symbols() {
        mongoTemplate.save(
                Entry.builder()
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
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("MSFT")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("MSFT")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("TSLA")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("APPL")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<Entry>>() {
                })
                .value(response -> assertThat(response).hasSize(5));

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
    }

    @DisplayName("Aggregate time period day")
    @Test
    void aggregateDay() {

        //Save deposit, withdrawal and taxes, but they are not considered while aggregating
        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .type(EntryType.DEPOSIT)
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);
        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .type(EntryType.TAXES)
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);
        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .type(EntryType.WITHDRAWAL)
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("MSFT")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .exitDate(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("MSFT")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2022, 1, 2, 1, 1, 0))
                        .exitDate(LocalDateTime.of(2022, 1, 2, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("PPE")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2022, 2, 2, 1, 1, 0))
                        .exitDate(LocalDateTime.of(2022, 2, 2, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("BMY")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2022, 3, 2, 1, 1, 0))
                        .exitDate(LocalDateTime.of(2022, 3, 2, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("ABBV")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2022, 3, 2, 2, 1, 0))
                        .exitDate(LocalDateTime.of(2022, 3, 2, 2, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("HD")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2022, 3, 3, 3, 1, 0))
                        .exitDate(LocalDateTime.of(2022, 3, 3, 3, 1, 0))
                        .build(),
                entryCollection);

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/trade/aggregate/time")
                        .queryParam("aggregation", AggregateType.DAY.name())
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<PeriodAggregatedResult>>() {
                })
                .value(response -> {
                    assertThat(response).hasSize(3);

                    assertThat(response.get(0).getGroup()).isEqualTo("2022-03");
                    assertThat(response.get(0).getItems()).hasSize(2);
                    assertThat(response.get(0).getItems()).extracting(PeriodAggregated::getGroup).containsExactly("2022-03-03", "2022-03-02");
                    assertThat(response.get(0).getItems()).extracting(PeriodAggregated::getCount).containsExactly(1L, 2L);
                    assertThat(response.get(0).getItems()).extracting(PeriodAggregated::getResult).containsExactly(BigDecimal.valueOf(100.00), BigDecimal.valueOf(200.00));

                    assertThat(response.get(1).getGroup()).isEqualTo("2022-02");
                    assertThat(response.get(1).getItems()).hasSize(1);
                    assertThat(response.get(1).getItems()).extracting(PeriodAggregated::getGroup).containsExactly("2022-02-02");
                    assertThat(response.get(1).getItems()).extracting(PeriodAggregated::getCount).containsExactly(1L);
                    assertThat(response.get(1).getItems()).extracting(PeriodAggregated::getResult).containsExactly(BigDecimal.valueOf(100.00));

                    assertThat(response.get(2).getGroup()).isEqualTo("2022-01");
                    assertThat(response.get(2).getItems()).hasSize(2);
                    assertThat(response.get(2).getItems()).extracting(PeriodAggregated::getGroup).containsExactly("2022-01-02", "2022-01-01");
                    assertThat(response.get(2).getItems()).extracting(PeriodAggregated::getCount).containsExactly(1L, 1L);
                    assertThat(response.get(2).getItems()).extracting(PeriodAggregated::getResult).containsExactly(BigDecimal.valueOf(100.00), BigDecimal.valueOf(100.00));
                });
    }

    @DisplayName("Aggregate time period by WEEK")
    @Test
    void aggregateWeek() {

        //Save deposit, withdrawal and taxes, but they are not considered while aggregating
        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .type(EntryType.DEPOSIT)
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);
        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .type(EntryType.TAXES)
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);
        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .type(EntryType.WITHDRAWAL)
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("MSFT")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2022, 5, 2, 1, 1, 0))
                        .exitDate(LocalDateTime.of(2022, 5, 2, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("MSFT")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2022, 5, 3, 1, 1, 0))
                        .exitDate(LocalDateTime.of(2022, 5, 3, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("PPE")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2022, 8, 2, 1, 1, 0))
                        .exitDate(LocalDateTime.of(2022, 8, 2, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("BMY")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2022, 10, 4, 1, 1, 0))
                        .exitDate(LocalDateTime.of(2022, 10, 4, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("ABBV")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2022, 10, 4, 2, 1, 0))
                        .exitDate(LocalDateTime.of(2022, 10, 4, 2, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("HD")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2022, 10, 5, 3, 1, 0))
                        .exitDate(LocalDateTime.of(2022, 10, 5, 3, 1, 0))
                        .build(),
                entryCollection);

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/trade/aggregate/time")
                        .queryParam("aggregation", AggregateType.WEEK.name())
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<PeriodAggregatedResult>>() {
                })
                .value(response -> {
                    assertThat(response).hasSize(3);

                    assertThat(response.get(0).getGroup()).isEqualTo("2022-10");
                    assertThat(response.get(0).getItems()).hasSize(1);
                    assertThat(response.get(0).getItems()).extracting(PeriodAggregated::getGroup).containsExactly("2022-40");
                    assertThat(response.get(0).getItems()).extracting(PeriodAggregated::getCount).containsExactly(3L);
                    assertThat(response.get(0).getItems()).extracting(PeriodAggregated::getResult).containsExactly(BigDecimal.valueOf(300.00));

                    assertThat(response.get(1).getGroup()).isEqualTo("2022-08");
                    assertThat(response.get(1).getItems()).hasSize(1);
                    assertThat(response.get(1).getItems()).extracting(PeriodAggregated::getGroup).containsExactly("2022-31");
                    assertThat(response.get(1).getItems()).extracting(PeriodAggregated::getCount).containsExactly(1L);
                    assertThat(response.get(1).getItems()).extracting(PeriodAggregated::getResult).containsExactly(BigDecimal.valueOf(100.00));

                    assertThat(response.get(2).getGroup()).isEqualTo("2022-05");
                    assertThat(response.get(2).getItems()).hasSize(1);
                    assertThat(response.get(2).getItems()).extracting(PeriodAggregated::getGroup).containsExactly("2022-18");
                    assertThat(response.get(2).getItems()).extracting(PeriodAggregated::getCount).containsExactly(2L);
                    assertThat(response.get(2).getItems()).extracting(PeriodAggregated::getResult).containsExactly(BigDecimal.valueOf(200.00));
                });
    }

    @DisplayName("Aggregate time period by MONTH")
    @Test
    void aggregateMonth() {

        //Save deposit, withdrawal and taxes, but they are not considered while aggregating
        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .type(EntryType.DEPOSIT)
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);
        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .type(EntryType.TAXES)
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);
        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .type(EntryType.WITHDRAWAL)
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("MSFT")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2022, 5, 2, 1, 1, 0))
                        .exitDate(LocalDateTime.of(2022, 5, 2, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("MSFT")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2022, 5, 3, 1, 1, 0))
                        .exitDate(LocalDateTime.of(2022, 5, 3, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("PPE")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2022, 8, 2, 1, 1, 0))
                        .exitDate(LocalDateTime.of(2022, 8, 2, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("BMY")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2023, 1, 4, 1, 1, 0))
                        .exitDate(LocalDateTime.of(2023, 1, 4, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("ABBV")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2023, 2, 4, 2, 1, 0))
                        .exitDate(LocalDateTime.of(2023, 2, 4, 2, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("HD")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2023, 2, 5, 3, 1, 0))
                        .exitDate(LocalDateTime.of(2023, 2, 5, 3, 1, 0))
                        .build(),
                entryCollection);

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/trade/aggregate/time")
                        .queryParam("aggregation", AggregateType.MONTH.name())
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<PeriodAggregatedResult>>() {
                })
                .value(response -> {
                    assertThat(response).hasSize(2);

                    assertThat(response.get(0).getGroup()).isEqualTo("2023");
                    assertThat(response.get(0).getItems()).hasSize(2);
                    assertThat(response.get(0).getItems()).extracting(PeriodAggregated::getGroup).containsExactly("2023-02", "2023-01");
                    assertThat(response.get(0).getItems()).extracting(PeriodAggregated::getCount).containsExactly(2L, 1L);
                    assertThat(response.get(0).getItems()).extracting(PeriodAggregated::getResult).containsExactly(BigDecimal.valueOf(200.00), BigDecimal.valueOf(100.00));

                    assertThat(response.get(1).getGroup()).isEqualTo("2022");
                    assertThat(response.get(1).getItems()).hasSize(2);
                    assertThat(response.get(1).getItems()).extracting(PeriodAggregated::getGroup).containsExactly("2022-08", "2022-05");
                    assertThat(response.get(1).getItems()).extracting(PeriodAggregated::getCount).containsExactly(1L, 2L);
                    assertThat(response.get(1).getItems()).extracting(PeriodAggregated::getResult).containsExactly(BigDecimal.valueOf(100.00), BigDecimal.valueOf(200.00));
                });
    }
}