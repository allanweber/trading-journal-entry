package com.trading.journal.entry.entries;

import com.allanweber.jwttoken.helper.DateHelper;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.NumberFormat;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Document(collection = "entries")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Entry {

    @Id
    private String id;

    @NotNull(message = "Date is required")
    @JsonFormat(pattern = DateHelper.DATE_FORMAT)
    private LocalDateTime date;

    @NotNull(message = "Entry type is required")
    private EntryType type;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @NumberFormat(pattern = "#0.00")
    private BigDecimal price;

    private GraphType graphType;

    private String graphMeasure;

    private String symbol;

    private EntryDirection direction;

    @NumberFormat(pattern = "#0.00")
    @Positive(message = "Position size must be positive")
    private BigDecimal size;

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

    @Setter
    @JsonIgnore
    private String screenshotBefore;

    @Setter
    @JsonIgnore
    private String screenshotAfter;

    private String notes;

    /**
     * Calculated fields
     * Begin
     */

    @NumberFormat(pattern = "#0.0000")
    @Setter
    private BigDecimal accountRisked;

    @NumberFormat(pattern = "#0.00")
    @Setter
    private BigDecimal plannedRR;

    @NumberFormat(pattern = "#0.00")
    @Setter
    private BigDecimal grossResult;

    @NumberFormat(pattern = "#0.00")
    @Setter
    private BigDecimal netResult;

    @NumberFormat(pattern = "#0.0000")
    @Setter
    private BigDecimal accountChange;

    @NumberFormat(pattern = "#0.00")
    @Setter
    private BigDecimal accountBalance;

    /**
     * Calculated fields
     * End
     */

    @JsonIgnore
    public boolean isFinished() {
        return Objects.nonNull(netResult);
    }

    public void clearNonTrade() {
        this.symbol = null;
        this.direction = null;
        this.size = null;
        this.profitPrice = null;
        this.lossPrice = null;
        this.accountRisked = null;
        this.plannedRR = null;
        this.exitPrice = null;
        this.grossResult = null;
        this.costs = null;
    }
}
