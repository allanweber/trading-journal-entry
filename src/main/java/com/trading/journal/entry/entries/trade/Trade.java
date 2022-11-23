package com.trading.journal.entry.entries.trade;

import com.allanweber.jwttoken.helper.DateHelper;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.trading.journal.entry.entries.EntryDirection;
import com.trading.journal.entry.entries.GraphType;
import lombok.*;
import org.springframework.format.annotation.NumberFormat;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
@ValidTrade
public class Trade {

    @NotNull(message = "Date is required")
    @JsonFormat(pattern = DateHelper.DATE_FORMAT)
    private LocalDateTime date;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @NumberFormat(pattern = "#0.00")
    private BigDecimal price;

    @NotNull(message = "Symbol is required")
    private String symbol;

    @NotNull(message = "Direction is required")
    private EntryDirection direction;

    @NotNull(message = "Position size is required")
    @NumberFormat(pattern = "#0.00")
    @Positive(message = "Position size must be positive")
    private BigDecimal size;

    private GraphType graphType;

    private String graphMeasure;

    @NumberFormat(pattern = "#0.00")
    private BigDecimal profitPrice;

    @NumberFormat(pattern = "#0.00")
    private BigDecimal lossPrice;

    @NumberFormat(pattern = "#0.00")
    private BigDecimal costs;

    @NumberFormat(pattern = "#0.00")
    private BigDecimal exitPrice;

    @JsonFormat(pattern = DateHelper.DATE_FORMAT)
    private LocalDateTime exitDate;

    private String notes;
}
