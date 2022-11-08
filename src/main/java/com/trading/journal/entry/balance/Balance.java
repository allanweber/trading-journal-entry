package com.trading.journal.entry.balance;

import lombok.*;
import org.springframework.format.annotation.NumberFormat;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Balance {

    @NumberFormat(pattern = "#0.00")
    @Setter
    private BigDecimal startBalance;

    @NumberFormat(pattern = "#0.00")
    private BigDecimal accountBalance;

    @NumberFormat(pattern = "#0.00")
    private BigDecimal closedPositions;

    @NumberFormat(pattern = "#0.00")
    private BigDecimal deposits;

    @NumberFormat(pattern = "#0.00")
    private BigDecimal taxes;

    @NumberFormat(pattern = "#0.00")
    private BigDecimal withdrawals;
}
