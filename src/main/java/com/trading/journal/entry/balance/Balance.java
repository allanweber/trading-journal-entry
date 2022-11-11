package com.trading.journal.entry.balance;

import com.allanweber.jwttoken.helper.DateHelper;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.trading.journal.entry.journal.Currency;
import lombok.*;
import org.springframework.format.annotation.NumberFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Balance {

    @NumberFormat(pattern = "#0.00")
    private BigDecimal accountBalance;

    @NumberFormat(pattern = "#0.00")
    private BigDecimal closedPositions;

    @NumberFormat(pattern = "#0.00")
    private BigDecimal openedPositions;

    @NumberFormat(pattern = "#0.00")
    private BigDecimal available;

    @NumberFormat(pattern = "#0.00")
    private BigDecimal deposits;

    @NumberFormat(pattern = "#0.00")
    private BigDecimal taxes;

    @NumberFormat(pattern = "#0.00")
    private BigDecimal withdrawals;

    @NumberFormat(pattern = "#0.00")
    @Setter
    private BigDecimal startBalance;

    @Setter
    private Currency currency;

    @JsonFormat(pattern = DateHelper.DATE_FORMAT)
    @Setter
    private LocalDateTime startJournal;
}
