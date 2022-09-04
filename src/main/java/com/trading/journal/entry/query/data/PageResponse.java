package com.trading.journal.entry.query.data;

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
}
