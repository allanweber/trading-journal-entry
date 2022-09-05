package com.trading.journal.entry.core;

import com.trading.journal.entry.EntryForTest;
import com.trading.journal.entry.queries.data.FilterOperation;
import com.trading.journal.entry.queries.QueryCriteriaBuilder;
import com.trading.journal.entry.queries.data.Filter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class QueryCriteriaBuilderTest {

    @DisplayName("Check query built with multiple filters")
    @Test
    void test() {
        List<Filter> filters = asList(
                Filter.builder().field("symbol").operation(FilterOperation.EQUAL).value("abc").build(),
                Filter.builder().field("price").operation(FilterOperation.GREATER_THAN_OR_EQUAL_TO).value("123").build(),
                Filter.builder().field("date").operation(FilterOperation.EQUAL).value("2022-02-22").build(),
                Filter.builder().field("someInteger").operation(FilterOperation.LESS_THAN_OR_EQUAL_TO).value("5").build(),
                Filter.builder().field("someDecimal").operation(FilterOperation.GREATER_THAN).value("25.33").build()
        );

        Query query = new QueryCriteriaBuilder<>(EntryForTest.class).buildQuery(filters);

        String queryString = query.getQueryObject().get("$and").toString();

        assertThat(queryString)
                .contains("Document{{price=Document{{$gte=123.0}}}}");

        assertThat(queryString)
                .contains("Document{{symbol=abc}}");

        assertThat(queryString)
                .contains("Document{{date=Document{{$gte=2022-02-22T00:00, $lte=2022-02-22T23:59:59}}}}");

        assertThat(queryString)
                .contains("Document{{someInteger=Document{{$lte=5}}}");

        assertThat(queryString)
                .contains("Document{{someDecimal=Document{{$gt=25.33}}}}");
    }

    @DisplayName("Check query built with multiple filters")
    @Test
    void test2() {
        List<Filter> filters = asList(
                Filter.builder().field("symbol").operation(FilterOperation.EQUAL).value("abc").build(),
                Filter.builder().field("price").operation(FilterOperation.LESS_THAN).value("123").build(),
                Filter.builder().field("date").operation(FilterOperation.GREATER_THAN).value("2022-02-22").build(),
                Filter.builder().field("date").operation(FilterOperation.LESS_THAN).value("2022-02-22").build()
        );

        Query query = new QueryCriteriaBuilder<>(EntryForTest.class).buildQuery(filters);

        String queryString = query.getQueryObject().get("$and").toString();

        assertThat(queryString)
                .contains("Document{{price=Document{{$lt=123.0}}}}");

        assertThat(queryString)
                .contains("Document{{symbol=abc}}");

        assertThat(queryString)
                .contains("Document{{date=Document{{$gt=2022-02-22T00:00}}}}");
        assertThat(queryString)
                .contains("Document{{date=Document{{$lt=2022-02-22T23:59:59}}}}");
    }
}