package com.trading.journal.entry.balance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.NumberFormat;

import java.math.BigDecimal;

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
}
