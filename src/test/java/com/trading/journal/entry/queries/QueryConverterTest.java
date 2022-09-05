package com.trading.journal.entry.queries;

import com.trading.journal.entry.ApplicationException;
import com.trading.journal.entry.queries.data.Filter;
import com.trading.journal.entry.queries.data.FilterOperation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

class QueryConverterTest {

    @DisplayName("Load sort from query parameters to Sort object")
    @Test
    void pageable() {
        String[] sortParam = new String[]{"name", "asc", "id", "desc", "age", "asc"};

        Sort sort = QueryConverter.queryParamsToSort(sortParam);

        assertThat(sort.get()).hasSize(3);
        assertThat(Objects.requireNonNull(sort.getOrderFor("name")).getDirection()).isEqualTo(Sort.Direction.ASC);
        assertThat(Objects.requireNonNull(sort.getOrderFor("id")).getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(Objects.requireNonNull(sort.getOrderFor("age")).getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @DisplayName("Load sort from query parameters to Sort object is empty return only sort for ID")
    @Test
    void pageableNoSort() {
        Sort sort = QueryConverter.queryParamsToSort(new String[]{});

        assertThat(sort.get()).hasSize(1);
        assertThat(Objects.requireNonNull(sort.getOrderFor("id")).getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @DisplayName("Load sort from query parameters to Sort object with extra spaces in sort")
    @Test
    void pageableSort() {
        String[] sortParam = new String[]{" name", "  asc", "id  ", "desc  ", "age ", " asc"};

        Sort sort = QueryConverter.queryParamsToSort(sortParam);

        assertThat(sort.get()).hasSize(3);
        assertThat(Objects.requireNonNull(sort.getOrderFor("name")).getDirection()).isEqualTo(Sort.Direction.ASC);
        assertThat(Objects.requireNonNull(sort.getOrderFor("id")).getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(Objects.requireNonNull(sort.getOrderFor("age")).getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @DisplayName("Load sort from query parameters to Sort object throws exception when sort array is not odd")
    @Test
    void pageableSortException() {
        String[] sortParam = new String[]{"name", "desc", "age"};

        ApplicationException exception = assertThrows(ApplicationException.class, () -> QueryConverter.queryParamsToSort(sortParam));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getStatusText()).isEqualTo("Sort is invalid. It must be a pair of column and direction");
    }

    @DisplayName("Load sort from query parameters to Sort object throws exception when direction is not valid (asc or desc)")
    @Test
    void pageableDirectionException() {
        String[] sortParam = new String[]{"name", "asc", "id", "abc", "age", "asc"};

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> QueryConverter.queryParamsToSort(sortParam));
        assertThat(exception.getMessage()).contains("Invalid value 'abc' for orders given; Has to be either 'desc' or 'asc'");
    }

    @DisplayName("Load Filters")
    @Test
    void filters() {
        String[] filterParam = {"field1.eq", "abc", "field2.gte", "123", "field3.lt", "2022-02-22"};

        List<Filter> filters = QueryConverter.queryParamsToFilter(filterParam);

        assertThat(filters).isNotEmpty();
        assertThat(filters).extracting(Filter::getField).containsExactlyInAnyOrder("field1", "field2", "field3");
        assertThat(filters).extracting(Filter::getOperation).containsExactlyInAnyOrder(FilterOperation.EQUAL, FilterOperation.GREATER_THAN_OR_EQUAL_TO, FilterOperation.LESS_THAN);
        assertThat(filters).extracting(Filter::getValue).containsExactlyInAnyOrder("abc", "123", "2022-02-22");
    }

    @DisplayName("Filter with invalid length, must be pair")
    @Test
    void filtersWithEmpty() {
        String[] filterParam = {"field1.eq", "abc", "a", "field3.btn", "2022-02-22"};

        ApplicationException exception = assertThrows(ApplicationException.class, () -> QueryConverter.queryParamsToFilter(filterParam));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getStatusText()).isEqualTo("Filter is invalid. It must be a pair of FieldName.Operation and Value");
    }

    @DisplayName("Load Filters with invalid format throw and exception")
    @Test
    void filtersInvalidFormat() {
        String[] filterParam = {"field1.eq", "abc", "field2", "123", "field3.btn", "2022-02-22"};

        ApplicationException exception = assertThrows(ApplicationException.class, () -> QueryConverter.queryParamsToFilter(filterParam));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getStatusText()).isEqualTo("Field name field2 is invalid, must be FieldName.Operation");
    }

    @DisplayName("Load Filters with invalid operation throw and exception")
    @Test
    void filtersInvalidOperation() {
        String[] filterParam = {"field1.xyz", "abc", "field2.gte", "123", "field3.btn", "2022-02-22"};


        ApplicationException exception = assertThrows(ApplicationException.class, () -> QueryConverter.queryParamsToFilter(filterParam));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getStatusText()).isEqualTo("xyz operation is invalid");
    }
}