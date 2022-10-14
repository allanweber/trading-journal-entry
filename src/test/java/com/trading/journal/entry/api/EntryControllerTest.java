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
class EntryControllerTest {

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
    }

    @BeforeEach
    public void cleanJournal() {
        mongoTemplate.dropCollection(journalCollection);
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

    @DisplayName("Get all entries from a journal must be ordered by date")
    @Test
    void all() {
        mongoTemplate.save(Entry.builder().price(BigDecimal.valueOf(10.00)).symbol("MSFT").direction(EntryDirection.LONG).date(LocalDateTime.of(2022, 8, 1, 18, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(BigDecimal.valueOf(20.00)).symbol("AAPL").direction(EntryDirection.SHORT).date(LocalDateTime.of(2022, 8, 5, 18, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(BigDecimal.valueOf(30.00)).symbol("NVDA").direction(EntryDirection.LONG).date(LocalDateTime.of(2022, 8, 10, 18, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(BigDecimal.valueOf(40.00)).symbol("TSLA").direction(EntryDirection.SHORT).date(LocalDateTime.of(2022, 8, 3, 18, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(BigDecimal.valueOf(50.00)).symbol("AMZN").direction(EntryDirection.LONG).date(LocalDateTime.of(2022, 8, 2, 18, 23, 30)).build(), entryCollection);

        mongoTemplate.save(Entry.builder().price(BigDecimal.valueOf(60.00)).symbol("MSFT").direction(EntryDirection.LONG).date(LocalDateTime.of(2022, 8, 6, 0, 21, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(BigDecimal.valueOf(70.00)).symbol("AAPL").direction(EntryDirection.SHORT).date(LocalDateTime.of(2022, 8, 4, 17, 22, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(BigDecimal.valueOf(80.00)).symbol("NVDA").direction(EntryDirection.LONG).date(LocalDateTime.of(2022, 8, 8, 16, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(BigDecimal.valueOf(90.00)).symbol("TSLA").direction(EntryDirection.SHORT).date(LocalDateTime.of(2022, 8, 9, 15, 24, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(BigDecimal.valueOf(100.00)).symbol("AMZN").direction(EntryDirection.LONG).date(LocalDateTime.of(2022, 8, 7, 23, 25, 30)).build(), entryCollection);

        mongoTemplate.save(Entry.builder().price(BigDecimal.valueOf(110.00)).symbol("MSFT").direction(EntryDirection.LONG).date(LocalDateTime.of(2022, 9, 15, 18, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(BigDecimal.valueOf(120.31)).symbol("AAPL").direction(EntryDirection.SHORT).date(LocalDateTime.of(2022, 9, 14, 18, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(BigDecimal.valueOf(130.00)).symbol("NVDA").direction(EntryDirection.LONG).date(LocalDateTime.of(2022, 9, 13, 18, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(BigDecimal.valueOf(140.59)).symbol("TSLA").direction(EntryDirection.SHORT).date(LocalDateTime.of(2022, 9, 12, 18, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(BigDecimal.valueOf(150.00)).symbol("AMZN").direction(EntryDirection.LONG).date(LocalDateTime.of(2022, 9, 11, 18, 23, 30)).build(), entryCollection);

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/entries")
                        .pathSegment("{journal-id}")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<Entry>>() {
                })
                .value(response -> {
                    assertThat(response).hasSize(15);
                    assertThat(response).extracting(Entry::getDate)
                            .extracting(LocalDateTime::getDayOfMonth)
                            .containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
                });
    }

    @DisplayName("Create a new Trade entry")
    @Test
    void createTrade() {
        Entry entry = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 1, 17, 35, 59))
                .type(EntryType.TRADE)
                .symbol("USD/EUR")
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(1.1234))
                .size(BigDecimal.valueOf(500.00))
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure(1)
                .build();

        AtomicReference<String> entryId = new AtomicReference<>();
        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/entries")
                        .pathSegment("{journal-id}")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(entry)
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

        Entry updateEntry = Entry.builder()
                .id(entryId.get())
                .date(LocalDateTime.of(2022, 9, 1, 17, 35, 59))
                .type(EntryType.TRADE)
                .symbol("USD/EUR")
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(1.1234))
                .size(BigDecimal.valueOf(500.00))
                .profitPrice(BigDecimal.valueOf(1.2345))
                .lossPrice(BigDecimal.valueOf(1.009))
                .exitPrice(BigDecimal.valueOf(1.2345))
                .costs(BigDecimal.valueOf(2.34))
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure(1)
                .build();
        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/entries")
                        .pathSegment("{journal-id}")
                        .build(journalId))
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
        Entry entry = Entry.builder()
                .type(EntryType.TRADE)
                .profitPrice(BigDecimal.valueOf(1.2345))
                .lossPrice(BigDecimal.valueOf(1.009))
                .exitPrice(BigDecimal.valueOf(1.2345))
                .costs(BigDecimal.valueOf(2.34))
                .build();

        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/entries")
                        .pathSegment("{journal-id}")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(entry)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(new ParameterizedTypeReference<Map<String, List<String>>>() {
                })
                .value(response -> {
                    assertThat(response.get("errors")).hasSize(7);
                    assertThat(response.get("errors")).contains("Date is required");
                    assertThat(response.get("errors")).contains("Symbol is required");
                    assertThat(response.get("errors")).contains("Position size is required");
                    assertThat(response.get("errors")).contains("Price is required");
                    assertThat(response.get("errors")).contains("Direction is required");
                    assertThat(response.get("errors")).contains("Graph Type is required");
                    assertThat(response.get("errors")).contains("Graph Measure is required");
                });
    }

    @DisplayName("Create a new Withdrawal entry")
    @Test
    void createWithdrawal() {
        Entry entry = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 1, 17, 35, 59))
                .type(EntryType.WITHDRAWAL)
                .price(BigDecimal.valueOf(50))
                .build();

        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/entries")
                        .pathSegment("{journal-id}")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(entry)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectHeader()
                .exists("Location")
                .expectBody(Entry.class)
                .value(response -> {
                    assertThat(response.getId()).isNotNull();
                    assertThat(response.getDate()).isEqualTo(LocalDateTime.of(2022, 9, 1, 17, 35, 59));
                    assertThat(response.getType()).isEqualTo(EntryType.WITHDRAWAL);
                    assertThat(response.getPrice()).isEqualTo(BigDecimal.valueOf(50));

                    assertThat(response.getSymbol()).isNull();
                    assertThat(response.getDirection()).isNull();
                    assertThat(response.getSize()).isNull();
                    assertThat(response.getProfitPrice()).isNull();
                    assertThat(response.getLossPrice()).isNull();
                    assertThat(response.getPlannedRR()).isNull();
                    assertThat(response.getAccountRisked()).isNull();
                    assertThat(response.getGrossResult()).isNull();

                    assertThat(response.getNetResult()).isEqualTo(BigDecimal.valueOf(-50.00).setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountChange()).isEqualTo(BigDecimal.valueOf(-0.5000).setScale(4, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountBalance()).isEqualTo(BigDecimal.valueOf(50.00).setScale(2, RoundingMode.HALF_EVEN));
                });
    }

    @DisplayName("Try to create an invalid Withdrawal entry")
    @Test
    void invalidWithdrawal() {
        Entry entry = Entry.builder()
                .type(EntryType.WITHDRAWAL)
                .build();

        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/entries")
                        .pathSegment("{journal-id}")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(entry)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(new ParameterizedTypeReference<Map<String, List<String>>>() {
                })
                .value(response -> {
                    assertThat(response.get("errors")).hasSize(2);
                    assertThat(response.get("errors")).contains("Date is required");
                    assertThat(response.get("errors")).contains("Price is required");
                });
    }

    @DisplayName("Create a new Taxes entry")
    @Test
    void createTaxes() {
        Entry entry = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 1, 17, 35, 59))
                .type(EntryType.TAXES)
                .price(BigDecimal.valueOf(50))
                .build();

        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/entries")
                        .pathSegment("{journal-id}")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(entry)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectHeader()
                .exists("Location")
                .expectBody(Entry.class)
                .value(response -> {
                    assertThat(response.getId()).isNotNull();
                    assertThat(response.getDate()).isEqualTo(LocalDateTime.of(2022, 9, 1, 17, 35, 59));
                    assertThat(response.getType()).isEqualTo(EntryType.TAXES);
                    assertThat(response.getPrice()).isEqualTo(BigDecimal.valueOf(50));

                    assertThat(response.getSymbol()).isNull();
                    assertThat(response.getDirection()).isNull();
                    assertThat(response.getSize()).isNull();
                    assertThat(response.getProfitPrice()).isNull();
                    assertThat(response.getLossPrice()).isNull();
                    assertThat(response.getPlannedRR()).isNull();
                    assertThat(response.getAccountRisked()).isNull();
                    assertThat(response.getGrossResult()).isNull();

                    assertThat(response.getNetResult()).isEqualTo(BigDecimal.valueOf(-50.00).setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountChange()).isEqualTo(BigDecimal.valueOf(-0.5000).setScale(4, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountBalance()).isEqualTo(BigDecimal.valueOf(50.00).setScale(2, RoundingMode.HALF_EVEN));
                });
    }

    @DisplayName("Try to create an invalid Taxes entry")
    @Test
    void invalidTaxes() {
        Entry entry = Entry.builder()
                .type(EntryType.TAXES)
                .build();

        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/entries")
                        .pathSegment("{journal-id}")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(entry)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(new ParameterizedTypeReference<Map<String, List<String>>>() {
                })
                .value(response -> {
                    assertThat(response.get("errors")).hasSize(2);
                    assertThat(response.get("errors")).contains("Date is required");
                    assertThat(response.get("errors")).contains("Price is required");
                });
    }

    @DisplayName("Create a new Deposit entry")
    @Test
    void createDeposit() {
        Entry entry = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 1, 17, 35, 59))
                .type(EntryType.DEPOSIT)
                .price(BigDecimal.valueOf(50))
                .build();

        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/entries")
                        .pathSegment("{journal-id}")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(entry)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectHeader()
                .exists("Location")
                .expectBody(Entry.class)
                .value(response -> {
                    assertThat(response.getId()).isNotNull();
                    assertThat(response.getDate()).isEqualTo(LocalDateTime.of(2022, 9, 1, 17, 35, 59));
                    assertThat(response.getType()).isEqualTo(EntryType.DEPOSIT);
                    assertThat(response.getPrice()).isEqualTo(BigDecimal.valueOf(50));

                    assertThat(response.getSymbol()).isNull();
                    assertThat(response.getDirection()).isNull();
                    assertThat(response.getSize()).isNull();
                    assertThat(response.getProfitPrice()).isNull();
                    assertThat(response.getLossPrice()).isNull();
                    assertThat(response.getPlannedRR()).isNull();
                    assertThat(response.getAccountRisked()).isNull();
                    assertThat(response.getGrossResult()).isNull();

                    assertThat(response.getNetResult()).isEqualTo(BigDecimal.valueOf(50.00).setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountChange()).isEqualTo(BigDecimal.valueOf(0.5000).setScale(4, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountBalance()).isEqualTo(BigDecimal.valueOf(150.00).setScale(2, RoundingMode.HALF_EVEN));
                });
    }

    @DisplayName("Try to create an invalid Deposit entry")
    @Test
    void invalidDeposit() {
        Entry entry = Entry.builder()
                .type(EntryType.DEPOSIT)
                .build();

        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/entries")
                        .pathSegment("{journal-id}")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(entry)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(new ParameterizedTypeReference<Map<String, List<String>>>() {
                })
                .value(response -> {
                    assertThat(response.get("errors")).hasSize(2);
                    assertThat(response.get("errors")).contains("Date is required");
                    assertThat(response.get("errors")).contains("Price is required");
                });
    }

    @DisplayName("Create a new entry and delete it")
    @Test
    void deleteEntry() {
        Entry entry = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 1, 17, 35, 59))
                .type(EntryType.DEPOSIT)
                .price(BigDecimal.valueOf(50))
                .build();

        AtomicReference<String> entryId = new AtomicReference<>();
        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/entries")
                        .pathSegment("{journal-id}")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(entry)
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
                        .path("/entries")
                        .pathSegment("{journal-id}")
                        .pathSegment("{entry-id}")
                        .build(journalId, entryId.get()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk();

        assertThat(mongoTemplate.findAll(Entry.class, entryCollection)).isEmpty();
    }
}