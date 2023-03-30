package com.trading.journal.entry.api;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.allanweber.jwttoken.service.JwtResolveToken;
import com.allanweber.jwttoken.service.JwtTokenReader;
import tooling.MongoDbContainerInitializer;
import tooling.WithCustomMockUser;
import com.trading.journal.entry.strategy.Strategy;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = MongoDbContainerInitializer.class)
@WithCustomMockUser(tenancyName = "paging-tenancy")
class StrategyControllerTest {
    private static String strategyCollection;

    @MockBean
    JwtTokenReader tokenReader;

    @MockBean
    JwtResolveToken resolveToken;

    @Autowired
    MongoTemplate mongoTemplate;

    private static WebTestClient webTestClient;

    @BeforeAll
    public static void setUp(@Autowired WebApplicationContext applicationContext) {
        webTestClient = MockMvcWebTestClient.bindToApplicationContext(applicationContext).build();
        strategyCollection = "PagingTenancy_strategies";
    }

    @AfterEach
    public void afterEach() {
        mongoTemplate.dropCollection(strategyCollection);
    }

    @BeforeEach
    public void mockAccessTokenInfo() {
        when(resolveToken.resolve(any())).thenReturn("token");
        when(tokenReader.getAccessTokenInfo(anyString()))
                .thenReturn(new AccessTokenInfo("user", 1L, "Paging-Tenancy", singletonList("ROLE_USER")));
    }

