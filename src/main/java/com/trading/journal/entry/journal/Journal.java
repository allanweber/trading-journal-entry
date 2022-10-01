package com.trading.journal.entry.journal;

import com.trading.journal.entry.balance.Balance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.NumberFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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

    @NotNull(message = "Start balance is required")
    @NumberFormat(pattern = "#0.00")
    private BigDecimal startBalance;

    private Balance currentBalance;

    private LocalDateTime lastBalance;
}
