package com.trading.journal.entry.api;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.allanweber.jwttoken.service.JwtResolveToken;
import com.allanweber.jwttoken.service.JwtTokenReader;
import com.trading.journal.entry.MongoDbContainerInitializer;
import com.trading.journal.entry.WithCustomMockUser;
import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.EntryDirection;
import com.trading.journal.entry.entries.EntryType;
import com.trading.journal.entry.journal.Journal;
import com.trading.journal.entry.queries.data.PageResponse;
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

import java.time.LocalDateTime;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = MongoDbContainerInitializer.class)
@WithCustomMockUser(tenancyName = "paging-tenancy")
class EntryControllerPagingTest {

    private static final String TENANCY = "paging-tenancy";
    private static String journalId;

    private static String journalCollection;

    private static String entryCollection;

    @MockBean
    JwtTokenReader tokenReader;

    @MockBean
    JwtResolveToken resolveToken;

    private static WebTestClient webTestClient;

    @BeforeAll
    public static void setUp(@Autowired WebApplicationContext applicationContext, @Autowired MongoTemplate mongoTemplate) {
        webTestClient = MockMvcWebTestClient.bindToApplicationContext(applicationContext).build();

        journalCollection = TENANCY.concat("_").concat("journals");
        Journal journal = mongoTemplate.save(Journal.builder().name("JOURNAL-1").build(), journalCollection);
        journalId = journal.getId();

        entryCollection = TENANCY.concat("_").concat(journal.getName()).concat("_").concat("entries");
        mongoTemplate.save(Entry.builder().price(10.00).symbol("MSFT").direction(EntryDirection.LONG).date(LocalDateTime.of(2022, 8, 29, 18, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(20.00).symbol("AAPL").direction(EntryDirection.SHORT).date(LocalDateTime.of(2022, 8, 29, 18, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(30.00).symbol("NVDA").direction(EntryDirection.LONG).date(LocalDateTime.of(2022, 8, 29, 18, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(40.00).symbol("TSLA").direction(EntryDirection.SHORT).date(LocalDateTime.of(2022, 8, 29, 18, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(50.00).symbol("AMZN").direction(EntryDirection.LONG).date(LocalDateTime.of(2022, 8, 29, 18, 23, 30)).build(), entryCollection);

        mongoTemplate.save(Entry.builder().price(60.00).symbol("MSFT").direction(EntryDirection.LONG).date(LocalDateTime.of(2022, 8, 31, 0, 21, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(70.00).symbol("AAPL").direction(EntryDirection.SHORT).date(LocalDateTime.of(2022, 8, 31, 17, 22, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(80.00).symbol("NVDA").direction(EntryDirection.LONG).date(LocalDateTime.of(2022, 8, 31, 16, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(90.00).symbol("TSLA").direction(EntryDirection.SHORT).date(LocalDateTime.of(2022, 8, 31, 15, 24, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(100.00).symbol("AMZN").direction(EntryDirection.LONG).date(LocalDateTime.of(2022, 8, 31, 23, 25, 30)).build(), entryCollection);

        mongoTemplate.save(Entry.builder().price(110.00).symbol("MSFT").direction(EntryDirection.LONG).date(LocalDateTime.of(2022, 9, 1, 18, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(120.31).symbol("AAPL").direction(EntryDirection.SHORT).date(LocalDateTime.of(2022, 9, 1, 18, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(130.00).symbol("NVDA").direction(EntryDirection.LONG).date(LocalDateTime.of(2022, 9, 1, 18, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(140.59).symbol("TSLA").direction(EntryDirection.SHORT).date(LocalDateTime.of(2022, 9, 1, 18, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(150.00).symbol("AMZN").direction(EntryDirection.LONG).date(LocalDateTime.of(2022, 9, 1, 18, 23, 30)).build(), entryCollection);
    }

    @AfterAll
    public static void shutDown(@Autowired MongoTemplate mongoTemplate) {
        mongoTemplate.dropCollection(entryCollection);
        mongoTemplate.dropCollection(journalCollection);
    }

    @BeforeEach
    public void mockAccessTokenInfo() {
        when(resolveToken.resolve(any())).thenReturn("token");
        when(tokenReader.getAccessTokenInfo(anyString()))
                .thenReturn(new AccessTokenInfo("user", 1L, TENANCY, singletonList("ROLE_USER")));
    }

    @DisplayName("Entry get 5 items without sort or filter")
    @Test
    void noSortOrFilter() {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/entries")
                        .pathSegment("{journal-id}")
                        .queryParam("page", "0")
                        .queryParam("size", "5")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<Entry>>() {
                })
                .value(response -> {
                    assertThat(response.getItems()).hasSize(5);
                    assertThat(response.getCurrentPage()).isEqualTo(0);
                    assertThat(response.getTotalPages()).isEqualTo(3);
                    assertThat(response.getTotalItems()).isEqualTo(15L);
                    assertThat(response.getItems()).extracting(Entry::getPrice)
                            .extracting(Double::intValue)
                            .containsExactlyInAnyOrder(10, 20, 30, 40, 50);
                });
    }

    @DisplayName("Entry get 5 items without sort or filter")
    @Test
    void noSortOrFilterSecondPage() {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/entries")
                        .pathSegment("{journal-id}")
                        .queryParam("page", "1")
                        .queryParam("size", "5")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<Entry>>() {
                })
                .value(response -> {
                    assertThat(response.getItems()).hasSize(5);
                    assertThat(response.getCurrentPage()).isEqualTo(1);
                    assertThat(response.getTotalPages()).isEqualTo(3);
                    assertThat(response.getTotalItems()).isEqualTo(15L);
                    assertThat(response.getItems()).extracting(Entry::getPrice)
                            .extracting(Double::intValue)
                            .containsExactlyInAnyOrder(60, 70, 80, 90, 100);
                });
    }

    @DisplayName("Entry get 0 items without sort or filter on forth page")
    @Test
    void noSortOrFilterEmpty() {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/entries")
                        .pathSegment("{journal-id}")
                        .queryParam("page", "3")
                        .queryParam("size", "5")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<Entry>>() {
                })
                .value(response -> {
                    assertThat(response.getItems()).hasSize(0);
                    assertThat(response.getCurrentPage()).isEqualTo(3);
                    assertThat(response.getTotalPages()).isEqualTo(3);
                    assertThat(response.getTotalItems()).isEqualTo(15L);
                });
    }

    @DisplayName("Entry get 5 items without filter sort by symbol")
    @Test
    void noFilterSortBySymbol() {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/entries")
                        .pathSegment("{journal-id}")
                        .queryParam("page", "0")
                        .queryParam("size", "5")
                        .queryParam("sort", "symbol", "asc")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<Entry>>() {
                })
                .value(response -> {
                    assertThat(response.getItems()).hasSize(5);
                    assertThat(response.getCurrentPage()).isEqualTo(0);
                    assertThat(response.getTotalPages()).isEqualTo(3);
                    assertThat(response.getTotalItems()).isEqualTo(15L);
                    assertThat(response.getItems()).extracting(Entry::getSymbol).containsOnly("AAPL", "AMZN");
                });
    }

    @DisplayName("Entry get 5 items without filter sort by symbol desc")
    @Test
    void noFilterSortBySymbolDesc() {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/entries")
                        .pathSegment("{journal-id}")
                        .queryParam("page", "0")
                        .queryParam("size", "5")
                        .queryParam("sort", "symbol", "desc")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<Entry>>() {
                })
                .value(response -> {
                    assertThat(response.getItems()).hasSize(5);
                    assertThat(response.getCurrentPage()).isEqualTo(0);
                    assertThat(response.getTotalPages()).isEqualTo(3);
                    assertThat(response.getTotalItems()).isEqualTo(15L);
                    assertThat(response.getItems()).extracting(Entry::getSymbol).containsOnly("TSLA", "NVDA");
                });
    }

    @DisplayName("Entry get 5 items without filter sort by date desc")
    @Test
    void noFilterSortByDateDesc() {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/entries")
                        .pathSegment("{journal-id}")
                        .queryParam("page", "0")
                        .queryParam("size", "5")
                        .queryParam("sort", "date", "desc")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<Entry>>() {
                })
                .value(response -> {
                    assertThat(response.getItems()).hasSize(5);
                    assertThat(response.getCurrentPage()).isEqualTo(0);
                    assertThat(response.getTotalPages()).isEqualTo(3);
                    assertThat(response.getTotalItems()).isEqualTo(15L);
                    assertThat(response.getItems()).extracting(Entry::getPrice)
                            .extracting(Double::intValue)
                            .containsExactlyInAnyOrder(110, 120, 130, 140, 150);
                });
    }

    @DisplayName("Entry get 3 items without sort filtering by symbol")
    @Test
    void noSortFilterBySymbol() {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/entries")
                        .pathSegment("{journal-id}")
                        .queryParam("page", "0")
                        .queryParam("size", "5")
                        .queryParam("filter", "symbol.eq", "MSFT")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<Entry>>() {
                })
                .value(response -> {
                    assertThat(response.getItems()).hasSize(3);
                    assertThat(response.getCurrentPage()).isEqualTo(0);
                    assertThat(response.getTotalPages()).isEqualTo(1);
                    assertThat(response.getTotalItems()).isEqualTo(3L);
                    assertThat(response.getItems()).extracting(Entry::getSymbol).containsOnly("MSFT");
                });
    }

    @DisplayName("Entry get 5 items without sort filtering by date with only year month and day")
    @Test
    void noSortFilterByDate() {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/entries")
                        .pathSegment("{journal-id}")
                        .queryParam("page", "0")
                        .queryParam("size", "5")
                        .queryParam("filter", "date.eq", "2022-08-31")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<Entry>>() {
                })
                .value(response -> {
                    assertThat(response.getItems()).hasSize(5);
                    assertThat(response.getCurrentPage()).isEqualTo(0);
                    assertThat(response.getTotalPages()).isEqualTo(1);
                    assertThat(response.getTotalItems()).isEqualTo(5L);
                    assertThat(response.getItems()).extracting(Entry::getDate)
                            .extracting(LocalDateTime::getDayOfMonth)
                            .containsOnly(31);
                    assertThat(response.getItems()).extracting(Entry::getPrice)
                            .extracting(Double::intValue)
                            .containsExactlyInAnyOrder(60, 70, 80, 90, 100);
                });
    }

    @DisplayName("Entry get 1 item without sort filtering by date with only year month and day and symbol")
    @Test
    void noSortFilterByDateAndSymbol() {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/entries")
                        .pathSegment("{journal-id}")
                        .queryParam("page", "0")
                        .queryParam("size", "5")
                        .queryParam("filter", "date.eq", "2022-08-31", "symbol.eq", "MSFT")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<Entry>>() {
                })
                .value(response -> {
                    assertThat(response.getItems()).hasSize(1);
                    assertThat(response.getCurrentPage()).isEqualTo(0);
                    assertThat(response.getTotalPages()).isEqualTo(1);
                    assertThat(response.getTotalItems()).isEqualTo(1L);
                    assertThat(response.getItems()).extracting(Entry::getDate)
                            .extracting(LocalDateTime::getDayOfMonth)
                            .containsOnly(31);
                    assertThat(response.getItems()).extracting(Entry::getPrice)
                            .extracting(Double::intValue)
                            .containsOnly(60);
                });
    }

    @DisplayName("Entry get 3 items without sort filtering by price in range value")
    @Test
    void noSortFilterByPriceRange() {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/entries")
                        .pathSegment("{journal-id}")
                        .queryParam("page", "0")
                        .queryParam("size", "5")
                        .queryParam("filter", "price.gt", "120.30", "price.lt", "140.60")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<Entry>>() {
                })
                .value(response -> {
                    assertThat(response.getItems()).hasSize(3);
                    assertThat(response.getCurrentPage()).isEqualTo(0);
                    assertThat(response.getTotalPages()).isEqualTo(1);
                    assertThat(response.getTotalItems()).isEqualTo(3L);
                    assertThat(response.getItems()).extracting(Entry::getSymbol)
                            .containsExactlyInAnyOrder("AAPL", "NVDA", "TSLA");
                    assertThat(response.getItems()).extracting(Entry::getPrice)
                            .containsOnly(120.31, 130.00, 140.59);
                });
    }

    @DisplayName("Entry get 3 items without sort filtering by price in range value including exact value")
    @Test
    void noSortFilterByPriceRangeWithExactValue() {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/entries")
                        .pathSegment("{journal-id}")
                        .queryParam("page", "0")
                        .queryParam("size", "5")
                        .queryParam("filter", "price.gte", "120.31", "price.lte", "140.59")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<Entry>>() {
                })
                .value(response -> {
                    assertThat(response.getItems()).hasSize(3);
                    assertThat(response.getCurrentPage()).isEqualTo(0);
                    assertThat(response.getTotalPages()).isEqualTo(1);
                    assertThat(response.getTotalItems()).isEqualTo(3L);
                    assertThat(response.getItems()).extracting(Entry::getSymbol)
                            .containsExactlyInAnyOrder("AAPL", "NVDA", "TSLA");
                    assertThat(response.getItems()).extracting(Entry::getPrice)
                            .containsOnly(120.31, 130.00, 140.59);
                });
    }

    @DisplayName("Entry get 1 item without sort filtering by exact price")
    @Test
    void noSortFilterByPrice() {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/entries")
                        .pathSegment("{journal-id}")
                        .queryParam("page", "0")
                        .queryParam("size", "5")
                        .queryParam("filter", "price.eq", "120.31")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<Entry>>() {
                })
                .value(response -> {
                    assertThat(response.getItems()).hasSize(1);
                    assertThat(response.getCurrentPage()).isEqualTo(0);
                    assertThat(response.getTotalPages()).isEqualTo(1);
                    assertThat(response.getTotalItems()).isEqualTo(1L);
                    assertThat(response.getItems()).extracting(Entry::getSymbol)
                            .containsOnly("AAPL");
                    assertThat(response.getItems()).extracting(Entry::getPrice)
                            .containsOnly(120.31);
                });
    }
}