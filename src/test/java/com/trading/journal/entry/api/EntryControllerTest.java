package com.trading.journal.entry.api;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.allanweber.jwttoken.service.JwtResolveToken;
import com.allanweber.jwttoken.service.JwtTokenReader;
import com.trading.journal.entry.MongoDbContainerInitializer;
import com.trading.journal.entry.WithCustomMockUser;
import com.trading.journal.entry.balance.Balance;
import com.trading.journal.entry.entries.*;
import com.trading.journal.entry.entries.trade.OpenTrades;
import com.trading.journal.entry.entries.trade.Trade;
import com.trading.journal.entry.journal.Journal;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.reactive.function.BodyInserters;
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
        mongoTemplate.save(Entry.builder().price(BigDecimal.valueOf(10.00)).symbol("MSFT").type(EntryType.TRADE).netResult(BigDecimal.valueOf(100)).date(LocalDateTime.of(2022, 8, 1, 18, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(BigDecimal.valueOf(20.00)).symbol("AAPL").type(EntryType.TRADE).date(LocalDateTime.of(2022, 8, 5, 18, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(BigDecimal.valueOf(30.00)).symbol("NVDA").type(EntryType.TRADE).netResult(BigDecimal.valueOf(100)).date(LocalDateTime.of(2022, 8, 10, 18, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(BigDecimal.valueOf(40.00)).symbol("TSLA").type(EntryType.TRADE).date(LocalDateTime.of(2022, 8, 3, 18, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(BigDecimal.valueOf(50.00)).symbol("AMZN").type(EntryType.TRADE).netResult(BigDecimal.valueOf(100)).date(LocalDateTime.of(2022, 8, 2, 18, 23, 30)).build(), entryCollection);

        mongoTemplate.save(Entry.builder().price(BigDecimal.valueOf(60.00)).symbol("MSFT").type(EntryType.TRADE).date(LocalDateTime.of(2022, 8, 6, 0, 21, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(BigDecimal.valueOf(70.00)).symbol("AAPL").type(EntryType.TRADE).date(LocalDateTime.of(2022, 8, 4, 17, 22, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(BigDecimal.valueOf(80.00)).symbol("NVDA").type(EntryType.TRADE).date(LocalDateTime.of(2022, 8, 8, 16, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(BigDecimal.valueOf(90.00)).symbol("TSLA").type(EntryType.TRADE).date(LocalDateTime.of(2022, 8, 9, 15, 24, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(BigDecimal.valueOf(100.00)).symbol("AMZN").type(EntryType.TRADE).date(LocalDateTime.of(2022, 8, 7, 23, 25, 30)).build(), entryCollection);

        mongoTemplate.save(Entry.builder().price(BigDecimal.valueOf(110.00)).symbol("MSFT").type(EntryType.TRADE).date(LocalDateTime.of(2022, 9, 15, 18, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(BigDecimal.valueOf(120.31)).symbol("AAPL").type(EntryType.TRADE).date(LocalDateTime.of(2022, 9, 14, 18, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(BigDecimal.valueOf(130.00)).symbol("NVDA").type(EntryType.TRADE).date(LocalDateTime.of(2022, 9, 13, 18, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(BigDecimal.valueOf(140.59)).symbol("TSLA").type(EntryType.TRADE).date(LocalDateTime.of(2022, 9, 12, 18, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(BigDecimal.valueOf(150.00)).symbol("AMZN").type(EntryType.TRADE).date(LocalDateTime.of(2022, 9, 11, 18, 23, 30)).build(), entryCollection);

        mongoTemplate.save(Entry.builder().price(BigDecimal.valueOf(10.00)).type(EntryType.DEPOSIT).netResult(BigDecimal.valueOf(100)).date(LocalDateTime.of(2022, 10, 1, 18, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(BigDecimal.valueOf(10.00)).type(EntryType.TAXES).netResult(BigDecimal.valueOf(100)).date(LocalDateTime.of(2022, 10, 1, 18, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(BigDecimal.valueOf(10.00)).type(EntryType.WITHDRAWAL).netResult(BigDecimal.valueOf(100)).date(LocalDateTime.of(2022, 10, 1, 18, 23, 30)).build(), entryCollection);

        //GET ALL RECORDS
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
                .value(response -> {
                    assertThat(response).hasSize(18);
                    assertThat(response).extracting(Entry::getDate)
                            .extracting(LocalDateTime::getDayOfMonth)
                            .containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 1, 1, 1);
                });

        //GET BY SYMBOL
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries")
                        .queryParam("symbol", "MSFT")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<Entry>>() {
                })
                .value(response -> assertThat(response).hasSize(3));

        //GET BY TYPE TRADE
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries")
                        .queryParam("type", "TRADE")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<Entry>>() {
                })
                .value(response -> assertThat(response).hasSize(15));

        //GET BY TYPE DEPOSIT
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries")
                        .queryParam("type", "DEPOSIT")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<Entry>>() {
                })
                .value(response -> assertThat(response).hasSize(1));

        //GET BY TYPE TAXES
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries")
                        .queryParam("type", "TAXES")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<Entry>>() {
                })
                .value(response -> assertThat(response).hasSize(1));

        //GET BY TYPE WITHDRAWAL
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries")
                        .queryParam("type", "WITHDRAWAL")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<Entry>>() {
                })
                .value(response -> assertThat(response).hasSize(1));

        //GET BY STATUS CLOSED
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries")
                        .queryParam("status", "CLOSED")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<Entry>>() {
                })
                .value(response -> assertThat(response).hasSize(6));

        //GET BY STATUS OPEN
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries")
                        .queryParam("status", "OPEN")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<Entry>>() {
                })
                .value(response -> assertThat(response).hasSize(12));

        //GET BY FROM DATE
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries")
                        .queryParam("from", "2022-09-11 00:00:00")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<Entry>>() {
                })
                .value(response -> assertThat(response).hasSize(8));

        //GET BY TYPE, SYMBOL AND FROM DATE
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries")
                        .queryParam("type", "TRADE")
                        .queryParam("symbol", "TSLA")
                        .queryParam("from", "2022-09-11 00:00:00")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<Entry>>() {
                })
                .value(response -> assertThat(response).hasSize(1));

        //GET BY TYPE, SYMBOL, FROM DATE STATUS CLOSED
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries")
                        .queryParam("type", "TRADE")
                        .queryParam("symbol", "TSLA")
                        .queryParam("status", "CLOSED")
                        .queryParam("from", "2022-09-11 00:00:00")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<Entry>>() {
                })
                .value(response -> assertThat(response).hasSize(0));
    }

    @DisplayName("Get Entries by Direction")
    @Test
    void direction() {
        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("LONG")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("SHORT")
                        .direction(EntryDirection.SHORT)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries")
                        .queryParam("direction", EntryDirection.LONG)
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<Entry>>() {
                })
                .value(response -> {
                    assertThat(response).hasSize(1);
                    assertThat(response).extracting(Entry::getSymbol).containsExactly("LONG");
                });

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries")
                        .queryParam("direction", EntryDirection.SHORT)
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<Entry>>() {
                })
                .value(response -> {
                    assertThat(response).hasSize(1);
                    assertThat(response).extracting(Entry::getSymbol).containsExactly("SHORT");
                });
    }

    @DisplayName("Get Entries by result WIN or LOSE")
    @Test
    void result() {
        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("WIN")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("LOSE")
                        .direction(EntryDirection.SHORT)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(-100))
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries")
                        .queryParam("result", EntryResult.WIN)
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<Entry>>() {
                })
                .value(response -> {
                    assertThat(response).hasSize(1);
                    assertThat(response).extracting(Entry::getSymbol).containsExactly("WIN");
                });

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries")
                        .queryParam("result", EntryResult.LOSE)
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<Entry>>() {
                })
                .value(response -> {
                    assertThat(response).hasSize(1);
                    assertThat(response).extracting(Entry::getSymbol).containsExactly("LOSE");
                });
    }

    @DisplayName("Create a new Trade entry and add images to it")
    @Test
    void addImages() {
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


        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("file", new ClassPathResource("java.png"));
        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/{entry-id}/image")
                        .queryParam("type", UploadType.IMAGE_BEFORE)
                        .build(journalId, entryId.get()))
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .exchange()
                .expectStatus()
                .isOk();

        List<Entry> all = mongoTemplate.findAll(Entry.class, entryCollection);
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getScreenshotBefore()).isNotNull();
        assertThat(all.get(0).getScreenshotAfter()).isNull();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/{entry-id}/image")
                        .queryParam("type", UploadType.IMAGE_BEFORE)
                        .build(journalId, entryId.get()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk().expectBody(EntryImageResponse.class)
                .value(response -> assertThat(response.getImage()).isNotNull());

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/{entry-id}/image")
                        .queryParam("type", UploadType.IMAGE_AFTER)
                        .build(journalId, entryId.get()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk().expectBody(EntryImageResponse.class)
                .value(response -> assertThat(response.getImage()).isNull());

        bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("file", new ClassPathResource("java.png"));
        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/{entry-id}/image")
                        .queryParam("type", UploadType.IMAGE_AFTER)
                        .build(journalId, entryId.get()))
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .exchange()
                .expectStatus()
                .isOk();

        all = mongoTemplate.findAll(Entry.class, entryCollection);
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getScreenshotBefore()).isNotNull();
        assertThat(all.get(0).getScreenshotAfter()).isNotNull();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/{entry-id}/image")
                        .queryParam("type", UploadType.IMAGE_BEFORE)
                        .build(journalId, entryId.get()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk().expectBody(EntryImageResponse.class)
                .value(response -> assertThat(response.getImage()).isNotNull());

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/{entry-id}/image")
                        .queryParam("type", UploadType.IMAGE_AFTER)
                        .build(journalId, entryId.get()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk().expectBody(EntryImageResponse.class)
                .value(response -> assertThat(response.getImage()).isNotNull());
    }
}