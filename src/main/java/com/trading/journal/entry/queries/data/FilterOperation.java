package com.trading.journal.entry.queries.data;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum FilterOperation {

    EQUAL("eq", "Equal"),
    NOT_EQUAL("ne", "Not equal"),
    GREATER_THAN("gt", "Greater than"),
    GREATER_THAN_OR_EQUAL_TO("gte", "Greater than or equal"),
    LESS_THAN("lt", "Less than"),
    LESS_THAN_OR_EQUAL_TO("lte", "Less than or equal");

    private final String operation;

    private final String description;

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(operation);
    }

    public static FilterOperation fromValue(String value) {
        FilterOperation operation = null;
        for (FilterOperation op : FilterOperation.values()) {
            if (String.valueOf(op.operation).equalsIgnoreCase(value)) {
                operation = op;
            }
        }
        return operation;
    }

}
