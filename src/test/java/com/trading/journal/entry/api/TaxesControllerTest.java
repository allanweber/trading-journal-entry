package com.trading.journal.entry.api;

import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.EntryType;
import com.trading.journal.entry.entries.taxes.Taxes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import tooling.IntegratedTestWithJournal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaxesControllerTest extends IntegratedTestWithJournal {

    @BeforeEach
    public void beforeEach() {
        mongoTemplate.dropCollection("TestTenancy_entries");
    }

    @DisplayName("Create a new Taxes entry")
    @Test
    void createTaxes() {
        Taxes taxes = Taxes.builder()
                .date(LocalDateTime.of(2022, 9, 1, 17, 35, 59))
                .price(BigDecimal.valueOf(50))
                .build();

        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/taxes")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(taxes)
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
                    assertThat(response.getAccountChange()).isEqualTo(BigDecimal.valueOf(-0.0050).setScale(4, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountBalance()).isEqualTo(BigDecimal.valueOf(9950.00).setScale(2, RoundingMode.HALF_EVEN));
                });
    }

    @DisplayName("Try to create an invalid Taxes entry")
    @Test
    void invalidTaxes() {
        Taxes taxes = Taxes.builder()
                .build();

        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/taxes")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(taxes)
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
}