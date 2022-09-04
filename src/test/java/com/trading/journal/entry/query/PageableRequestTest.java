package com.trading.journal.entry.query;

import com.trading.journal.entry.ApplicationException;
import com.trading.journal.entry.query.data.Filter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

class PageableRequestTest {

    @DisplayName("Load pageable request with sort")
    @Test
    void pageable() {
        String[] sort = new String[]{"name", "asc", "id", "desc", "age", "asc"};
        PageableRequest pageableRequest = PageableRequest.builder().page(1).size(20).sort(sort).build();

        Pageable pageable = pageableRequest.pageable();

        assertThat(pageableRequest.getFilters()).isEmpty();
        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(20);
        assertThat(pageable.getSort().get()).hasSize(3);
        assertThat(Objects.requireNonNull(pageable.getSort().getOrderFor("name")).getDirection()).isEqualTo(Sort.Direction.ASC);
        assertThat(Objects.requireNonNull(pageable.getSort().getOrderFor("id")).getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(Objects.requireNonNull(pageable.getSort().getOrderFor("age")).getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @DisplayName("Load pageable request without sort")
    @Test
    void pageableNoSort() {
        PageableRequest pageableRequest = PageableRequest.builder().page(1).size(20).build();

        Pageable pageable = pageableRequest.pageable();

        assertThat(pageableRequest.getFilters()).isEmpty();
        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(20);
        assertThat(pageable.getSort().get()).hasSize(1);
        assertThat(Objects.requireNonNull(pageable.getSort().getOrderFor("id")).getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @DisplayName("Load pageable request even with extra spaces in sort")
    @Test
    void pageableSort() {
        String[] sort = new String[]{" name", "  asc", "id  ", "desc  ", "age ", " asc"};
        PageableRequest pageableRequest = PageableRequest.builder().page(1).size(20).sort(sort).build();

        Pageable pageable = pageableRequest.pageable();

        assertThat(pageableRequest.getFilters()).isEmpty();
        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(20);
        assertThat(pageable.getSort().get()).hasSize(3);
        assertThat(Objects.requireNonNull(pageable.getSort().getOrderFor("name")).getDirection()).isEqualTo(Sort.Direction.ASC);
        assertThat(Objects.requireNonNull(pageable.getSort().getOrderFor("id")).getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(Objects.requireNonNull(pageable.getSort().getOrderFor("age")).getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @DisplayName("Load pageable throws exception when sort array is not odd")
    @Test
    void pageableSortException() {
        String[] sort = new String[]{"name", "desc", "age"};
        PageableRequest pageableRequest = PageableRequest.builder().page(1).size(20).sort(sort).build();

        ApplicationException exception = assertThrows(ApplicationException.class, pageableRequest::pageable);
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getStatusText()).isEqualTo("Sort is invalid. It must be a pair of column and direction");
    }

    @DisplayName("Load pageable throws exception when direction is not valid (asc or desc)")
    @Test
    void pageableDirectionException() {
        String[] sort = new String[]{"name", "asc", "id", "abc", "age", "asc"};
        PageableRequest pageableRequest = PageableRequest.builder().page(1).size(20).sort(sort).build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, pageableRequest::pageable);
        assertThat(exception.getMessage()).contains("Invalid value 'abc' for orders given; Has to be either 'desc' or 'asc'");
    }

    @DisplayName("Load Filters")
    @Test
    void filters() {
        String[] filter = {"field1.eq", "abc", "field2.gte", "123", "field3.lt", "2022-02-22"};
        PageableRequest pageableRequest = PageableRequest.builder().filter(filter).build();

        List<Filter> filters = pageableRequest.getFilters();

        assertThat(filters).isNotEmpty();
        assertThat(filters).extracting(Filter::getField).containsExactlyInAnyOrder("field1", "field2", "field3");
        assertThat(filters).extracting(Filter::getOperation).containsExactlyInAnyOrder(FilterOperation.EQUAL, FilterOperation.GREATER_THAN_OR_EQUAL_TO, FilterOperation.LESS_THAN);
        assertThat(filters).extracting(Filter::getValue).containsExactlyInAnyOrder("abc", "123", "2022-02-22");
    }

    @DisplayName("Filter with invalid length, must be pair")
    @Test
    void filtersWithEmpty() {
        String[] filter = {"field1.eq", "abc", "a", "field3.btn", "2022-02-22"};
        PageableRequest pageableRequest = PageableRequest.builder().filter(filter).build();

        ApplicationException exception = assertThrows(ApplicationException.class, pageableRequest::getFilters);
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getStatusText()).isEqualTo("Filter is invalid. It must be a pair of FieldName.Operation and Value");
    }

    @DisplayName("Load Filters with invalid format throw and exception")
    @Test
    void filtersInvalidFormat() {
        String[] filter = {"field1.eq", "abc", "field2", "123", "field3.btn", "2022-02-22"};
        PageableRequest pageableRequest = PageableRequest.builder().filter(filter).build();

        ApplicationException exception = assertThrows(ApplicationException.class, pageableRequest::getFilters);
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getStatusText()).isEqualTo("Field name field2 is invalid, must be FieldName.Operation");
    }

    @DisplayName("Load Filters with invalid operation throw and exception")
    @Test
    void filtersInvalidOperation() {
        String[] filter = {"field1.xyz", "abc", "field2.gte", "123", "field3.btn", "2022-02-22"};
        PageableRequest pageableRequest = PageableRequest.builder().filter(filter).build();

        ApplicationException exception = assertThrows(ApplicationException.class, pageableRequest::getFilters);
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getStatusText()).isEqualTo("xyz operation is invalid");
    }
}