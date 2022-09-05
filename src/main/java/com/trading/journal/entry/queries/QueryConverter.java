package com.trading.journal.entry.queries;

import com.trading.journal.entry.ApplicationException;
import com.trading.journal.entry.queries.data.Filter;
import com.trading.journal.entry.queries.data.FilterOperation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QueryConverter {

    public static final String SPLIT_DELIMITER = "\\.";
    public static final int EXPECTED_LENGTH = 2;

    public static final int FIELD_INDEX = 0;
    public static final int OPERATION_INDEX = 1;

    public static Sort queryParamsToSort(String[] sort) {
        Sort sortable = Sort.by("id").ascending();
        if (sort != null && sort.length > 0) {
            if (sort.length % EXPECTED_LENGTH != 0) {
                throw new ApplicationException("Sort is invalid. It must be a pair of column and direction");
            }
            List<Sort.Order> orders = new ArrayList<>();
            String column = null;
            for (int index = 0; index < sort.length; index++) {
                if (index % EXPECTED_LENGTH == 0) {
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

    public static List<Filter> queryParamsToFilter(String[] filter) {
        List<Filter> filterList = new ArrayList<>();
        if (filter != null && filter.length > 0) {
            if (filter.length % EXPECTED_LENGTH != 0) {
                throw new ApplicationException("Filter is invalid. It must be a pair of FieldName.Operation and Value");
            }
            Filter.FilterBuilder filterBuilder = null;
            for (int index = 0; index < filter.length; index++) {
                if (index % EXPECTED_LENGTH == 0) {
                    filterBuilder = buildFilter(filter[index]);
                } else {
                    Filter build = filterBuilder.value(filter[index].trim()).build();
                    filterList.add(build);
                }
            }
        }
        return filterList;
    }

    private static Filter.FilterBuilder buildFilter(String filedAndOperation) {
        String[] fieldAdnOperator = filedAndOperation.trim().split(SPLIT_DELIMITER);
        if (fieldAdnOperator.length != EXPECTED_LENGTH) {
            throw new ApplicationException(String.format("Field name %s is invalid, must be FieldName.Operation", filedAndOperation));
        }
        FilterOperation operation = FilterOperation.fromValue(fieldAdnOperator[OPERATION_INDEX]);
        if (operation == null) {
            throw new ApplicationException(String.format("%s operation is invalid", fieldAdnOperator[OPERATION_INDEX]));
        }
        String field = fieldAdnOperator[FIELD_INDEX];
        return Filter.builder().field(field).operation(operation);
    }
}
