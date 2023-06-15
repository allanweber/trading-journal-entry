package com.trading.journal.entry.journal;

import com.allanweber.jwttoken.helper.DateHelper;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.trading.journal.entry.balance.Balance;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.NumberFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "journals")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Journal {

    @Id
    private String id;

    @NotBlank(message = "Journal name is required")
    private String name;

    @JsonFormat(pattern = DateHelper.DATE_FORMAT)
    @NotNull(message = "Start journal date is required")
    private LocalDateTime startJournal;

    @NotNull(message = "Start balance is required")
    @NumberFormat(pattern = "#0.00")
    private BigDecimal startBalance;

    @NotNull(message = "Currency is required")
    private Currency currency;

    @Setter
    private Balance currentBalance;

    @JsonFormat(pattern = DateHelper.DATE_FORMAT)
    @Setter
    private LocalDateTime lastBalance;
}
