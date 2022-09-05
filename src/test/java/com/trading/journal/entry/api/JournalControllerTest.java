package com.trading.journal.entry.api;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.allanweber.jwttoken.service.JwtResolveToken;
import com.allanweber.jwttoken.service.JwtTokenReader;
import com.trading.journal.entry.MongoDbContainerInitializer;
import com.trading.journal.entry.WithCustomMockUser;
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

import java.util.List;
import java.util.Map;

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

    private static final String TENANCY = "paging-tenancy";

    private static String journalCollection;

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
    }

    @AfterEach
    public void shutDown() {
        mongoTemplate.dropCollection(journalCollection);
    }

    @BeforeEach
    public void mockAccessTokenInfo() {
        when(resolveToken.resolve(any())).thenReturn("token");
        when(tokenReader.getAccessTokenInfo(anyString()))
                .thenReturn(new AccessTokenInfo("user", 1L, TENANCY, singletonList("ROLE_USER")));
    }

    @DisplayName("Get all journals")
    @Test
    void getAll() {
        mongoTemplate.save(Journal.builder().name("journal-1").build(), journalCollection);
        mongoTemplate.save(Journal.builder().name("journal-2").build(), journalCollection);
        mongoTemplate.save(Journal.builder().name("journal-3").build(), journalCollection);

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
        Journal journal = mongoTemplate.save(Journal.builder().name("journal-1").build(), journalCollection);

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
        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(Journal.builder().name("journal-1").build())
                .exchange()
                .expectStatus()
                .isCreated()
                .expectHeader()
                .exists("Location")
                .expectBody(Journal.class)
                .value(response -> {
                    assertThat(response.getId()).isNotNull();
                    assertThat(response.getName()).isEqualTo("journal-1");
                });
    }

    @DisplayName("Create journal with same name return error")
    @Test
    void createSameName() {
        mongoTemplate.save(Journal.builder().name("journal-1").build(), journalCollection);
        mongoTemplate.save(Journal.builder().name("journal-2").build(), journalCollection);
        mongoTemplate.save(Journal.builder().name("journal-3").build(), journalCollection);

        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(Journal.builder().name("journal-1").build())
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT)
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response -> assertThat(response.get("error")).isEqualTo("There is already another journal with the same name"));
    }

    @DisplayName("Create journal with similar name id ok")
    @Test
    void createSimilar() {
        mongoTemplate.save(Journal.builder().name("journal-1").build(), journalCollection);

        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(Journal.builder().name("journal-11").build())
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
}