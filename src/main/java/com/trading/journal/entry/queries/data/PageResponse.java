package com.trading.journal.entry.queries.data;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@NoArgsConstructor
@JsonPropertyOrder({"totalItems", "totalPages", "currentPage", "items"})
@Getter
public class PageResponse<T> {

    private long totalItems;
    private List<T> items;

    private int totalPages;

    private int currentPage;

    public PageResponse(Page<T> page) {
        this.items = page.getContent();
        this.totalItems = page.getTotalElements();
        this.currentPage = page.getNumber();
        this.totalPages = page.getTotalPages();
    }

    public PageResponse(long totalItems, List<T> items, int totalPages, int currentPage) {
        this.totalItems = totalItems;
        this.items = items;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
    }
}