    @DisplayName("Get all strategies")
    @Test
    void getAll() {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/strategies")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageWrapper<Strategy>>() {
                })
                .value(response -> assertThat(response.getContent()).isEmpty());

        mongoTemplate.save(Strategy.builder().name("strategy-1").build(), strategyCollection);
        mongoTemplate.save(Strategy.builder().name("strategy-2").build(), strategyCollection);
        mongoTemplate.save(Strategy.builder().name("strategy-3").build(), strategyCollection);

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/strategies")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageWrapper<Strategy>>() {
                })
                .value(response -> assertThat(response.getContent()).hasSize(3));
    }

    @DisplayName("Get all strategies paginated")
    @Test
    void getAllPaging() {
        for (int i = 1; i <= 25; i++) {
            mongoTemplate.save(Strategy.builder().name("strategy-" + i).build(), strategyCollection);
        }

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/strategies")
                        .queryParam("page", "0")
                        .queryParam("size", "10")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageWrapper<Strategy>>() {
                })
                .value(response -> {
                    assertThat(response.getContent()).hasSize(10);
                    assertThat(response.getTotalPages()).isEqualTo(3);
                    assertThat(response.getTotal()).isEqualTo(25);
                });

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/strategies")
                        .queryParam("page", "1")
                        .queryParam("size", "10")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageWrapper<Strategy>>() {
                })
                .value(response -> {
                    assertThat(response.getContent()).hasSize(10);
                    assertThat(response.getTotalPages()).isEqualTo(3);
                    assertThat(response.getTotal()).isEqualTo(25);
                });

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/strategies")
                        .queryParam("page", "2")
                        .queryParam("size", "10")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageWrapper<Strategy>>() {
                })
                .value(response -> {
                    assertThat(response.getContent()).hasSize(5);
                    assertThat(response.getTotalPages()).isEqualTo(3);
                    assertThat(response.getTotal()).isEqualTo(25);
                });

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/strategies")
                        .queryParam("page", "0")
                        .queryParam("size", "17")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageWrapper<Strategy>>() {
                })
                .value(response -> {
                    assertThat(response.getContent()).hasSize(17);
                    assertThat(response.getTotalPages()).isEqualTo(2);
                    assertThat(response.getTotal()).isEqualTo(25);
                });

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/strategies")
                        .queryParam("page", "4")
                        .queryParam("size", "10")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageWrapper<Strategy>>() {
                })
                .value(response -> {
                    assertThat(response.getContent()).isEmpty();
                    assertThat(response.getTotalPages()).isEqualTo(3);
                    assertThat(response.getTotal()).isEqualTo(25);
                });
    }

    @DisplayName("Create a new strategy")
    @Test
    void create() {
        Strategy strategy = Strategy.builder().name("strategy-1").build();

        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/strategies")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(strategy)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectHeader()
                .exists("Location")
                .expectBody(Strategy.class)
                .value(strategyResponse -> {
                    assertThat(strategyResponse.getId()).isNotNull();
                    assertThat(strategyResponse.getName()).isEqualTo("strategy-1");
                    assertThat(strategyResponse.getColor()).isNull();
                });

        List<Strategy> all = mongoTemplate.findAll(Strategy.class, strategyCollection);
        assertThat(all).hasSize(1);

        Strategy strategy2 = Strategy.builder().name("strategy-2").color("red").build();

        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/strategies")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(strategy2)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectHeader()
                .exists("Location")
                .expectBody(Strategy.class)
                .value(strategyResponse -> {
                    assertThat(strategyResponse.getId()).isNotNull();
                    assertThat(strategyResponse.getName()).isEqualTo("strategy-2");
                    assertThat(strategyResponse.getColor()).isEqualTo("red");
                });

        all = mongoTemplate.findAll(Strategy.class, strategyCollection);
        assertThat(all).hasSize(2);
    }

    @DisplayName("Update strategy")
    @Test
    void update() {
        Strategy saved = mongoTemplate.save(Strategy.builder().name("strategy-1").build(), strategyCollection);
        Strategy strategy = Strategy.builder().id(saved.getId()).name("strategy-updated").color("red").build();

        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/strategies")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(strategy)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Strategy.class)
                .value(strategyResponse -> {
                    assertThat(strategyResponse.getId()).isNotNull();
                    assertThat(strategyResponse.getName()).isEqualTo("strategy-updated");
                });

        List<Strategy> all = mongoTemplate.findAll(Strategy.class, strategyCollection);
        assertThat(all).hasSize(1);
    }

    @DisplayName("Create a invalid strategy return exception")
    @Test
    void createInvalid() {
        Strategy strategy = Strategy.builder().build();

        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/strategies")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(strategy)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(new ParameterizedTypeReference<Map<String, List<String>>>() {
                })
                .value(response -> {
                    assertThat(response.get("errors")).hasSize(1);
                    assertThat(response.get("errors")).contains("Name is required");
                });

        List<Strategy> all = mongoTemplate.findAll(Strategy.class, strategyCollection);
        assertThat(all).hasSize(0);
    }

    @DisplayName("Get strategy by Id")
    @Test
    void get() {
        Strategy strategy1 = mongoTemplate.save(Strategy.builder().name("strategy-1").build(), strategyCollection);
        Strategy strategy2 = mongoTemplate.save(Strategy.builder().name("strategy-2").build(), strategyCollection);

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/strategies")
                        .pathSegment("{strategy-id}")
                        .build(strategy1.getId()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Strategy.class)
                .value(strategyResponse -> {
                    assertThat(strategyResponse.getId()).isNotNull();
                    assertThat(strategyResponse.getName()).isEqualTo("strategy-1");
                });

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/strategies")
                        .pathSegment("{strategy-id}")
                        .build(strategy2.getId()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Strategy.class)
                .value(strategyResponse -> {
                    assertThat(strategyResponse.getId()).isNotNull();
                    assertThat(strategyResponse.getName()).isEqualTo("strategy-2");
                });
    }

    @DisplayName("Get strategy by Id not found")
    @Test
    void getNotFound() {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/strategies")
                        .pathSegment("{strategy-id}")
                        .build(UUID.randomUUID().toString()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response -> assertThat(response.get("error")).isEqualTo("Strategy not found"));
    }

    @DisplayName("Delete strategy by Id, last strategy drop the collection")
    @Test
    void delete() {
        Strategy strategy = mongoTemplate.save(Strategy.builder().name("strategy-1").build(), strategyCollection);

        webTestClient
                .delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/strategies")
                        .pathSegment("{strategy-id}")
                        .build(strategy.getId()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk();

        boolean collectionExists = mongoTemplate.collectionExists(strategyCollection);
        assertThat(collectionExists).isFalse();
    }

    @DisplayName("Delete strategy by Id, but it is not last strategy do not drop the collection")
    @Test
    void deleteNoDrop() {
        Strategy strategy = mongoTemplate.save(Strategy.builder().name("strategy-1").build(), strategyCollection);
        mongoTemplate.save(Strategy.builder().name("strategy-2").build(), strategyCollection);

        webTestClient
                .delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/strategies")
                        .pathSegment("{strategy-id}")
                        .build(strategy.getId()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk();

        boolean collectionExists = mongoTemplate.collectionExists(strategyCollection);
        assertThat(collectionExists).isTrue();
        List<Strategy> all = mongoTemplate.findAll(Strategy.class, strategyCollection);
        assertThat(all).hasSize(1);
    }

    @DisplayName("Delete strategy by Id not found")
    @Test
    void deleteNotFound() {
        mongoTemplate.save(Strategy.builder().name("strategy-1").build(), strategyCollection);
        webTestClient
                .delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/strategies")
                        .pathSegment("{strategy-id}")
                        .build(UUID.randomUUID().toString()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response -> assertThat(response.get("error")).isEqualTo("Strategy not found"));

        List<Strategy> all = mongoTemplate.findAll(Strategy.class, strategyCollection);
        assertThat(all).hasSize(1);
    }
}