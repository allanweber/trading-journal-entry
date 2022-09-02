package com.trading.journal.entry.core;

import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.query.PageableRequest;
import com.trading.journal.entry.query.QueryCriteriaBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QueryCriteriaBuilderTest {

    @DisplayName("")
    @Test
    void test() {
        String[] filter = {"symbol.eq", "abc", "price.gte", "123", "date.btn", "2022-02-22"};
        PageableRequest pageableRequest = PageableRequest.builder().filter(filter).build();
        new QueryCriteriaBuilder<>(Entry.class).buildQuery(pageableRequest.getFilters());
    }
}