package com.trading.journal.entry.entries;

import com.allanweber.jwttoken.helper.DateHelper;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
@EntryByType
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
    private BigDecimal price;

    private String symbol;

    private EntryDirection direction;

    private BigDecimal size;

    private BigDecimal profitPrice;

    private BigDecimal lossPrice;

    private BigDecimal costs;

    private BigDecimal exitPrice;

    @JsonFormat(pattern = DateHelper.DATE_FORMAT)
    private LocalDateTime exitDate;

    private String screenshotBefore;

    private String screenshotAfter;

    private String notes;

    /**
     * Calculated fields
     * Begin
     */

    @Setter
    private BigDecimal accountRisked;

    @Setter
    private BigDecimal plannedRR;

    @Setter
    private BigDecimal grossResult;

    @Setter
    private BigDecimal netResult;

    @Setter
    private BigDecimal accountChange;

    @Setter
    private BigDecimal accountBalance;

    /**
     * Calculated fields
     * End
     */

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
