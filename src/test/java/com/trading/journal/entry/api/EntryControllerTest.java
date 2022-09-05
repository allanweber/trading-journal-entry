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
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
class EntryControllerTest {

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
                        .path("/entry")
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