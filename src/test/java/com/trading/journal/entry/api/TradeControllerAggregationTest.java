package com.trading.journal.entry.api;

import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.EntryDirection;
import com.trading.journal.entry.entries.EntryType;
import com.trading.journal.entry.entries.trade.aggregate.AggregateType;
import com.trading.journal.entry.entries.trade.aggregate.PeriodAggregatedResult;
import com.trading.journal.entry.entries.trade.aggregate.PeriodItem;
import com.trading.journal.entry.entries.trade.aggregate.TradesAggregated;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import tooling.IntegratedTestWithJournal;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TradeControllerAggregationTest extends IntegratedTestWithJournal {

    private static final String entryCollection = "TestTenancy_JOURNAL-1_entries";

    @BeforeEach
    public void beforeEach() {
        mongoTemplate.dropCollection(entryCollection);
    }

    @DisplayName("Aggregate time period day")
    @Test
    void aggregateDay() {
        //Save deposit, withdrawal and taxes, but they are not considered while aggregating
        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .type(EntryType.DEPOSIT)
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);
        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .type(EntryType.TAXES)
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);
        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .type(EntryType.WITHDRAWAL)
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("MSFT")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .exitDate(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("MSFT")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2022, 1, 2, 1, 1, 0))
                        .exitDate(LocalDateTime.of(2022, 1, 2, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("PPE")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2022, 2, 2, 1, 1, 0))
                        .exitDate(LocalDateTime.of(2022, 2, 2, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("BMY")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2022, 3, 2, 1, 1, 0))
                        .exitDate(LocalDateTime.of(2022, 3, 2, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("ABBV")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2022, 3, 2, 2, 1, 0))
                        .exitDate(LocalDateTime.of(2022, 3, 2, 2, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("HD")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2022, 3, 3, 3, 1, 0))
                        .exitDate(LocalDateTime.of(2022, 3, 3, 3, 1, 0))
                        .build(),
                entryCollection);

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/trade/aggregate/time")
                        .queryParam("aggregation", AggregateType.DAY.name())
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(PeriodAggregatedResult.class)
                .value(response -> {
                    assertThat(response.getTotal()).isEqualTo(5);
                    assertThat(response.getItems()).hasSize(3);

                    assertThat(response.getItems().get(0).getGroup()).isEqualTo("2022-03");
                    assertThat(response.getItems().get(0).getItems()).hasSize(2);
                    assertThat(response.getItems().get(0).getItems()).extracting(PeriodItem::getGroup).containsExactly("2022-03-03", "2022-03-02");
                    assertThat(response.getItems().get(0).getItems()).extracting(PeriodItem::getCount).containsExactly(1L, 2L);
                    assertThat(response.getItems().get(0).getItems()).extracting(PeriodItem::getResult).containsExactly(BigDecimal.valueOf(100.00), BigDecimal.valueOf(200.00));

                    assertThat(response.getItems().get(1).getGroup()).isEqualTo("2022-02");
                    assertThat(response.getItems().get(1).getItems()).hasSize(1);
                    assertThat(response.getItems().get(1).getItems()).extracting(PeriodItem::getGroup).containsExactly("2022-02-02");
                    assertThat(response.getItems().get(1).getItems()).extracting(PeriodItem::getCount).containsExactly(1L);
                    assertThat(response.getItems().get(1).getItems()).extracting(PeriodItem::getResult).containsExactly(BigDecimal.valueOf(100.00));

                    assertThat(response.getItems().get(2).getGroup()).isEqualTo("2022-01");
                    assertThat(response.getItems().get(2).getItems()).hasSize(2);
                    assertThat(response.getItems().get(2).getItems()).extracting(PeriodItem::getGroup).containsExactly("2022-01-02", "2022-01-01");
                    assertThat(response.getItems().get(2).getItems()).extracting(PeriodItem::getCount).containsExactly(1L, 1L);
                    assertThat(response.getItems().get(2).getItems()).extracting(PeriodItem::getResult).containsExactly(BigDecimal.valueOf(100.00), BigDecimal.valueOf(100.00));
                });

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/trade/aggregate/trade")
                        .queryParam("from", "2022-03-03 00:00:00")
                        .queryParam("until", "2022-03-03 23:59:59")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<TradesAggregated>>() {
                })
                .value(response -> {
                    assertThat(response).hasSize(1);
                    assertThat(response.get(0).getCount()).isEqualTo(1);
                    assertThat(response.get(0).getGroup()).isEqualTo("2022-03-03");
                    assertThat(response.get(0).getItems()).hasSize(1);

                    assertThat(response.get(0).getItems().get(0).getSymbol()).isEqualTo("HD");
                    assertThat(response.get(0).getItems().get(0).getNetResult()).isEqualTo(BigDecimal.valueOf(100));
                });
    }

    @DisplayName("Aggregate time period by WEEK")
    @Test
    void aggregateWeek() {
        //Save deposit, withdrawal and taxes, but they are not considered while aggregating
        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .type(EntryType.DEPOSIT)
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);
        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .type(EntryType.TAXES)
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);
        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .type(EntryType.WITHDRAWAL)
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("MSFT")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2022, 5, 2, 1, 1, 0))
                        .exitDate(LocalDateTime.of(2022, 5, 2, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("MSFT")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2022, 5, 3, 1, 1, 0))
                        .exitDate(LocalDateTime.of(2022, 5, 3, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("PPE")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2022, 8, 2, 1, 1, 0))
                        .exitDate(LocalDateTime.of(2022, 8, 2, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("BMY")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2022, 10, 4, 1, 1, 0))
                        .exitDate(LocalDateTime.of(2022, 10, 4, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("ABBV")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2022, 10, 4, 2, 1, 0))
                        .exitDate(LocalDateTime.of(2022, 10, 4, 2, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("HD")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2022, 10, 5, 3, 1, 0))
                        .exitDate(LocalDateTime.of(2022, 10, 5, 3, 1, 0))
                        .build(),
                entryCollection);

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/trade/aggregate/time")
                        .queryParam("aggregation", AggregateType.WEEK.name())
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(PeriodAggregatedResult.class)
                .value(response -> {
                    assertThat(response.getTotal()).isEqualTo(3);
                    assertThat(response.getItems()).hasSize(3);

                    assertThat(response.getItems().get(0).getGroup()).isEqualTo("2022-10");
                    assertThat(response.getItems().get(0).getItems()).hasSize(1);
                    assertThat(response.getItems().get(0).getItems()).extracting(PeriodItem::getGroup).containsExactly("2022-40");
                    assertThat(response.getItems().get(0).getItems()).extracting(PeriodItem::getCount).containsExactly(3L);
                    assertThat(response.getItems().get(0).getItems()).extracting(PeriodItem::getResult).containsExactly(BigDecimal.valueOf(300.00));

                    assertThat(response.getItems().get(1).getGroup()).isEqualTo("2022-08");
                    assertThat(response.getItems().get(1).getItems()).hasSize(1);
                    assertThat(response.getItems().get(1).getItems()).extracting(PeriodItem::getGroup).containsExactly("2022-31");
                    assertThat(response.getItems().get(1).getItems()).extracting(PeriodItem::getCount).containsExactly(1L);
                    assertThat(response.getItems().get(1).getItems()).extracting(PeriodItem::getResult).containsExactly(BigDecimal.valueOf(100.00));

                    assertThat(response.getItems().get(2).getGroup()).isEqualTo("2022-05");
                    assertThat(response.getItems().get(2).getItems()).hasSize(1);
                    assertThat(response.getItems().get(2).getItems()).extracting(PeriodItem::getGroup).containsExactly("2022-18");
                    assertThat(response.getItems().get(2).getItems()).extracting(PeriodItem::getCount).containsExactly(2L);
                    assertThat(response.getItems().get(2).getItems()).extracting(PeriodItem::getResult).containsExactly(BigDecimal.valueOf(200.00));
                });

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/trade/aggregate/trade")
                        .queryParam("from", "2022-10-03 00:00:00")
                        .queryParam("until", "2022-10-09 23:59:59")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<TradesAggregated>>() {
                })
                .value(response -> {
                    assertThat(response).hasSize(2);
                    assertThat(response.get(0).getCount()).isEqualTo(1);
                    assertThat(response.get(0).getGroup()).isEqualTo("2022-10-05");
                    assertThat(response.get(0).getItems()).hasSize(1);
                    assertThat(response.get(0).getItems().get(0).getSymbol()).isEqualTo("HD");
                    assertThat(response.get(0).getItems().get(0).getNetResult()).isEqualTo(BigDecimal.valueOf(100));

                    assertThat(response.get(1).getCount()).isEqualTo(2);
                    assertThat(response.get(1).getGroup()).isEqualTo("2022-10-04");
                    assertThat(response.get(1).getItems()).hasSize(2);
                    assertThat(response.get(1).getItems().get(0).getSymbol()).isEqualTo("ABBV");
                    assertThat(response.get(1).getItems().get(0).getNetResult()).isEqualTo(BigDecimal.valueOf(100));
                    assertThat(response.get(1).getItems().get(1).getSymbol()).isEqualTo("BMY");
                    assertThat(response.get(1).getItems().get(1).getNetResult()).isEqualTo(BigDecimal.valueOf(100));
                });
    }

    @DisplayName("Aggregate time period by MONTH")
    @Test
    void aggregateMonth() {

        //Save deposit, withdrawal and taxes, but they are not considered while aggregating
        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .type(EntryType.DEPOSIT)
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);
        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .type(EntryType.TAXES)
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);
        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .type(EntryType.WITHDRAWAL)
                        .date(LocalDateTime.of(2022, 1, 1, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("MSFT")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2022, 5, 2, 1, 1, 0))
                        .exitDate(LocalDateTime.of(2022, 5, 2, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("MSFT")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2022, 5, 3, 1, 1, 0))
                        .exitDate(LocalDateTime.of(2022, 5, 3, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("PPE")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2022, 8, 2, 1, 1, 0))
                        .exitDate(LocalDateTime.of(2022, 8, 2, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("BMY")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2023, 1, 4, 1, 1, 0))
                        .exitDate(LocalDateTime.of(2023, 1, 4, 1, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("ABBV")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2023, 2, 4, 2, 1, 0))
                        .exitDate(LocalDateTime.of(2023, 2, 4, 2, 1, 0))
                        .build(),
                entryCollection);

        mongoTemplate.save(
                Entry.builder()
                        .price(BigDecimal.valueOf(10.00))
                        .symbol("HD")
                        .direction(EntryDirection.LONG)
                        .type(EntryType.TRADE)
                        .netResult(BigDecimal.valueOf(100))
                        .date(LocalDateTime.of(2023, 2, 5, 3, 1, 0))
                        .exitDate(LocalDateTime.of(2023, 2, 5, 3, 1, 0))
                        .build(),
                entryCollection);

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/trade/aggregate/time")
                        .queryParam("aggregation", AggregateType.MONTH.name())
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(PeriodAggregatedResult.class)
                .value(response -> {
                    assertThat(response.getTotal()).isEqualTo(4);
                    assertThat(response.getItems()).hasSize(2);

                    assertThat(response.getItems().get(0).getGroup()).isEqualTo("2023");
                    assertThat(response.getItems().get(0).getItems()).hasSize(2);
                    assertThat(response.getItems().get(0).getItems()).extracting(PeriodItem::getGroup).containsExactly("2023-02", "2023-01");
                    assertThat(response.getItems().get(0).getItems()).extracting(PeriodItem::getCount).containsExactly(2L, 1L);
                    assertThat(response.getItems().get(0).getItems()).extracting(PeriodItem::getResult).containsExactly(BigDecimal.valueOf(200.00), BigDecimal.valueOf(100.00));

                    assertThat(response.getItems().get(1).getGroup()).isEqualTo("2022");
                    assertThat(response.getItems().get(1).getItems()).hasSize(2);
                    assertThat(response.getItems().get(1).getItems()).extracting(PeriodItem::getGroup).containsExactly("2022-08", "2022-05");
                    assertThat(response.getItems().get(1).getItems()).extracting(PeriodItem::getCount).containsExactly(1L, 2L);
                    assertThat(response.getItems().get(1).getItems()).extracting(PeriodItem::getResult).containsExactly(BigDecimal.valueOf(100.00), BigDecimal.valueOf(200.00));
                });

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/trade/aggregate/trade")
                        .queryParam("from", "2023-02-01 00:00:00")
                        .queryParam("until", "2023-02-28 23:59:59")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<TradesAggregated>>() {
                })
                .value(response -> {
                    assertThat(response).hasSize(2);
                    assertThat(response.get(0).getCount()).isEqualTo(1);
                    assertThat(response.get(0).getGroup()).isEqualTo("2023-02-05");
                    assertThat(response.get(0).getItems()).hasSize(1);
                    assertThat(response.get(0).getItems().get(0).getSymbol()).isEqualTo("HD");
                    assertThat(response.get(0).getItems().get(0).getNetResult()).isEqualTo(BigDecimal.valueOf(100));

                    assertThat(response.get(1).getCount()).isEqualTo(1);
                    assertThat(response.get(1).getGroup()).isEqualTo("2023-02-04");
                    assertThat(response.get(1).getItems()).hasSize(1);
                    assertThat(response.get(1).getItems().get(0).getSymbol()).isEqualTo("ABBV");
                    assertThat(response.get(1).getItems().get(0).getNetResult()).isEqualTo(BigDecimal.valueOf(100));
                });
    }
}