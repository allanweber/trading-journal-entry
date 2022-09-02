package com.trading.journal.entry.query;

import com.trading.journal.entry.ApplicationException;
import com.trading.journal.entry.query.data.Filter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class PageableRequest {
    public static final String SPLIT_DELIMITER = "\\.";

    private int page;

    private int size;

    private String[] sort;

    private String[] filter;

    public Pageable pageable() {
        Sort sortable = loadSort();
        return PageRequest.of(page, size, sortable);
    }

    public List<Filter> getFilters() {
        return loadFilters();
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

    private List<Filter> loadFilters() {
        List<Filter> filterList = new ArrayList<>();
        if (filter != null && filter.length > 0) {
            if (filter.length % 2 != 0) {
                throw new ApplicationException("Filter is invalid. It must be a pair of FieldName.Operation and Value");
            }
            String field = null;
            FilterOperation operation = null;
            for (int index = 0; index < filter.length; index++) {
                if (index % 2 == 0) {
                    String[] fieldAdnOperator = filter[index].trim().split(SPLIT_DELIMITER);
                    if (fieldAdnOperator.length != 2) {
                        throw new ApplicationException(String.format("Field name %s is invalid, must be FieldName.Operation", filter[index].trim()));
                    }
                    field = fieldAdnOperator[0];
                    operation = FilterOperation.fromValue(fieldAdnOperator[1]);
                    if (operation == null) {
                        throw new ApplicationException(String.format("%s operation is invalid", fieldAdnOperator[1]));
                    }
                } else {
                    String value = filter[index].trim();
                    filterList.add(new Filter(field, operation, value));
                }
            }
        }
        return filterList;
    }

    private static List<String> split(String search, String delimiter) {
        return Stream.of(search.split(delimiter))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }
}
