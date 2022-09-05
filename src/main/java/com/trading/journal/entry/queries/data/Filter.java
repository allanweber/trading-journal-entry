package com.trading.journal.entry.queries.data;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@EqualsAndHashCode
public class Filter {
    @NotBlank(message = "Filter name is required")
    private String field;

    @NotNull(message = "Filed operation is required")
    private FilterOperation operation;

    @NotBlank(message = "Filter value is required")
    private String value;
}
