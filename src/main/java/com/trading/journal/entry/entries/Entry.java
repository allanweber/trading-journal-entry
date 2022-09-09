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

@Document(collection = "entries")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@EqualsAndHashCode
public class Entry {

    @Id
    private String id;

    @NotNull(message = "Date is required")
    @JsonFormat(pattern = DateHelper.DATE_FORMAT)
    private LocalDateTime date;

    @NotNull(message = "Entry type is required")
    private EntryType type;

    @NotNull(message = "Symbol is required")
    private String symbol;

    @NotNull(message = "Direction is required")
    private EntryDirection direction;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @NotNull(message = "Position size is required")
    @Positive(message = "Position size must be positive")
    private BigDecimal size;

    private BigDecimal profitPrice;

    private BigDecimal lossPrice;

    @Setter
    private BigDecimal accountRisked;

    @Setter
    private BigDecimal plannedRR;

    private BigDecimal exitPrice;

    @Setter
    private BigDecimal grossResult;

    private BigDecimal costs;

    @Setter
    private BigDecimal netResult;

    @Setter
    private BigDecimal accountChange;

    @Setter
    private BigDecimal accountBalance;

    @JsonFormat(pattern = DateHelper.DATE_FORMAT)
    private LocalDateTime exitDate;

    private String screenshotBefore;

    private String screenshotAfter;

    private String notes;
}
