package com.trading.journal.entry.journal;

import com.allanweber.jwttoken.helper.DateHelper;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.NumberFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class JournalData {
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

    public static JournalData fromJournal(Journal journal) {
        return JournalData.builder()
                .id(journal.getId())
                .name(journal.getName())
                .startJournal(journal.getStartJournal())
                .startBalance(journal.getStartBalance())
                .currency(journal.getCurrency())
                .build();
    }
}
