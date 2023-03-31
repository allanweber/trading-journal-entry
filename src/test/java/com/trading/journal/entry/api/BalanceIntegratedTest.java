package com.trading.journal.entry.api;

import com.trading.journal.entry.balance.Balance;
import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.EntryDirection;
import com.trading.journal.entry.entries.EntryType;
import com.trading.journal.entry.entries.GraphType;
import com.trading.journal.entry.entries.deposit.Deposit;
import com.trading.journal.entry.entries.taxes.Taxes;
import com.trading.journal.entry.entries.trade.CloseTrade;
import com.trading.journal.entry.entries.trade.Trade;
import com.trading.journal.entry.entries.withdrawal.Withdrawal;
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
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BalanceIntegratedTest extends IntegratedTestWithJournal {

    @BeforeEach
    public void beforeEach() {
        mongoTemplate.dropCollection("TestTenancy_JOURNAL-1_entries");
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

    @DisplayName("Create entries and check for Balance")
    @Test
    void balance() {
        //Create an entry
        AtomicReference<String> entryMSFTId = new AtomicReference<>();
        Trade entryMSFT = Trade.builder()
                .date(LocalDateTime.of(2022, 1, 1, 12, 0, 0))
                .symbol("MSFT")
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(100.00))
                .size(BigDecimal.valueOf(15))
                .profitPrice(BigDecimal.valueOf(200.00))
                .lossPrice(BigDecimal.valueOf(80.00))
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure("1M")
                .build();
        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/trade")
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
        Trade entryAPPL = Trade.builder()
                .date(LocalDateTime.of(2022, 1, 2, 12, 0, 0))
                .symbol("APPL")
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(200.00))
                .size(BigDecimal.valueOf(20))
                .profitPrice(BigDecimal.valueOf(400.00))
                .lossPrice(BigDecimal.valueOf(150.00))
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure("1M")
                .build();
        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/trade")
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
        Trade entryNVDA = Trade.builder()
                .date(LocalDateTime.of(2022, 1, 3, 12, 0, 0))
                .symbol("NVDA")
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(375.00))
                .size(BigDecimal.valueOf(20))
                .profitPrice(BigDecimal.valueOf(150.00))
                .lossPrice(BigDecimal.valueOf(400.00))
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure("1M")
                .costs(BigDecimal.valueOf(50.00))
                .build();
        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/trade")
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
        Trade entryTSLA = Trade.builder()
                .date(LocalDateTime.of(2022, 1, 4, 12, 0, 0))
                .symbol("TSLA")
                .direction(EntryDirection.SHORT)
                .price(BigDecimal.valueOf(700))
                .size(BigDecimal.valueOf(30))
                .profitPrice(BigDecimal.valueOf(500.00))
                .lossPrice(BigDecimal.valueOf(745.00))
                .graphType(GraphType.CANDLESTICK)
                .graphMeasure("1M")
                .build();
        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/trade")
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

        //Check the Balance
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/balance")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(Balance.class)
                .value(response -> {
                            assertThat(response.getAccountBalance()).isEqualTo(BigDecimal.valueOf(10000.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getClosedPositions()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getDeposits()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getTaxes()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getWithdrawals()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));

                            assertThat(response.getStartBalance()).isEqualTo(BigDecimal.valueOf(10000.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getStartJournal()).isEqualTo(LocalDateTime.of(2022, 11, 10, 15, 25, 35));

                            assertThat(response.getOpenedPositions()).isEqualTo(BigDecimal.valueOf(34000.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getAvailable()).isEqualTo(BigDecimal.valueOf(-24000.00).setScale(2, RoundingMode.HALF_EVEN));
                        }
                );

        //Check entries not finished are 4 and finish is 0
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(new ParameterizedTypeReference<PageWrapper<Entry>>() {
                })
                .value(response -> {
                    assertThat(response.getContent()).hasSize(4);
                    List<Entry> finished = response.getContent().stream().filter(Entry::isFinished).toList();
                    assertThat(finished).isEmpty();

                    List<Entry> notFinished = response.getContent().stream().filter(entry -> !entry.isFinished()).toList();
                    assertThat(notFinished).hasSize(4);
                });

        //Exit SECOND added trade, APPL winning
        CloseTrade closeAPPL = CloseTrade.builder()
                .exitPrice(BigDecimal.valueOf(400.00))
                .exitDate(LocalDateTime.of(2022, 1, 2, 12, 0, 0))
                .build();
        webTestClient
                .patch()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/trade/{trade-id}/close")
                        .build(journalId, entryAPPLId.get()))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(closeAPPL)
                .exchange()
                .expectBody(Entry.class)
                .value(response -> {
                    assertThat(response.getPlannedRR()).isEqualTo(BigDecimal.valueOf(4.00).setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountRisked()).isEqualTo(BigDecimal.valueOf(0.1000).setScale(4, RoundingMode.HALF_EVEN));
                    assertThat(response.getProfitPrice()).isEqualTo(BigDecimal.valueOf(400.00));
                    assertThat(response.getLossPrice()).isEqualTo(BigDecimal.valueOf(150.00));
                    assertThat(response.getExitPrice()).isEqualTo(BigDecimal.valueOf(400.00));

                    assertThat(response.getGrossResult()).isEqualTo(BigDecimal.valueOf(4000.00).setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(response.getNetResult()).isEqualTo(BigDecimal.valueOf(4000.00).setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountChange()).isEqualTo(BigDecimal.valueOf(0.4000).setScale(4, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountBalance()).isEqualTo(BigDecimal.valueOf(14000.00).setScale(2, RoundingMode.HALF_EVEN));
                });

        //Check the Balance
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/balance")
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

                            assertThat(response.getStartBalance()).isEqualTo(BigDecimal.valueOf(10000.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getStartJournal()).isEqualTo(LocalDateTime.of(2022, 11, 10, 15, 25, 35));

                            assertThat(response.getOpenedPositions()).isEqualTo(BigDecimal.valueOf(30000.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getAvailable()).isEqualTo(BigDecimal.valueOf(-16000.00).setScale(2, RoundingMode.HALF_EVEN));
                        }
                );

        //Check entries not finished 3 and finish 1
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(new ParameterizedTypeReference<PageWrapper<Entry>>() {
                })
                .value(response -> {
                    assertThat(response.getContent()).hasSize(4);
                    List<Entry> finished = response.getContent().stream().filter(Entry::isFinished).toList();
                    assertThat(finished).hasSize(1);
                    assertThat(finished).extracting(Entry::getSymbol).containsExactly("APPL");

                    List<Entry> notFinished = response.getContent().stream().filter(entry -> !entry.isFinished()).toList();
                    assertThat(notFinished).hasSize(3);
                    assertThat(notFinished).extracting(Entry::getSymbol).containsExactlyInAnyOrder(
                            "MSFT", "NVDA", "TSLA"
                    );
                });

        //Exit LAST added trade, TSLA losing
        CloseTrade closeTSLA = CloseTrade.builder()
                .exitPrice(BigDecimal.valueOf(745.00))
                .exitDate(LocalDateTime.of(2022, 1, 5, 12, 0, 0))
                .build();
        webTestClient
                .patch()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/trade/{trade-id}/close")
                        .build(journalId, entryTSLAId.get()))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(closeTSLA)
                .exchange()
                .expectBody(Entry.class)
                .value(response -> {
                    assertThat(response.getPlannedRR()).isEqualTo(BigDecimal.valueOf(4.44).setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountRisked()).isEqualTo(BigDecimal.valueOf(0.0964).setScale(4, RoundingMode.HALF_EVEN));
                    assertThat(response.getProfitPrice()).isEqualTo(BigDecimal.valueOf(500.00));
                    assertThat(response.getLossPrice()).isEqualTo(BigDecimal.valueOf(745.00));
                    assertThat(response.getExitPrice()).isEqualTo(BigDecimal.valueOf(745.00));

                    assertThat(response.getGrossResult()).isEqualTo(BigDecimal.valueOf(-1350.00).setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(response.getNetResult()).isEqualTo(BigDecimal.valueOf(-1350.00).setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountChange()).isEqualTo(BigDecimal.valueOf(-0.0964).setScale(4, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountBalance()).isEqualTo(BigDecimal.valueOf(12650.00).setScale(2, RoundingMode.HALF_EVEN));
                });

        //Check the Balance
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/balance")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(Balance.class)
                .value(response -> {
                            assertThat(response.getAccountBalance()).isEqualTo(BigDecimal.valueOf(12650.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getClosedPositions()).isEqualTo(BigDecimal.valueOf(2650.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getDeposits()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getTaxes()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getWithdrawals()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));

                            assertThat(response.getStartBalance()).isEqualTo(BigDecimal.valueOf(10000.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getStartJournal()).isEqualTo(LocalDateTime.of(2022, 11, 10, 15, 25, 35));

                            assertThat(response.getOpenedPositions()).isEqualTo(BigDecimal.valueOf(9000.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getAvailable()).isEqualTo(BigDecimal.valueOf(3650.00).setScale(2, RoundingMode.HALF_EVEN));
                        }
                );

        //Check entries not finished 2 and finished 2
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(new ParameterizedTypeReference<PageWrapper<Entry>>() {
                })
                .value(response -> {
                    assertThat(response.getContent()).hasSize(4);
                    List<Entry> finished = response.getContent().stream().filter(Entry::isFinished).toList();
                    assertThat(finished).hasSize(2);
                    assertThat(finished).extracting(Entry::getSymbol).containsExactlyInAnyOrder(
                            "APPL", "TSLA"
                    );

                    List<Entry> notFinished = response.getContent().stream().filter(entry -> !entry.isFinished()).toList();
                    assertThat(notFinished).hasSize(2);
                    assertThat(notFinished).extracting(Entry::getSymbol).containsExactlyInAnyOrder(
                            "MSFT", "NVDA"
                    );
                });

        //Exit THIRD added trade, NVDA winning
        CloseTrade closeNVDA = CloseTrade.builder()
                .exitPrice(BigDecimal.valueOf(160).setScale(2, RoundingMode.HALF_EVEN))
                .exitDate(LocalDateTime.of(2022, 1, 6, 12, 0, 0))
                .build();
        webTestClient
                .patch()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/trade/{trade-id}/close")
                        .build(journalId, entryNVDAId.get()))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(closeNVDA)
                .exchange()
                .expectBody(Entry.class)
                .value(response -> {
                    assertThat(response.getPlannedRR()).isEqualTo(BigDecimal.valueOf(9.00).setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountRisked()).isEqualTo(BigDecimal.valueOf(0.0395).setScale(4, RoundingMode.HALF_EVEN));
                    assertThat(response.getProfitPrice()).isEqualTo(BigDecimal.valueOf(150.00));
                    assertThat(response.getLossPrice()).isEqualTo(BigDecimal.valueOf(400.00));
                    assertThat(response.getCosts()).isEqualTo(BigDecimal.valueOf(50.00));
                    assertThat(response.getExitPrice()).isEqualTo(BigDecimal.valueOf(160.00).setScale(2, RoundingMode.HALF_EVEN));

                    assertThat(response.getGrossResult()).isEqualTo(BigDecimal.valueOf(4300.00).setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(response.getNetResult()).isEqualTo(BigDecimal.valueOf(4250.00).setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountChange()).isEqualTo(BigDecimal.valueOf(0.3360).setScale(4, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountBalance()).isEqualTo(BigDecimal.valueOf(16900.00).setScale(2, RoundingMode.HALF_EVEN));
                });

        //Check the Balance
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/balance")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(Balance.class)
                .value(response -> {
                            assertThat(response.getAccountBalance()).isEqualTo(BigDecimal.valueOf(16900.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getClosedPositions()).isEqualTo(BigDecimal.valueOf(6900.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getDeposits()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getTaxes()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getWithdrawals()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));

                            assertThat(response.getStartBalance()).isEqualTo(BigDecimal.valueOf(10000.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getStartJournal()).isEqualTo(LocalDateTime.of(2022, 11, 10, 15, 25, 35));

                            assertThat(response.getOpenedPositions()).isEqualTo(BigDecimal.valueOf(1500.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getAvailable()).isEqualTo(BigDecimal.valueOf(15400.00).setScale(2, RoundingMode.HALF_EVEN));
                        }
                );

        //Check entries not finished 1 and finished 3
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(new ParameterizedTypeReference<PageWrapper<Entry>>() {
                })
                .value(response -> {
                    assertThat(response.getContent()).hasSize(4);
                    List<Entry> finished = response.getContent().stream().filter(Entry::isFinished).toList();
                    assertThat(finished).hasSize(3);
                    assertThat(finished).extracting(Entry::getSymbol).containsExactlyInAnyOrder(
                            "APPL", "NVDA", "TSLA"
                    );

                    List<Entry> notFinished = response.getContent().stream().filter(entry -> !entry.isFinished()).toList();
                    assertThat(notFinished).hasSize(1);
                    assertThat(notFinished).extracting(Entry::getSymbol).containsExactly(
                            "MSFT"
                    );
                });

        // WITHDRAWAL
        Withdrawal withdrawal = Withdrawal.builder()
                .date(LocalDateTime.now())
                .price(BigDecimal.valueOf(6000.00).setScale(2, RoundingMode.HALF_EVEN))
                .build();
        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/withdrawal")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(withdrawal)
                .exchange()
                .expectBody(Entry.class)
                .value(response -> {
                    assertThat(response.getPlannedRR()).isNull();
                    assertThat(response.getAccountRisked()).isNull();
                    assertThat(response.getProfitPrice()).isNull();
                    assertThat(response.getLossPrice()).isNull();
                    assertThat(response.getCosts()).isNull();
                    assertThat(response.getExitPrice()).isNull();
                    assertThat(response.getGrossResult()).isNull();

                    assertThat(response.getNetResult()).isEqualTo(BigDecimal.valueOf(-6000.00).setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountChange()).isEqualTo(BigDecimal.valueOf(-0.3550).setScale(4, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountBalance()).isEqualTo(BigDecimal.valueOf(10900.00).setScale(2, RoundingMode.HALF_EVEN));
                });

        //Check the Balance
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/balance")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(Balance.class)
                .value(response -> {
                            assertThat(response.getAccountBalance()).isEqualTo(BigDecimal.valueOf(10900.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getClosedPositions()).isEqualTo(BigDecimal.valueOf(6900.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getDeposits()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getTaxes()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getWithdrawals()).isEqualTo(BigDecimal.valueOf(6000.00).setScale(2, RoundingMode.HALF_EVEN));

                            assertThat(response.getStartBalance()).isEqualTo(BigDecimal.valueOf(10000.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getStartJournal()).isEqualTo(LocalDateTime.of(2022, 11, 10, 15, 25, 35));

                            assertThat(response.getOpenedPositions()).isEqualTo(BigDecimal.valueOf(1500.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getAvailable()).isEqualTo(BigDecimal.valueOf(9400.00).setScale(2, RoundingMode.HALF_EVEN));
                        }
                );

        //Check 5 Entries, 4 finished, 3 trades finished, 1 not finished, one entry WITHDRAWAL
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(new ParameterizedTypeReference<PageWrapper<Entry>>() {
                })
                .value(response -> {
                    assertThat(response.getContent()).hasSize(5);

                    List<Entry> finished = response.getContent().stream().filter(Entry::isFinished).toList();
                    assertThat(finished).hasSize(4);

                    List<Entry> tradesFinished = response.getContent().stream().filter(entry -> EntryType.TRADE.equals(entry.getType())).filter(Entry::isFinished).toList();
                    assertThat(tradesFinished).extracting(Entry::getSymbol).containsExactlyInAnyOrder(
                            "APPL", "NVDA", "TSLA"
                    );

                    List<Entry> notFinished = response.getContent().stream().filter(entry -> !entry.isFinished()).toList();
                    assertThat(notFinished).hasSize(1);
                    assertThat(notFinished).extracting(Entry::getSymbol).containsExactly(
                            "MSFT"
                    );
                    assertThat(notFinished).extracting(Entry::getType).containsExactly(
                            EntryType.TRADE
                    );

                    List<Entry> withdrawals = response.getContent().stream().filter(entry -> EntryType.WITHDRAWAL.equals(entry.getType())).filter(Entry::isFinished).toList();
                    assertThat(withdrawals).hasSize(1);
                });

        //TAXES
        Taxes taxes = Taxes.builder()
                .date(LocalDateTime.now())
                .price(BigDecimal.valueOf(400.00).setScale(2, RoundingMode.HALF_EVEN))
                .build();
        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/taxes")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(taxes)
                .exchange()
                .expectBody(Entry.class)
                .value(response -> {
                    assertThat(response.getPlannedRR()).isNull();
                    assertThat(response.getAccountRisked()).isNull();
                    assertThat(response.getProfitPrice()).isNull();
                    assertThat(response.getLossPrice()).isNull();
                    assertThat(response.getCosts()).isNull();
                    assertThat(response.getExitPrice()).isNull();
                    assertThat(response.getGrossResult()).isNull();

                    assertThat(response.getNetResult()).isEqualTo(BigDecimal.valueOf(-400.00).setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountChange()).isEqualTo(BigDecimal.valueOf(-0.03669).setScale(4, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountBalance()).isEqualTo(BigDecimal.valueOf(10500.00).setScale(2, RoundingMode.HALF_EVEN));
                });

        //Check the Balance
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/balance")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(Balance.class)
                .value(response -> {
                            assertThat(response.getAccountBalance()).isEqualTo(BigDecimal.valueOf(10500.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getClosedPositions()).isEqualTo(BigDecimal.valueOf(6900.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getDeposits()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getTaxes()).isEqualTo(BigDecimal.valueOf(400.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getWithdrawals()).isEqualTo(BigDecimal.valueOf(6000.00).setScale(2, RoundingMode.HALF_EVEN));

                            assertThat(response.getStartBalance()).isEqualTo(BigDecimal.valueOf(10000.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getStartJournal()).isEqualTo(LocalDateTime.of(2022, 11, 10, 15, 25, 35));

                            assertThat(response.getOpenedPositions()).isEqualTo(BigDecimal.valueOf(1500.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getAvailable()).isEqualTo(BigDecimal.valueOf(9000.00).setScale(2, RoundingMode.HALF_EVEN));
                        }
                );

        //Check 6 Entries, 5 finished, 3 trades finished, 1 not finished, one entry WITHDRAWAL, one trade TAXES
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(new ParameterizedTypeReference<PageWrapper<Entry>>() {
                })
                .value(response -> {
                    assertThat(response.getContent()).hasSize(6);

                    List<Entry> finished = response.getContent().stream().filter(Entry::isFinished).toList();
                    assertThat(finished).hasSize(5);

                    List<Entry> tradesFinished = response.getContent().stream().filter(entry -> EntryType.TRADE.equals(entry.getType())).filter(Entry::isFinished).toList();
                    assertThat(tradesFinished).extracting(Entry::getSymbol).containsExactlyInAnyOrder(
                            "APPL", "NVDA", "TSLA"
                    );

                    List<Entry> notFinished = response.getContent().stream().filter(entry -> !entry.isFinished()).toList();
                    assertThat(notFinished).hasSize(1);
                    assertThat(notFinished).extracting(Entry::getSymbol).containsExactly(
                            "MSFT"
                    );
                    assertThat(notFinished).extracting(Entry::getType).containsExactly(
                            EntryType.TRADE
                    );

                    List<Entry> withdrawals = response.getContent().stream().filter(entry -> EntryType.WITHDRAWAL.equals(entry.getType())).filter(Entry::isFinished).toList();
                    assertThat(withdrawals).hasSize(1);

                    List<Entry> taxesEntries = response.getContent().stream().filter(entry -> EntryType.TAXES.equals(entry.getType())).filter(Entry::isFinished).toList();
                    assertThat(taxesEntries).hasSize(1);
                });

        //LOSE MSFT losing
        CloseTrade closeMSFT = CloseTrade.builder()
                .exitPrice(BigDecimal.valueOf(80.00).setScale(2, RoundingMode.HALF_EVEN))
                .exitDate(LocalDateTime.of(2022, 1, 7, 12, 0, 0))
                .build();
        webTestClient
                .patch()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/trade/{trade-id}/close")
                        .build(journalId, entryMSFTId.get()))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(closeMSFT)
                .exchange()
                .expectBody(Entry.class)
                .value(response -> {
                    assertThat(response.getPlannedRR()).isEqualTo(BigDecimal.valueOf(5.00).setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountRisked()).isEqualTo(BigDecimal.valueOf(0.0286).setScale(4, RoundingMode.HALF_EVEN));
                    assertThat(response.getProfitPrice()).isEqualTo(BigDecimal.valueOf(200.00));
                    assertThat(response.getLossPrice()).isEqualTo(BigDecimal.valueOf(80.00));
                    assertThat(response.getExitPrice()).isEqualTo(BigDecimal.valueOf(80.00).setScale(2, RoundingMode.HALF_EVEN));

                    assertThat(response.getGrossResult()).isEqualTo(BigDecimal.valueOf(-300.00).setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(response.getNetResult()).isEqualTo(BigDecimal.valueOf(-300.00).setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountChange()).isEqualTo(BigDecimal.valueOf(-0.0286).setScale(4, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountBalance()).isEqualTo(BigDecimal.valueOf(10200.00).setScale(2, RoundingMode.HALF_EVEN));
                });

        //Check the Balance
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/balance")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(Balance.class)
                .value(response -> {
                            assertThat(response.getAccountBalance()).isEqualTo(BigDecimal.valueOf(10200.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getClosedPositions()).isEqualTo(BigDecimal.valueOf(6600.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getDeposits()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getTaxes()).isEqualTo(BigDecimal.valueOf(400.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getWithdrawals()).isEqualTo(BigDecimal.valueOf(6000.00).setScale(2, RoundingMode.HALF_EVEN));

                            assertThat(response.getStartBalance()).isEqualTo(BigDecimal.valueOf(10000.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getStartJournal()).isEqualTo(LocalDateTime.of(2022, 11, 10, 15, 25, 35));

                            assertThat(response.getOpenedPositions()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getAvailable()).isEqualTo(BigDecimal.valueOf(10200.00).setScale(2, RoundingMode.HALF_EVEN));
                        }
                );

        //Check 6 Entries, 6 finished, 4 trades finished, 0 not finished, one entry WITHDRAWAL, one trade TAXES
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(new ParameterizedTypeReference<PageWrapper<Entry>>() {
                })
                .value(response -> {
                    assertThat(response.getContent()).hasSize(6);

                    List<Entry> finished = response.getContent().stream().filter(Entry::isFinished).toList();
                    assertThat(finished).hasSize(6);

                    List<Entry> tradesFinished = response.getContent().stream().filter(entry -> EntryType.TRADE.equals(entry.getType())).filter(Entry::isFinished).toList();
                    assertThat(tradesFinished).extracting(Entry::getSymbol).containsExactlyInAnyOrder(
                            "APPL", "NVDA", "TSLA", "MSFT"
                    );

                    List<Entry> notFinished = response.getContent().stream().filter(entry -> !entry.isFinished()).toList();
                    assertThat(notFinished).isEmpty();

                    List<Entry> withdrawals = response.getContent().stream().filter(entry -> EntryType.WITHDRAWAL.equals(entry.getType())).filter(Entry::isFinished).toList();
                    assertThat(withdrawals).hasSize(1);

                    List<Entry> taxesEntries = response.getContent().stream().filter(entry -> EntryType.TAXES.equals(entry.getType())).filter(Entry::isFinished).toList();
                    assertThat(taxesEntries).hasSize(1);
                });

        // DEPOSIT
        Deposit deposit = Deposit.builder()
                .date(LocalDateTime.now())
                .price(BigDecimal.valueOf(3000.00).setScale(2, RoundingMode.HALF_EVEN))
                .build();
        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/deposit")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(deposit)
                .exchange()
                .expectBody(Entry.class)
                .value(response -> {
                    assertThat(response.getPlannedRR()).isNull();
                    assertThat(response.getAccountRisked()).isNull();
                    assertThat(response.getProfitPrice()).isNull();
                    assertThat(response.getLossPrice()).isNull();
                    assertThat(response.getCosts()).isNull();
                    assertThat(response.getExitPrice()).isNull();
                    assertThat(response.getGrossResult()).isNull();

                    assertThat(response.getNetResult()).isEqualTo(BigDecimal.valueOf(3000.00).setScale(2, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountChange()).isEqualTo(BigDecimal.valueOf(0.2941).setScale(4, RoundingMode.HALF_EVEN));
                    assertThat(response.getAccountBalance()).isEqualTo(BigDecimal.valueOf(13200.00).setScale(2, RoundingMode.HALF_EVEN));
                });

        //Check the Balance
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/balance")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(Balance.class)
                .value(response -> {
                            assertThat(response.getAccountBalance()).isEqualTo(BigDecimal.valueOf(13200.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getClosedPositions()).isEqualTo(BigDecimal.valueOf(6600.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getDeposits()).isEqualTo(BigDecimal.valueOf(3000.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getTaxes()).isEqualTo(BigDecimal.valueOf(400.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getWithdrawals()).isEqualTo(BigDecimal.valueOf(6000.00).setScale(2, RoundingMode.HALF_EVEN));

                            assertThat(response.getStartBalance()).isEqualTo(BigDecimal.valueOf(10000.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getStartJournal()).isEqualTo(LocalDateTime.of(2022, 11, 10, 15, 25, 35));

                            assertThat(response.getOpenedPositions()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getAvailable()).isEqualTo(BigDecimal.valueOf(13200.00).setScale(2, RoundingMode.HALF_EVEN));
                        }
                );

        //Check 7 Entries, 7 finished, 4 trades finished, 0 not finished, one entry WITHDRAWAL, one trade TAXES, one DEPOSIT
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(new ParameterizedTypeReference<PageWrapper<Entry>>() {
                })
                .value(response -> {
                    assertThat(response.getContent()).hasSize(7);

                    List<Entry> finished = response.getContent().stream().filter(Entry::isFinished).toList();
                    assertThat(finished).hasSize(7);

                    List<Entry> tradesFinished = response.getContent().stream().filter(entry -> EntryType.TRADE.equals(entry.getType())).filter(Entry::isFinished).toList();
                    assertThat(tradesFinished).extracting(Entry::getSymbol).containsExactlyInAnyOrder(
                            "APPL", "NVDA", "TSLA", "MSFT"
                    );

                    List<Entry> notFinished = response.getContent().stream().filter(entry -> !entry.isFinished()).toList();
                    assertThat(notFinished).isEmpty();

                    List<Entry> withdrawals = response.getContent().stream().filter(entry -> EntryType.WITHDRAWAL.equals(entry.getType())).filter(Entry::isFinished).toList();
                    assertThat(withdrawals).hasSize(1);

                    List<Entry> taxesEntries = response.getContent().stream().filter(entry -> EntryType.TAXES.equals(entry.getType())).filter(Entry::isFinished).toList();
                    assertThat(taxesEntries).hasSize(1);

                    List<Entry> deposits = response.getContent().stream().filter(entry -> EntryType.DEPOSIT.equals(entry.getType())).filter(Entry::isFinished).toList();
                    assertThat(deposits).hasSize(1);
                });

        //DELETE A TRADE
        webTestClient
                .delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries/{entry-id}")
                        .build(journalId, entryAPPLId.get()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk();

        //Check the Balance
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/balance")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(Balance.class)
                .value(response -> {
                            assertThat(response.getAccountBalance()).isEqualTo(BigDecimal.valueOf(9200.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getClosedPositions()).isEqualTo(BigDecimal.valueOf(2600.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getDeposits()).isEqualTo(BigDecimal.valueOf(3000.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getTaxes()).isEqualTo(BigDecimal.valueOf(400.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getWithdrawals()).isEqualTo(BigDecimal.valueOf(6000.00).setScale(2, RoundingMode.HALF_EVEN));

                            assertThat(response.getStartBalance()).isEqualTo(BigDecimal.valueOf(10000.00).setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getStartJournal()).isEqualTo(LocalDateTime.of(2022, 11, 10, 15, 25, 35));

                            assertThat(response.getOpenedPositions()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
                            assertThat(response.getAvailable()).isEqualTo(BigDecimal.valueOf(9200.00).setScale(2, RoundingMode.HALF_EVEN));
                        }
                );

        //Check 6 Entries, 6 finished, 3 trades finished, 0 not finished, one entry WITHDRAWAL, one trade TAXES, one DEPOSIT
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/journals/{journal-id}/entries")
                        .build(journalId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(new ParameterizedTypeReference<PageWrapper<Entry>>() {
                })
                .value(response -> {
                    assertThat(response.getContent()).hasSize(6);

                    List<Entry> finished = response.getContent().stream().filter(Entry::isFinished).toList();
                    assertThat(finished).hasSize(6);

                    List<Entry> tradesFinished = response.getContent().stream().filter(entry -> EntryType.TRADE.equals(entry.getType())).filter(Entry::isFinished).toList();
                    assertThat(tradesFinished).extracting(Entry::getSymbol).containsExactlyInAnyOrder(
                            "NVDA", "TSLA", "MSFT"
                    );

                    List<Entry> notFinished = response.getContent().stream().filter(entry -> !entry.isFinished()).toList();
                    assertThat(notFinished).isEmpty();

                    List<Entry> withdrawals = response.getContent().stream().filter(entry -> EntryType.WITHDRAWAL.equals(entry.getType())).filter(Entry::isFinished).toList();
                    assertThat(withdrawals).hasSize(1);

                    List<Entry> taxesEntries = response.getContent().stream().filter(entry -> EntryType.TAXES.equals(entry.getType())).filter(Entry::isFinished).toList();
                    assertThat(taxesEntries).hasSize(1);

                    List<Entry> deposits = response.getContent().stream().filter(entry -> EntryType.DEPOSIT.equals(entry.getType())).filter(Entry::isFinished).toList();
                    assertThat(deposits).hasSize(1);
                });
    }
}