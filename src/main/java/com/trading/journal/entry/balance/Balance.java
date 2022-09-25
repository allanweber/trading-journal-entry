package com.trading.journal.entry.balance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Balance {

    private BigDecimal accountBalance;

    private BigDecimal closedPositions;

    private BigDecimal deposits;

    private BigDecimal taxes;

    private BigDecimal withdrawals;
}
