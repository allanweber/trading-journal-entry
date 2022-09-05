package com.trading.journal.entry.queries;

import com.trading.journal.entry.queries.data.Filter;
import com.trading.journal.entry.queries.data.FilterOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

public class QueryCriteriaBuilder<T> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final Map<String, Class<?>> fields;

    private static final Map<String, BiFunction<Filter, Class<?>, Criteria>> FILTER_CRITERIA = new ConcurrentHashMap<>();

    static {
        FILTER_CRITERIA.put(FilterOperation.EQUAL.name(), (filter, filedType) -> {
            if (filedType.getSimpleName().toLowerCase(Locale.getDefault()).contains("localdate")) {
                LocalDateTime startDate = LocalDateTime.parse(filter.getValue().concat(" 00:00:00"), DATE_FORMATTER);
                LocalDateTime endDate = LocalDateTime.parse(filter.getValue().concat(" 23:59:59"), DATE_FORMATTER);
                return Criteria.where(filter.getField()).gte(startDate).lte(endDate);
            }
            return Criteria.where(filter.getField()).is(convertValueToType(filter, filedType));
        });
        FILTER_CRITERIA.put(FilterOperation.NOT_EQUAL.name(), (filter, filedType) -> Criteria.where(filter.getField()).ne(convertValueToType(filter, filedType)));
        FILTER_CRITERIA.put(FilterOperation.GREATER_THAN.name(), (filter, filedType) -> Criteria.where(filter.getField()).gt(convertValueToType(filter, filedType)));
        FILTER_CRITERIA.put(FilterOperation.GREATER_THAN_OR_EQUAL_TO.name(), (filter, filedType) -> Criteria.where(filter.getField()).gte(convertValueToType(filter, filedType)));
        FILTER_CRITERIA.put(FilterOperation.LESS_THAN.name(), (filter, filedType) -> Criteria.where(filter.getField()).lt(convertValueToType(filter, filedType)));
        FILTER_CRITERIA.put(FilterOperation.LESS_THAN_OR_EQUAL_TO.name(), (filter, filedType) -> Criteria.where(filter.getField()).lte(convertValueToType(filter, filedType)));
    }

    public QueryCriteriaBuilder(Class<T> clazz) {
        fields = Arrays.stream(clazz.getDeclaredFields())
                .collect(Collectors.toMap(java.lang.reflect.Field::getName, java.lang.reflect.Field::getType));
    }

    public Query buildQuery(List<Filter> filters) {
        Map<Filter, Class<?>> filterAndType = ofNullable(filters)
                .orElse(emptyList())
                .parallelStream()
                .collect(Collectors.toMap(filter -> filter, filter -> fields.get(filter.getField())));

        Query query = new Query();
        if (!filterAndType.isEmpty()) {
            List<Criteria> criteriaAndClause = new ArrayList<>();
            Criteria criteria = new Criteria();

            filterAndType.forEach((filter, filedType) -> {
                BiFunction<Filter, Class<?>, Criteria> function = FILTER_CRITERIA.get(filter.getOperation().name());
                criteriaAndClause.add(function.apply(filter, filedType));
            });

            if (!criteriaAndClause.isEmpty()) {
                query = Query.query(criteria.andOperator(criteriaAndClause.toArray(new Criteria[0])));
            }
        }
        return query;
    }

    private static Object convertValueToType(Filter filter, Class<?> filedType) {
        String fieldTypeName = filedType.getSimpleName().toLowerCase(Locale.getDefault());
        Object value;
        if (fieldTypeName.contains("double")) {
            value = Double.parseDouble(filter.getValue());
        } else if (fieldTypeName.contains("int")) {
            value = Integer.parseInt(filter.getValue());
        } else if (fieldTypeName.contains("bigdecimal")) {
            value = new BigDecimal(filter.getValue());
        } else if (fieldTypeName.contains("localdate")) {
            value = switch (filter.getOperation()) {
                case GREATER_THAN, GREATER_THAN_OR_EQUAL_TO ->
                        LocalDateTime.parse(filter.getValue().concat(" 00:00:00"), DATE_FORMATTER);
                case LESS_THAN, LESS_THAN_OR_EQUAL_TO ->
                        LocalDateTime.parse(filter.getValue().concat(" 23:59:59"), DATE_FORMATTER);
                default -> LocalDateTime.parse(filter.getValue(), DATE_FORMATTER);
            };
        } else {
            value = filter.getValue();
        }
        return value;
    }
}
