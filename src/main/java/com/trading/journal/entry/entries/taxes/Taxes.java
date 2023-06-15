package com.trading.journal.entry.entries.taxes;

import com.allanweber.jwttoken.helper.DateHelper;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.format.annotation.NumberFormat;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Taxes {

    @NotNull(message = "Date is required")
    @JsonFormat(pattern = DateHelper.DATE_FORMAT)
    private LocalDateTime date;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @NumberFormat(pattern = "#0.00")
    private BigDecimal price;
}
