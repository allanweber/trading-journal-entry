package com.trading.journal.entry.queries.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class PageableRequest {

    public static final String ID_COLUMN = "id";
    private int page;

    @Builder.Default
    private int size = 100;

    @Builder.Default
    private Sort sort = Sort.by(ID_COLUMN).ascending();

    private List<Filter> filters;

    public Pageable pageable() {
        return PageRequest.of(page, size, sort);
    }
}
