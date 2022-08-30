package com.trading.journal.entry.pageable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class PageResponseTest {

    @DisplayName("100 total items, 10 pages")
    @Test
    void pageResponse() {
        PageableRequest pageableRequest = PageableRequest.builder()
                .page(0)
                .size(10)
                .build();
        long total = 100;
        List<Integer> items = IntStream.rangeClosed(1, 10).boxed().toList();

        PageResponse<Integer> pageResponse = new PageResponse<>(pageableRequest, total, items);

        assertThat(pageResponse.getCurrentPage()).isEqualTo(0);
        assertThat(pageResponse.getTotalPages()).isEqualTo(10);
        assertThat(pageResponse.getTotalItems()).isEqualTo(100);
        assertThat(pageResponse.getItems()).hasSize(10);
    }

    @DisplayName("31 total items, 4 pages")
    @Test
    void pageResponse4Pages() {
        PageableRequest pageableRequest = PageableRequest.builder()
                .page(0)
                .size(10)
                .build();
        long total = 31;
        List<Integer> items = IntStream.rangeClosed(1, 10).boxed().toList();

        PageResponse<Integer> pageResponse = new PageResponse<>(pageableRequest, total, items);

        assertThat(pageResponse.getCurrentPage()).isEqualTo(0);
        assertThat(pageResponse.getTotalPages()).isEqualTo(4);
        assertThat(pageResponse.getTotalItems()).isEqualTo(31);
        assertThat(pageResponse.getItems()).hasSize(10);
    }

    @DisplayName("29 total items, 3 pages")
    @Test
    void pageResponse3Pages() {
        PageableRequest pageableRequest = PageableRequest.builder()
                .page(0)
                .size(10)
                .build();
        long total = 29;
        List<Integer> items = IntStream.rangeClosed(1, 10).boxed().toList();

        PageResponse<Integer> pageResponse = new PageResponse<>(pageableRequest, total, items);

        assertThat(pageResponse.getCurrentPage()).isEqualTo(0);
        assertThat(pageResponse.getTotalPages()).isEqualTo(3);
        assertThat(pageResponse.getTotalItems()).isEqualTo(29);
        assertThat(pageResponse.getItems()).hasSize(10);
    }
}