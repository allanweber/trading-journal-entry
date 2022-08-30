package com.trading.journal.entry.pageable;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@JsonPropertyOrder({"totalItems", "totalPages", "currentPage", "items"})
@Getter
public class PageResponse<T> {

    private long totalItems;
    private List<T> items;

    private int totalPages;

    private int currentPage;

    public PageResponse(PageableRequest pageRequest, long total, List<T> items) {
        this.items = items;
        this.totalItems = total;
        this.currentPage = pageRequest.getPage();
        this.totalPages = (int) (total / pageRequest.getSize()) + ((int) (total % pageRequest.getSize()) > 0 ? 1 : 0);
    }
}
