package com.trading.journal.entry.api;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@NoArgsConstructor
public class PageWrapper<T> {

    private int totalPages;
    private long total;
    private List<T> content;

    public PageWrapper(Page<T> page) {
        this.content = page.getContent();
        this.total = page.getTotalElements();
        this.totalPages = page.getTotalPages();
    }
}
