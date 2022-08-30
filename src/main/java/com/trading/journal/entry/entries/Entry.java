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

@Document(collection = "hello")
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

    @NotNull(message = "Symbol is required")
    private String symbol;

    @NotNull(message = "Direction is required")
    private EntryDirection direction;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;
}
