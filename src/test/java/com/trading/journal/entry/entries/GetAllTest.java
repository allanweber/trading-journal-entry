package com.trading.journal.entry.entries;

import com.trading.journal.entry.queries.data.Filter;
import com.trading.journal.entry.queries.data.FilterOperation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GetAllTest {

    @DisplayName("No filter")
    @Test
    void noFilter() {
        GetAll all = GetAll.builder().build();
        List<Filter> filters = all.filterAll();
        assertThat(filters).isEmpty();
    }

    @DisplayName("Symbol filter")
    @Test
    void symbol() {
        GetAll all = GetAll.builder().symbol("MSFT").build();
        List<Filter> filters = all.filterAll();
        assertThat(filters).hasSize(1);
        assertThat(filters).extracting(Filter::getField).containsExactly("symbol");
        assertThat(filters).extracting(Filter::getOperation).containsExactly(FilterOperation.EQUAL);
        assertThat(filters).extracting(Filter::getValue).containsExactly("MSFT");
    }

    @DisplayName("Type filter")
    @Test
    void type() {
        GetAll all = GetAll.builder().type(EntryType.DEPOSIT).build();
        List<Filter> filters = all.filterAll();
        assertThat(filters).hasSize(1);
        assertThat(filters).extracting(Filter::getField).containsExactly("type");
        assertThat(filters).extracting(Filter::getOperation).containsExactly(FilterOperation.EQUAL);
        assertThat(filters).extracting(Filter::getValue).containsExactly(EntryType.DEPOSIT.name());
    }

    @DisplayName("From when status null")
    @Test
    void fromNullStatus() {
        GetAll all = GetAll.builder().from("2022-01-01 00:00:00").build();
        List<Filter> filters = all.filterAll();
        assertThat(filters).hasSize(1);
        assertThat(filters).extracting(Filter::getField).containsExactly("date");
        assertThat(filters).extracting(Filter::getOperation).containsExactly(FilterOperation.GREATER_THAN_OR_EQUAL_TO);
        assertThat(filters).extracting(Filter::getValue).containsExactly("2022-01-01 00:00:00");
    }

    @DisplayName("From when status open")
    @Test
    void fromOpenStatus() {
        GetAll all = GetAll.builder().status(EntryStatus.OPEN).from("2022-01-01 00:00:00").build();
        List<Filter> filters = all.filterAll();
        assertThat(filters).hasSize(2);
        assertThat(filters).extracting(Filter::getField).containsExactly("date", "netResult");
        assertThat(filters).extracting(Filter::getOperation).containsExactly(FilterOperation.GREATER_THAN_OR_EQUAL_TO, FilterOperation.EXISTS);
        assertThat(filters).extracting(Filter::getValue).containsExactly("2022-01-01 00:00:00", "false");
    }

    @DisplayName("From when status Closed")
    @Test
    void fromClosedStatus() {
        GetAll all = GetAll.builder().status(EntryStatus.CLOSED).from("2022-01-01 00:00:00").build();
        List<Filter> filters = all.filterAll();
        assertThat(filters).hasSize(2);
        assertThat(filters).extracting(Filter::getField).containsExactly("exitDate", "netResult");
        assertThat(filters).extracting(Filter::getOperation).containsExactly(FilterOperation.GREATER_THAN_OR_EQUAL_TO, FilterOperation.EXISTS);
        assertThat(filters).extracting(Filter::getValue).containsExactly("2022-01-01 00:00:00", "true");
    }

    @DisplayName("Status open")
    @Test
    void openStatus() {
        GetAll all = GetAll.builder().status(EntryStatus.OPEN).build();
        List<Filter> filters = all.filterAll();
        assertThat(filters).hasSize(1);
        assertThat(filters).extracting(Filter::getField).containsExactly("netResult");
        assertThat(filters).extracting(Filter::getOperation).containsExactly(FilterOperation.EXISTS);
        assertThat(filters).extracting(Filter::getValue).containsExactly("false");
    }

    @DisplayName("Status Closed")
    @Test
    void closedStatus() {
        GetAll all = GetAll.builder().status(EntryStatus.CLOSED).build();
        List<Filter> filters = all.filterAll();
        assertThat(filters).hasSize(1);
        assertThat(filters).extracting(Filter::getField).containsExactly("netResult");
        assertThat(filters).extracting(Filter::getOperation).containsExactly(FilterOperation.EXISTS);
        assertThat(filters).extracting(Filter::getValue).containsExactly("true");
    }

    @DisplayName("Direction")
    @Test
    void direction() {
        GetAll all = GetAll.builder().direction(EntryDirection.LONG).build();
        List<Filter> filters = all.filterAll();
        assertThat(filters).hasSize(1);
        assertThat(filters).extracting(Filter::getField).containsExactly("direction");
        assertThat(filters).extracting(Filter::getOperation).containsExactly(FilterOperation.EQUAL);
        assertThat(filters).extracting(Filter::getValue).containsExactly(EntryDirection.LONG.name());
    }

    @DisplayName("Result WIN")
    @Test
    void win() {
        GetAll all = GetAll.builder().result(EntryResult.WIN).build();
        List<Filter> filters = all.filterAll();
        assertThat(filters).hasSize(1);
        assertThat(filters).extracting(Filter::getField).containsExactly("netResult");
        assertThat(filters).extracting(Filter::getOperation).containsExactly(FilterOperation.GREATER_THAN_OR_EQUAL_TO);
        assertThat(filters).extracting(Filter::getValue).containsExactly("0");
    }

    @DisplayName("Result LOSE")
    @Test
    void lose() {
        GetAll all = GetAll.builder().result(EntryResult.LOSE).build();
        List<Filter> filters = all.filterAll();
        assertThat(filters).hasSize(1);
        assertThat(filters).extracting(Filter::getField).containsExactly("netResult");
        assertThat(filters).extracting(Filter::getOperation).containsExactly(FilterOperation.LESS_THAN);
        assertThat(filters).extracting(Filter::getValue).containsExactly("0");
    }

    @DisplayName("Status is null sort by Date")
    @Test
    void sortByDate() {
        GetAll all = GetAll.builder().build();
        String sortBy = all.sortBy();
        assertThat(sortBy).isEqualTo("date");
    }

    @DisplayName("Status is open sort by Date")
    @Test
    void sortByDateStatusOpen() {
        GetAll all = GetAll.builder().status(EntryStatus.OPEN).build();
        String sortBy = all.sortBy();
        assertThat(sortBy).isEqualTo("date");
    }

    @DisplayName("Status is closed sort by Exit Date")
    @Test
    void sortByExitDate() {
        GetAll all = GetAll.builder().status(EntryStatus.CLOSED).build();
        String sortBy = all.sortBy();
        assertThat(sortBy).isEqualTo("exitDate");
    }
}