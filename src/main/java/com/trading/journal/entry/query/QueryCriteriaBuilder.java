package com.trading.journal.entry.query;

import com.trading.journal.entry.ApplicationException;
import com.trading.journal.entry.query.data.Filter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class QueryCriteriaBuilder<T> {

    private final Map<String, Class<?>> fields;

    private static final Map<String, Function<Filter, Criteria>> FILTER_CRITERIA = new HashMap<>();

    static {
        FILTER_CRITERIA.put(FilterOperation.EQUAL.name(), condition -> Criteria.where(condition.field()).is(condition.value()));
        FILTER_CRITERIA.put(FilterOperation.NOT_EQUAL.name(), condition -> Criteria.where(condition.field()).ne(condition.value()));
        FILTER_CRITERIA.put(FilterOperation.GREATER_THAN.name(), condition -> Criteria.where(condition.field()).gt(condition.value()));
        FILTER_CRITERIA.put(FilterOperation.GREATER_THAN_OR_EQUAL_TO.name(), condition -> Criteria.where(condition.field()).gte(condition.value()));
        FILTER_CRITERIA.put(FilterOperation.LESS_THAN.name(), condition -> Criteria.where(condition.field()).lt(condition.value()));
        FILTER_CRITERIA.put(FilterOperation.LESS_THAN_OR_EQUAL_TO.name(), condition -> Criteria.where(condition.field()).lte(condition.value()));
        FILTER_CRITERIA.put(FilterOperation.BETWEEN.name(), condition -> {
            if (condition.field().contains("date")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime startDate = LocalDateTime.parse(condition.value().concat(" 00:00:00"), formatter);
                LocalDateTime endDate = LocalDateTime.parse(condition.value().concat(" 23:59:59"), formatter);
                return Criteria.where(condition.field()).gte(startDate).lt(endDate);
            } else {
                throw new IllegalArgumentException(String.format("Invalid between for type %s", condition.field()));
            }
        });
    }

    public QueryCriteriaBuilder(Class<T> clazz) {
        fields = Arrays.stream(clazz.getDeclaredFields())
                .collect(Collectors.toMap(java.lang.reflect.Field::getName, java.lang.reflect.Field::getType));
    }

    public Query buildQuery(List<Filter> filters) {
        Map<Filter, Class<?>> filterAndType = filters.stream()
                .collect(Collectors.toMap(filter -> filter, filter -> fields.get(filter.field())));

        Query query = new Query();
        if (!filters.isEmpty()) {
            List<Criteria> criteriaAndClause = new ArrayList<>();
            Criteria criteria = new Criteria();

            filters.forEach(condition -> {
                Function<Filter, Criteria> function = FILTER_CRITERIA.get(condition.operation().name());
                if (function == null) {
                    throw new ApplicationException(String.format("Invalid function param type: %s", condition.operation()));
                }
                criteriaAndClause.add(function.apply(condition));
            });

            if (!criteriaAndClause.isEmpty()) {
                query = Query.query(criteria.andOperator(criteriaAndClause.toArray(new Criteria[0])));
            }
        }
        return query;
    }
}
