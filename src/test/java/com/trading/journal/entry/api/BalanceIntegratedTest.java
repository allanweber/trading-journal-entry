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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
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
class BalanceIntegratedTest {

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
        Journal journal = mongoTemplate.save(Journal.builder().name("JOURNAL-1").startBalance(BigDecimal.valueOf(10000)).build(), journalCollection);
        journalId = journal.getId();
        entryCollection = "PagingTenancy_JOURNAL-1_entries";
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
        //Create an entry
        AtomicReference<String> entryMSFTId = new AtomicReference<>();
        Entry entryMSFT = Entry.builder()
                .date(LocalDateTime.of(2022, 1, 1, 12, 0, 0))
                .type(EntryType.TRADE)
                .symbol("MSFT")
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(100.00))
                .size(BigDecimal.valueOf(15))
                .profitPrice(BigDecimal.valueOf(200.00))
                .lossPrice(BigDecimal.valueOf(80.00))
                .build();
        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/entries")
                        .pathSegment("{journal-id}")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(entryMSFT)
                .exchange()
                .expectBody(Entry.class)
                .value(response -> {
                    assertThat(response.getId()).isNotNull();
                    entryMSFTId.set(response.getId());
                    assertThat(response.getPlannedRR()).isEqualTo(BigDecimal.valueOf(5.00).setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountRisked()).isEqualTo(BigDecimal.valueOf(0.0300).setScale(4, RoundingMode.HALF_EVEN));
                    assertThat(response.getProfitPrice()).isEqualTo(BigDecimal.valueOf(200.00));
                    assertThat(response.getLossPrice()).isEqualTo(BigDecimal.valueOf(80.00));

                    assertThat(response.getGrossResult()).isNull();
                    assertThat(response.getNetResult()).isNull();
                    assertThat(response.getAccountChange()).isNull();
                    assertThat(response.getAccountBalance()).isNull();
                });

        //Create an entry
        AtomicReference<String> entryAPPLId = new AtomicReference<>();
        Entry entryAPPL = Entry.builder()
                .date(LocalDateTime.of(2022, 1, 2, 12, 0, 0))
                .type(EntryType.TRADE)
                .symbol("APPL")
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(200.00))
                .size(BigDecimal.valueOf(20))
                .profitPrice(BigDecimal.valueOf(400.00))
                .lossPrice(BigDecimal.valueOf(150.00))
                .build();
        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/entries")
                        .pathSegment("{journal-id}")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(entryAPPL)
                .exchange()
                .expectBody(Entry.class)
                .value(response -> {
                    assertThat(response.getId()).isNotNull();
                    entryAPPLId.set(response.getId());
                    assertThat(response.getPlannedRR()).isEqualTo(BigDecimal.valueOf(4.00).setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountRisked()).isEqualTo(BigDecimal.valueOf(0.1000).setScale(4, RoundingMode.HALF_EVEN));
                    assertThat(response.getProfitPrice()).isEqualTo(BigDecimal.valueOf(400.00));
                    assertThat(response.getLossPrice()).isEqualTo(BigDecimal.valueOf(150.00));

                    assertThat(response.getGrossResult()).isNull();
                    assertThat(response.getNetResult()).isNull();
                    assertThat(response.getAccountChange()).isNull();
                    assertThat(response.getAccountBalance()).isNull();
                });

        //Create an entry
        AtomicReference<String> entryNVDAId = new AtomicReference<>();
        Entry entryNVDA = Entry.builder()
                .date(LocalDateTime.of(2022, 1, 3, 12, 0, 0))
                .type(EntryType.TRADE)
                .symbol("NVDA")
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(375.00))
                .size(BigDecimal.valueOf(20))
                .profitPrice(BigDecimal.valueOf(150.00))
                .lossPrice(BigDecimal.valueOf(400.00))
                .build();
        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/entries")
                        .pathSegment("{journal-id}")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(entryNVDA)
                .exchange()
                .expectBody(Entry.class)
                .value(response -> {
                    assertThat(response.getId()).isNotNull();
                    entryNVDAId.set(response.getId());
                    assertThat(response.getPlannedRR()).isEqualTo(BigDecimal.valueOf(9.00).setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountRisked()).isEqualTo(BigDecimal.valueOf(0.0500).setScale(4, RoundingMode.HALF_EVEN));
                    assertThat(response.getProfitPrice()).isEqualTo(BigDecimal.valueOf(150.00));
                    assertThat(response.getLossPrice()).isEqualTo(BigDecimal.valueOf(400.00));

                    assertThat(response.getGrossResult()).isNull();
                    assertThat(response.getNetResult()).isNull();
                    assertThat(response.getAccountChange()).isNull();
                    assertThat(response.getAccountBalance()).isNull();
                });

        //Create an entry
        AtomicReference<String> entryTSLAId = new AtomicReference<>();
        Entry entryTSLA = Entry.builder()
                .date(LocalDateTime.of(2022, 1, 4, 12, 0, 0))
                .type(EntryType.TRADE)
                .symbol("TSLA")
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(700))
                .size(BigDecimal.valueOf(30))
                .profitPrice(BigDecimal.valueOf(500.00))
                .lossPrice(BigDecimal.valueOf(745.00))
                .build();
        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/entries")
                        .pathSegment("{journal-id}")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(entryTSLA)
                .exchange()
                .expectBody(Entry.class)
                .value(response -> {
                    assertThat(response.getId()).isNotNull();
                    entryTSLAId.set(response.getId());
                    assertThat(response.getPlannedRR()).isEqualTo(BigDecimal.valueOf(4.44).setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountRisked()).isEqualTo(BigDecimal.valueOf(0.1350).setScale(4, RoundingMode.HALF_EVEN));
                    assertThat(response.getProfitPrice()).isEqualTo(BigDecimal.valueOf(500.00));
                    assertThat(response.getLossPrice()).isEqualTo(BigDecimal.valueOf(745.00));

                    assertThat(response.getGrossResult()).isNull();
                    assertThat(response.getNetResult()).isNull();
                    assertThat(response.getAccountChange()).isNull();
                    assertThat(response.getAccountBalance()).isNull();
                });

        //Exit a trade in the middle
        entryAPPL = rebuildEntryWithId(entryAPPL, entryAPPLId.get())
                .exitPrice(BigDecimal.valueOf(400.00))
                .exitDate(LocalDateTime.of(2022, 1, 2, 12, 0, 0))
                .costs(BigDecimal.ZERO)
                .build();
        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/entries")
                        .pathSegment("{journal-id}")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(entryAPPL)
                .exchange()
                .expectBody(Entry.class)
                .value(response -> {
                    assertThat(response.getPlannedRR()).isEqualTo(BigDecimal.valueOf(4.00).setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountRisked()).isEqualTo(BigDecimal.valueOf(0.1000).setScale(4, RoundingMode.HALF_EVEN));
                    assertThat(response.getProfitPrice()).isEqualTo(BigDecimal.valueOf(400.00));
                    assertThat(response.getLossPrice()).isEqualTo(BigDecimal.valueOf(150.00));

                    assertThat(response.getGrossResult()).isEqualTo(BigDecimal.valueOf(4000.00).setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(response.getNetResult()).isEqualTo(BigDecimal.valueOf(4000.00).setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountChange()).isEqualTo(BigDecimal.valueOf(0.4000).setScale(4, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountBalance()).isEqualTo(BigDecimal.valueOf(14000.00).setScale(2, RoundingMode.HALF_EVEN));
                });

        //Check the Balance
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals")
                        .pathSegment("{journal-id}/balance")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(Balance.class)
                .value(response -> {
                            assertThat(response.getAccountBalance()).isEqualTo(BigDecimal.valueOf(14000.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getClosedPositions()).isEqualTo(BigDecimal.valueOf(4000.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getDeposits()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getTaxes()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getWithdrawals()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
                        }
                );
    }

    private static Entry.EntryBuilder rebuildEntryWithId(Entry originalEntry, String id) {
        return Entry.builder()
                .id(id)
                .date(originalEntry.getDate())
                .type(originalEntry.getType())
                .symbol(originalEntry.getSymbol())
                .direction(originalEntry.getDirection())
                .price(originalEntry.getPrice())
                .size(originalEntry.getSize())
                .profitPrice(originalEntry.getProfitPrice())
                .lossPrice(originalEntry.getLossPrice());
    }
}