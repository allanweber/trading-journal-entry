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
import java.util.List;

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

    private static final String TENANCY = "paging-tenancy";
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

        journalCollection = TENANCY.concat("_").concat("journals");
        Journal journal = mongoTemplate.save(Journal.builder().name("JOURNAL-1").build(), journalCollection);
        journalId = journal.getId();
        entryCollection = TENANCY.concat("_").concat(journal.getName()).concat("_").concat("entries");
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
                .thenReturn(new AccessTokenInfo("user", 1L, TENANCY, singletonList("ROLE_USER")));
    }

    @DisplayName("Get all entries from a journal must be ordered by date")
    @Test
    void all() {
        mongoTemplate.save(Entry.builder().price(10.00).symbol("MSFT").direction(EntryDirection.LONG).date(LocalDateTime.of(2022, 8, 1, 18, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(20.00).symbol("AAPL").direction(EntryDirection.SHORT).date(LocalDateTime.of(2022, 8, 5, 18, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(30.00).symbol("NVDA").direction(EntryDirection.LONG).date(LocalDateTime.of(2022, 8, 10, 18, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(40.00).symbol("TSLA").direction(EntryDirection.SHORT).date(LocalDateTime.of(2022, 8, 3, 18, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(50.00).symbol("AMZN").direction(EntryDirection.LONG).date(LocalDateTime.of(2022, 8, 2, 18, 23, 30)).build(), entryCollection);

        mongoTemplate.save(Entry.builder().price(60.00).symbol("MSFT").direction(EntryDirection.LONG).date(LocalDateTime.of(2022, 8, 6, 0, 21, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(70.00).symbol("AAPL").direction(EntryDirection.SHORT).date(LocalDateTime.of(2022, 8, 4, 17, 22, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(80.00).symbol("NVDA").direction(EntryDirection.LONG).date(LocalDateTime.of(2022, 8, 8, 16, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(90.00).symbol("TSLA").direction(EntryDirection.SHORT).date(LocalDateTime.of(2022, 8, 9, 15, 24, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(100.00).symbol("AMZN").direction(EntryDirection.LONG).date(LocalDateTime.of(2022, 8, 7, 23, 25, 30)).build(), entryCollection);

        mongoTemplate.save(Entry.builder().price(110.00).symbol("MSFT").direction(EntryDirection.LONG).date(LocalDateTime.of(2022, 9, 15, 18, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(120.31).symbol("AAPL").direction(EntryDirection.SHORT).date(LocalDateTime.of(2022, 9, 14, 18, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(130.00).symbol("NVDA").direction(EntryDirection.LONG).date(LocalDateTime.of(2022, 9, 13, 18, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(140.59).symbol("TSLA").direction(EntryDirection.SHORT).date(LocalDateTime.of(2022, 9, 12, 18, 23, 30)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().price(150.00).symbol("AMZN").direction(EntryDirection.LONG).date(LocalDateTime.of(2022, 9, 11, 18, 23, 30)).build(), entryCollection);

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

    @DisplayName("Create a new entry")
    @Test
    void create() {
        Entry entry = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 1, 17, 35, 59))
                .type(EntryType.TRADE)
                .symbol("USD/EUR")
                .direction(EntryDirection.LONG)
                .price(1.1234)
                .size(500.00)
                .profitPrice(1.2345)
                .lossPrice(1.009)
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
                    assertThat(response.getType()).isEqualTo(EntryType.TRADE);
                    assertThat(response.getSymbol()).isEqualTo("USD/EUR");
                    assertThat(response.getDirection()).isEqualTo(EntryDirection.LONG);
                    assertThat(response.getPrice()).isEqualTo(1.1234);
                    assertThat(response.getSize()).isEqualTo(500.00);
                    assertThat(response.getProfitPrice()).isEqualTo(1.2345);
                    assertThat(response.getLossPrice()).isEqualTo(1.009);

                    assertThat(response.getPlannedROR()).isNull();
                    assertThat(response.getAccountRisked()).isNull();
                });
    }
}