package com.trading.journal.entry.entries;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class EntriesQueryTest {

    @DisplayName("Journal filter")
    @Test
    void journal() {
        EntriesQuery entriesQuery = EntriesQuery.builder().journalId("123").build();
        Query query = entriesQuery.buildQuery();
        assertThat(query).isEqualTo(new Query()
                .addCriteria(Criteria.where("journalId").is("123"))
        );
    }

    @DisplayName("Symbol filter")
    @Test
    void symbol() {
        EntriesQuery entriesQuery = EntriesQuery.builder().symbol("MSFT").build();
        Query query = entriesQuery.buildQuery();
        assertThat(query).isEqualTo(new Query()
                .addCriteria(Criteria.where("journalId").is(null))
                .addCriteria(Criteria.where("symbol").is("MSFT"))
        );
    }

    @DisplayName("Type filter")
    @Test
    void type() {
        EntriesQuery entriesQuery = EntriesQuery.builder().type(EntryType.DEPOSIT).build();
        Query query = entriesQuery.buildQuery();
        assertThat(query).isEqualTo(new Query()
                .addCriteria(Criteria.where("journalId").is(null))
                .addCriteria(Criteria.where("type").is("DEPOSIT"))
        );
    }

    @DisplayName("From when status null")
    @Test
    void fromNullStatus() {
        EntriesQuery entriesQuery = EntriesQuery.builder().from("2022-01-01 00:00:00").build();
        Query query = entriesQuery.buildQuery();
        assertThat(query).isEqualTo(new Query()
                .addCriteria(Criteria.where("journalId").is(null))
                .addCriteria(Criteria.where("date").gte(LocalDateTime.parse("2022-01-01 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))));
    }

    @DisplayName("From when status open")
    @Test
    void fromOpenStatus() {
        EntriesQuery entriesQuery = EntriesQuery.builder().status(EntryStatus.OPEN).from("2022-01-01 00:00:00").build();
        Query query = entriesQuery.buildQuery();
        assertThat(query).isEqualTo(new Query()
                .addCriteria(Criteria.where("journalId").is(null))
                .addCriteria(Criteria.where("date").gte(LocalDateTime.parse("2022-01-01 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
                .addCriteria(Criteria.where("netResult").exists(false))
        );
    }

    @DisplayName("From when status Closed")
    @Test
    void fromClosedStatus() {
        EntriesQuery entriesQuery = EntriesQuery.builder().status(EntryStatus.CLOSED).from("2022-01-01 00:00:00").build();
        Query query = entriesQuery.buildQuery();
        assertThat(query).isEqualTo(new Query()
                .addCriteria(Criteria.where("journalId").is(null))
                .addCriteria(
                        new Criteria()
                                .orOperator(Criteria.where("type").is(EntryType.TRADE).and("exitDate").gte(LocalDateTime.parse("2022-01-01 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))),
                                        Criteria.where("type").ne(EntryType.TRADE).and("date").gte(LocalDateTime.parse("2022-01-01 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                                )
                )

                .addCriteria(Criteria.where("netResult").exists(true))
        );
    }

    @DisplayName("Status open")
    @Test
    void openStatus() {
        EntriesQuery entriesQuery = EntriesQuery.builder().status(EntryStatus.OPEN).build();
        Query query = entriesQuery.buildQuery();
        assertThat(query).isEqualTo(new Query()
                .addCriteria(Criteria.where("journalId").is(null))
                .addCriteria(Criteria.where("netResult").exists(false))
        );
    }

    @DisplayName("Status Closed")
    @Test
    void closedStatus() {
        EntriesQuery entriesQuery = EntriesQuery.builder().status(EntryStatus.CLOSED).build();
        Query query = entriesQuery.buildQuery();
        assertThat(query).isEqualTo(new Query()
                .addCriteria(Criteria.where("journalId").is(null))
                .addCriteria(Criteria.where("netResult").exists(true))
        );
    }

    @DisplayName("Direction Long")
    @Test
    void directionLong() {
        EntriesQuery entriesQuery = EntriesQuery.builder().direction(EntryDirection.LONG).build();
        Query query = entriesQuery.buildQuery();
        assertThat(query).isEqualTo(new Query()
                .addCriteria(Criteria.where("journalId").is(null))
                .addCriteria(Criteria.where("direction").is("LONG"))
        );
    }

    @DisplayName("Direction Short")
    @Test
    void directionShort() {
        EntriesQuery entriesQuery = EntriesQuery.builder().direction(EntryDirection.SHORT).build();
        Query query = entriesQuery.buildQuery();
        assertThat(query).isEqualTo(new Query()
                .addCriteria(Criteria.where("journalId").is(null))
                .addCriteria(Criteria.where("direction").is("SHORT"))
        );
    }

    @DisplayName("Closed Win")
    @Test
    void closedWin() {
        EntriesQuery entriesQuery = EntriesQuery.builder().status(EntryStatus.CLOSED).result(EntryResult.WIN).build();
        Query query = entriesQuery.buildQuery();
        assertThat(query).isEqualTo(new Query()
                .addCriteria(Criteria.where("journalId").is(null))
                .addCriteria(Criteria.where("netResult").exists(true).gte(BigDecimal.ZERO))
        );
    }

    @DisplayName("Closed Loose")
    @Test
    void closedLose() {
        EntriesQuery entriesQuery = EntriesQuery.builder().status(EntryStatus.CLOSED).result(EntryResult.LOSE).build();
        Query query = entriesQuery.buildQuery();
        assertThat(query).isEqualTo(new Query()
                .addCriteria(Criteria.where("journalId").is(null))
                .addCriteria(Criteria.where("netResult").exists(true).lt(BigDecimal.ZERO))
        );
    }

    @DisplayName("Open Win or loose only filter for open")
    @Test
    void openAndResult() {
        EntriesQuery open = EntriesQuery.builder().status(EntryStatus.OPEN).result(EntryResult.LOSE).build();
        Query openQuery = open.buildQuery();
        assertThat(openQuery).isEqualTo(new Query()
                .addCriteria(Criteria.where("journalId").is(null))
                .addCriteria(Criteria.where("netResult").exists(false))
        );

        EntriesQuery loose = EntriesQuery.builder().status(EntryStatus.OPEN).result(EntryResult.LOSE).build();
        Query looseQuery = loose.buildQuery();
        assertThat(looseQuery).isEqualTo(new Query()
                .addCriteria(Criteria.where("journalId").is(null))
                .addCriteria(Criteria.where("netResult").exists(false))
        );
    }

    @DisplayName("Result WIN")
    @Test
    void win() {
        EntriesQuery entriesQuery = EntriesQuery.builder().result(EntryResult.WIN).build();
        Query query = entriesQuery.buildQuery();
        assertThat(query).isEqualTo(new Query()
                .addCriteria(Criteria.where("journalId").is(null))
                .addCriteria(Criteria.where("netResult").gte(BigDecimal.ZERO))
        );
    }

    @DisplayName("Result LOSE")
    @Test
    void lose() {
        EntriesQuery entriesQuery = EntriesQuery.builder().result(EntryResult.LOSE).build();
        Query query = entriesQuery.buildQuery();
        assertThat(query).isEqualTo(new Query()
                .addCriteria(Criteria.where("journalId").is(null))
                .addCriteria(Criteria.where("netResult").lt(BigDecimal.ZERO))
        );
    }

    @DisplayName("By Strategies")
    @Test
    void strategies() {
        EntriesQuery entriesQuery = EntriesQuery.builder().strategyIds(asList("123456", "456789")).build();
        Query query = entriesQuery.buildQuery();
        assertThat(query).isEqualTo(new Query()
                .addCriteria(Criteria.where("journalId").is(null))
                .addCriteria(Criteria.where("strategyIds").in(asList("123456", "456789")))
        );
    }

    @DisplayName("With pageable sorting")
    @Test
    void pageSort() {
        PageRequest pageable = PageRequest.of(1, 20, Sort.by("date").ascending());
        EntriesQuery entriesQuery = EntriesQuery.builder().pageable(pageable).build();
        assertThat(entriesQuery.getPageable()).isEqualTo(pageable);
    }
}