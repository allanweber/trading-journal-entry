package com.trading.journal.entry.entries;

import com.allanweber.jwttoken.helper.DateHelper;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;

@Document(collection = "entries")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
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
    private Double price;

    @NotNull(message = "Position size is required")
    @Positive(message = "Position size must be positive")
    private Double size;

    private Double profitPrice;

    private Double lossPrice;

    private ReturnRate plannedROR;

    private Double exitPrice;

    @JsonFormat(pattern = DateHelper.DATE_FORMAT)
    private LocalDateTime exitDate;

    private ReturnRate actualROR;

    private Double accountRisked;

    private Double grossResult;

    private Double costs;

    private Double netResult;

    private Double accountChange;

    private Double accountBalance;

    private String duration;

    private String screenshotBefore;

    private String screenshotAfter;

    private String notes;

    private Double amount;
}
