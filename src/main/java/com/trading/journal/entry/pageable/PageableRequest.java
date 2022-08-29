package com.trading.journal.entry.pageable;

import com.trading.journal.entry.ApplicationException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageableRequest {

    private static final String COMMA = ",";
    private int page;

    private int size;

    private String[] sort;

    public Pageable pageable() {
        Sort sortable = loadSort();
        return PageRequest.of(page, size, sortable);
    }

    private Sort loadSort() {
        Sort sortable = Sort.by("id").ascending();
        if (sort != null && sort.length > 0) {
            if (sort.length % 2 != 0) {
                throw new ApplicationException("Sort is invalid. It must be a pair of column and direction");
            }
            List<Sort.Order> orders = new ArrayList<>();
            String column = null;
            for (int index = 0; index < sort.length; index++) {
                if (index % 2 == 0) {
                    column = sort[index].trim();
                } else {
                    Sort.Direction direction = Sort.Direction.fromString(sort[index].trim());
                    orders.add(new Sort.Order(direction, column));

                }
            }
            sortable = Sort.by(orders);
        }
        return sortable;
    }
}
