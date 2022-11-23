package com.trading.journal.entry.api;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.allanweber.jwttoken.service.JwtResolveToken;
import com.allanweber.jwttoken.service.JwtTokenReader;
import com.trading.journal.entry.MongoDbContainerInitializer;
import com.trading.journal.entry.WithCustomMockUser;
import com.trading.journal.entry.balance.Balance;
import com.trading.journal.entry.balance.BalanceService;
import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.EntryDirection;
import com.trading.journal.entry.entries.EntryType;
import com.trading.journal.entry.entries.GraphType;
import com.trading.journal.entry.entries.trade.Trade;
import com.trading.journal.entry.journal.Currency;
import com.trading.journal.entry.journal.Journal;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
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
class JournalControllerTest {

    public static final AccessTokenInfo TOKEN_INFO = new AccessTokenInfo("user", 1L, "Paging-Tenancy", singletonList("ROLE_USER"));
    private static String journalCollection;
    private static String entryCollection;

    @MockBean
    JwtTokenReader tokenReader;

    @MockBean
    JwtResolveToken resolveToken;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    BalanceService balanceService;

    private static WebTestClient webTestClient;

    @BeforeAll
    public static void setUp(@Autowired WebApplicationContext applicationContext) {
        webTestClient = MockMvcWebTestClient.bindToApplicationContext(applicationContext).build();
        journalCollection = "PagingTenancy_journals";
        entryCollection = "PagingTenancy_journal-1_entries";
    }

    @AfterEach
    public void afterEach() {
        mongoTemplate.dropCollection(journalCollection);
        mongoTemplate.dropCollection(entryCollection);
    }

    @BeforeEach
    public void mockAccessTokenInfo() {
        when(resolveToken.resolve(any())).thenReturn("token");
        when(tokenReader.getAccessTokenInfo(anyString()))
                .thenReturn(TOKEN_INFO);
    }

