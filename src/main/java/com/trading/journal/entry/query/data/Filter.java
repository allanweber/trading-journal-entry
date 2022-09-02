package com.trading.journal.entry.query.data;

import com.trading.journal.entry.query.FilterOperation;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record Filter(
        @NotBlank(message = "Filter name is required") String field,
        @NotNull(message = "Filed operation is required") FilterOperation operation,
        @NotBlank(message = "Filter value is required") String value) {
}