    @DisplayName("Get all journals")
    @Test
    void getAll() {
        mongoTemplate.save(buildJournal("journal-1"), journalCollection);
        mongoTemplate.save(buildJournal("journal-2"), journalCollection);
        mongoTemplate.save(buildJournal("journal-3"), journalCollection);

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<Journal>>() {
                })
                .value(response -> assertThat(response).hasSize(3));
    }

    @DisplayName("Get a journal")
    @Test
    void get() {
        Journal journal = mongoTemplate.save(buildJournal("journal-1"), journalCollection);

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals")
                        .pathSegment("{journal-id}")
                        .build(journal.getId()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Journal.class)
                .value(response -> assertThat(response.getName()).isEqualTo("journal-1"));
    }

    @DisplayName("Create journal")
    @Test
    void create() {
        Journal body = Journal.builder()
                .name("journal-1")
                .startBalance(BigDecimal.valueOf(150.32))
                .startJournal(LocalDateTime.of(2022, 11, 9, 19, 49, 0))
                .currency(Currency.DOLLAR)
                .build();

        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectHeader()
                .exists("Location")
                .expectBody(Journal.class)
                .value(journal -> {
                    assertThat(journal.getId()).isNotNull();
                    assertThat(journal.getName()).isEqualTo("journal-1");
                    assertThat(journal.getStartJournal()).isEqualTo(LocalDateTime.of(2022, 11, 9, 19, 49, 0));
                    assertThat(journal.getCurrentBalance().getAccountBalance()).isEqualTo(BigDecimal.valueOf(150.32));
                    assertThat(journal.getCurrentBalance().getTaxes()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(journal.getCurrentBalance().getWithdrawals()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(journal.getCurrentBalance().getDeposits()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(journal.getCurrentBalance().getClosedPositions()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(journal.getLastBalance()).isBetween(LocalDateTime.now().minusSeconds(5), LocalDateTime.now().plusSeconds(5));
                });
    }

    @DisplayName("Create journal with same name return error")
    @Test
    void createSameName() {
        mongoTemplate.save(buildJournal("journal-1"), journalCollection);
        mongoTemplate.save(buildJournal("journal-2"), journalCollection);
        mongoTemplate.save(buildJournal("journal-3"), journalCollection);

        Journal journal = Journal.builder().name("journal-1").startBalance(BigDecimal.valueOf(100.00)).startJournal(LocalDateTime.now()).currency(Currency.DOLLAR).build();
        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(journal)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT)
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response -> assertThat(response.get("error")).isEqualTo("There is already another journal with the same name"));
    }

    @DisplayName("Create journal with similar name is ok")
    @Test
    void createSimilar() {
        mongoTemplate.save(buildJournal("journal-1"), journalCollection);

        Journal journal = Journal.builder().name("journal-11").startBalance(BigDecimal.valueOf(100.00)).startJournal(LocalDateTime.now()).currency(Currency.DOLLAR).build();
        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(journal)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectHeader()
                .exists("Location")
                .expectBody(Journal.class)
                .value(response -> {
                    assertThat(response.getId()).isNotNull();
                    assertThat(response.getName()).isEqualTo("journal-11");
                });
    }

    @DisplayName("Create with invalid fields return error")
    @Test
    void invalidFields() {
        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(Journal.builder().build())
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(new ParameterizedTypeReference<Map<String, List<String>>>() {
                })
                .value(response -> {
                    assertThat(response.get("errors")).contains("Start balance is required");
                    assertThat(response.get("errors")).contains("Journal name is required");
                });
    }

    @DisplayName("Create a journal and delete it, should drop entries collection for this journal")
    @Test
    void delete() {
        AtomicReference<String> journalId = new AtomicReference<>();
        Journal body = Journal.builder().name("journal-1").startBalance(BigDecimal.valueOf(100.00)).startJournal(LocalDateTime.now()).currency(Currency.DOLLAR).build();
        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectHeader()
                .exists("Location")
                .expectBody(Journal.class)
                .value(response -> {
                    assertThat(response.getId()).isNotNull();
                    assertThat(response.getName()).isEqualTo("journal-1");
                    journalId.set(response.getId());
                });

        assertThat(mongoTemplate.findAll(Journal.class, journalCollection)).isNotEmpty();

        Trade trade = Trade.builder()
                .date(LocalDateTime.of(2022, 9, 1, 17, 35, 59))
                .symbol("USD/EUR")
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(1.1234))
                .size(BigDecimal.valueOf(500.00))
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure("1M")
                .build();
        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/trade")
                        .build(journalId.get()))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(trade)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectHeader()
                .exists("Location")
                .expectBody(Entry.class)
                .value(response -> assertThat(response.getId()).isNotNull());


        String entryCollection = "PagingTenancy_journal-1_entries";
        assertThat(mongoTemplate.collectionExists(entryCollection)).isTrue();

        webTestClient
                .delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals")
                        .pathSegment("{journal-id}")
                        .build(journalId.get()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk();

        body = Journal.builder().name("journal-1").startBalance(BigDecimal.valueOf(100.00)).startJournal(LocalDateTime.now()).currency(Currency.DOLLAR).build();
        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectHeader()
                .exists("Location")
                .expectBody(Journal.class)
                .value(response -> {
                    assertThat(response.getId()).isNotNull();
                    assertThat(response.getName()).isEqualTo("journal-1");
                    journalId.set(response.getId());
                });

        assertThat(mongoTemplate.collectionExists(journalCollection)).isTrue();
        assertThat(mongoTemplate.collectionExists(entryCollection)).isFalse();

        webTestClient
                .delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals")
                        .pathSegment("{journal-id}")
                        .build(journalId.get()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk();

        assertThat(mongoTemplate.collectionExists(journalCollection)).isFalse();
    }

    @DisplayName("Get journal balance for a journal not found")
    @Test
    void getBalanceNotFound() {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/balance")
                        .build(1))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response -> assertThat(response.get("error")).isEqualTo("Journal not found"));
    }

    @DisplayName("Get journal balance for a journal with no entries return Zero")
    @Test
    void getBalanceNoEntries() {
        Journal journal = mongoTemplate.save(buildJournal("journal-1"), journalCollection);

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/balance")
                        .build(journal.getId()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Balance.class)
                .value(response -> assertThat(response.getAccountBalance()).isZero());
    }

    @DisplayName("Get positive balance")
    @Test
    void getPositiveBalance() {
        Journal journal = mongoTemplate.save(buildJournal("journal-1"), journalCollection);

        LocalDateTime now = LocalDateTime.now();

        mongoTemplate.save(Entry.builder().type(EntryType.TRADE).date(now.minusDays(10)).netResult(BigDecimal.valueOf(123.45)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().type(EntryType.WITHDRAWAL).price(BigDecimal.valueOf(234.56)).date(now.minusDays(9)).netResult(BigDecimal.valueOf(-234.56)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().type(EntryType.TRADE).date(now.minusDays(8)).netResult(BigDecimal.valueOf(345.67)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().type(EntryType.DEPOSIT).price(BigDecimal.valueOf(456.78)).date(now.minusDays(7)).netResult(BigDecimal.valueOf(456.78)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().type(EntryType.TRADE).date(now.minusDays(6)).netResult(BigDecimal.valueOf(-567.89)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().type(EntryType.TRADE).date(now.minusDays(5)).netResult(BigDecimal.valueOf(678.91)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().type(EntryType.WITHDRAWAL).price(BigDecimal.valueOf(789.12)).date(now.minusDays(4)).netResult(BigDecimal.valueOf(-789.12)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().type(EntryType.TRADE).date(now.minusDays(3)).netResult(BigDecimal.valueOf(891.23)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().type(EntryType.TRADE).date(now.minusSeconds(1)).netResult(BigDecimal.valueOf(912.34)).build(), entryCollection);

        mongoTemplate.save(Entry.builder().type(EntryType.TRADE).date(now.plusDays(4)).price(BigDecimal.valueOf(789.12)).size(BigDecimal.valueOf(1)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().type(EntryType.TRADE).price(BigDecimal.valueOf(891.23)).date(now.plusMinutes(3)).size(BigDecimal.valueOf(1)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().type(EntryType.TRADE).price(BigDecimal.valueOf(912.34)).date(now.plusMinutes(1)).size(BigDecimal.valueOf(1)).build(), entryCollection);

        balanceService.calculateCurrentBalance(TOKEN_INFO, journal.getId());

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/balance")
                        .build(journal.getId()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Balance.class)
                .value(response -> {
                    assertThat(response.getAccountBalance()).isEqualTo(BigDecimal.valueOf(1816.81));
                    assertThat(response.getClosedPositions()).isEqualTo(BigDecimal.valueOf(2383.71));
                    assertThat(response.getDeposits()).isEqualTo(BigDecimal.valueOf(456.78));
                    assertThat(response.getTaxes()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(response.getWithdrawals()).isEqualTo(BigDecimal.valueOf(1023.68));
                    assertThat(response.getOpenedPositions()).isEqualTo(BigDecimal.valueOf(2592.69));
                    assertThat(response.getAvailable()).isEqualTo(BigDecimal.valueOf(-775.88));
                });
    }

    @DisplayName("Get negative balance")
    @Test
    void getNegativeBalance() {
        Journal journal = mongoTemplate.save(buildJournal("journal-1"), journalCollection);

        LocalDateTime now = LocalDateTime.now();

        mongoTemplate.save(Entry.builder().type(EntryType.TRADE).date(now.minusDays(10)).netResult(BigDecimal.valueOf(123.45)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().type(EntryType.TAXES).price(BigDecimal.valueOf(234.56)).date(now.minusDays(9)).netResult(BigDecimal.valueOf(-234.56)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().type(EntryType.DEPOSIT).price(BigDecimal.valueOf(345.67)).date(now.minusDays(8)).netResult(BigDecimal.valueOf(345.67)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().type(EntryType.TRADE).date(now.minusDays(7)).netResult(BigDecimal.valueOf(456.78)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().type(EntryType.TRADE).date(now.minusDays(6)).netResult(BigDecimal.valueOf(-567.89)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().type(EntryType.WITHDRAWAL).price(BigDecimal.valueOf(678.91)).date(now.minusDays(5)).netResult(BigDecimal.valueOf(-678.91)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().type(EntryType.TRADE).date(now.minusDays(4)).netResult(BigDecimal.valueOf(-789.12)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().type(EntryType.WITHDRAWAL).price(BigDecimal.valueOf(891.23)).date(now.minusDays(3)).netResult(BigDecimal.valueOf(-891.23)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().type(EntryType.DEPOSIT).price(BigDecimal.valueOf(912.34)).date(now.minusSeconds(1)).netResult(BigDecimal.valueOf(912.34)).build(), entryCollection);

        mongoTemplate.save(Entry.builder().type(EntryType.TRADE).date(now.plusDays(4)).price(BigDecimal.valueOf(789.12)).size(BigDecimal.valueOf(1)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().type(EntryType.TRADE).price(BigDecimal.valueOf(891.23)).date(now.plusMinutes(3)).size(BigDecimal.valueOf(1)).build(), entryCollection);
        mongoTemplate.save(Entry.builder().type(EntryType.TRADE).price(BigDecimal.valueOf(912.34)).date(now.plusMinutes(1)).size(BigDecimal.valueOf(1)).build(), entryCollection);

        balanceService.calculateCurrentBalance(TOKEN_INFO, journal.getId());

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/balance")
                        .build(journal.getId()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Balance.class)
                .value(response -> {
                            assertThat(response.getAccountBalance()).isEqualTo(BigDecimal.valueOf(-1323.47));
                            assertThat(response.getClosedPositions()).isEqualTo(BigDecimal.valueOf(-776.78));
                            assertThat(response.getDeposits()).isEqualTo(BigDecimal.valueOf(1258.01));
                            assertThat(response.getTaxes()).isEqualTo(BigDecimal.valueOf(234.56));
                            assertThat(response.getWithdrawals()).isEqualTo(BigDecimal.valueOf(1570.14));
                            assertThat(response.getOpenedPositions()).isEqualTo(BigDecimal.valueOf(2592.69));
                            assertThat(response.getAvailable()).isEqualTo(BigDecimal.valueOf(-3916.16));
                        }
                );
    }

    private static Journal buildJournal(String name) {
        return Journal.builder().name(name)
                .currency(Currency.DOLLAR)
                .startJournal(LocalDateTime.now())
                .startBalance(BigDecimal.ZERO)
                .currentBalance(Balance.builder()
                        .accountBalance(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                        .taxes(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                        .withdrawals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                        .deposits(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                        .closedPositions(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                        .openedPositions(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                        .available(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                        .build())
                .build();
    }
}