package com.trading.journal.entry.entries;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
public class CalculateEntry {

    private final Entry entry;

    private final BigDecimal balance;

    public Entry calculate() {

        //TEST OTHER TYPES
//        * When type is WITHDRAWAL, DEPOSIT, TAXES - clear all other values and keep only Date, Type, Price - use another endpoint to add those

        BigDecimal grossResult;
        if (EntryType.TRADE.equals(entry.getType())) {
            BigDecimal accountRisked = accountRisked();
            entry.setAccountRisked(accountRisked);

            BigDecimal plannedROR = calculatePlannedRR();
            entry.setPlannedRR(plannedROR);

            grossResult = grossResult();
            entry.setGrossResult(grossResult);
        } else {
            grossResult = entry.getPrice();
        }

        BigDecimal netResult = netResult(grossResult);
        entry.setNetResult(netResult);

        BigDecimal accountChanged = accountChanged(netResult);
        entry.setAccountChange(accountChanged);

        BigDecimal accountBalance = accountBalance(netResult);
        entry.setAccountBalance(accountBalance);

        return entry;
    }

    /**
     * Long: ((Price - Loss) * Size) / Balance
     * Long: ((Loss - Price) * Size) / Balance
     * If Account Risked < 0 than = (Account Risked) * -1
     */
    private BigDecimal accountRisked() {
        BigDecimal accountRisked = null;
        if (balance.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal loss = ofNullable(entry.getLossPrice()).orElse(BigDecimal.ZERO);
            if (EntryDirection.LONG.equals(entry.getDirection())) {
                accountRisked = entry.getPrice().subtract(loss).multiply(entry.getSize()).divide(balance, 4, RoundingMode.HALF_EVEN);
            } else {
                accountRisked = loss.subtract(entry.getPrice()).multiply(entry.getSize()).divide(balance, 4, RoundingMode.HALF_EVEN);
            }

            if (accountRisked.compareTo(BigDecimal.ZERO) < 0) {
                accountRisked = accountRisked.multiply(BigDecimal.valueOf(-1));
            }
        }
        return accountRisked;
    }

    /**
     * Risk:
     * accountRisked / accountRisked
     * Reward:
     * Long: (((Profit - Price) * Size) / ((Price - Loss) * Size)
     * Long: (((Price - Profit) * Size) / ((Loss - Price))* Size)
     */
    private BigDecimal calculatePlannedRR() {
        BigDecimal profit = ofNullable(entry.getProfitPrice()).orElse(BigDecimal.ZERO);
        BigDecimal loss = ofNullable(entry.getLossPrice()).orElse(BigDecimal.ZERO);
        BigDecimal reward;
        BigDecimal risk;
        if (EntryDirection.LONG.equals(entry.getDirection())) {
            reward = profit.subtract(entry.getPrice());
            risk = entry.getPrice().subtract(loss);
        } else {
            reward = entry.getPrice().subtract(profit);
            risk = loss.subtract(entry.getPrice());
        }
        reward = reward.multiply(entry.getSize());
        risk = risk.multiply(entry.getSize());
        return reward.divide(risk, 2, RoundingMode.HALF_EVEN);
    }

    /**
     * Long: (Exit Price - Entry Price) * Size
     * Short: (Entry Price - Exit Price) * Size
     */
    private BigDecimal grossResult() {
        BigDecimal grossResult = null;
        if (Objects.nonNull(entry.getExitPrice())) {
            if (EntryDirection.LONG.equals(entry.getDirection())) {
                grossResult = entry.getExitPrice().subtract(entry.getPrice());
            } else {
                grossResult = entry.getPrice().subtract(entry.getExitPrice());
            }
            grossResult = grossResult.multiply(entry.getSize()).setScale(2, RoundingMode.HALF_EVEN);
        }
        return grossResult;
    }

    /**
     * Wining Trade = Gross Result - Costs
     * Losing Trade = Gross Result + Costs
     */
    private BigDecimal netResult(BigDecimal grossResult) {
        BigDecimal netResult = null;
        if (Objects.nonNull(grossResult)) {
            BigDecimal costs = ofNullable(entry.getCosts()).orElse(BigDecimal.ZERO);
            netResult = grossResult.subtract(costs).setScale(2, RoundingMode.HALF_EVEN);
        }
        return netResult;
    }

    /**
     * Net Result / Balance
     */
    private BigDecimal accountChanged(BigDecimal netResult) {
        BigDecimal accountChanged = null;
        if (Objects.nonNull(netResult)) {
            if (balance.compareTo(BigDecimal.ZERO) == 0) {
                accountChanged = BigDecimal.ONE.setScale(4, RoundingMode.HALF_EVEN);
            } else {
                accountChanged = netResult.divide(balance, 4, RoundingMode.HALF_EVEN);
                if (accountChanged.compareTo(BigDecimal.ZERO) > 0 && netResult.compareTo(BigDecimal.ZERO) < 0) {
                    accountChanged = accountChanged.multiply(BigDecimal.valueOf(-1));
                }
            }
        }
        return accountChanged;
    }

    /**
     * Balance + Net Result
     */
    private BigDecimal accountBalance(BigDecimal netResult) {
        BigDecimal accountBalance = null;
        if (Objects.nonNull(netResult)) {
            accountBalance = balance.add(netResult);
        }
        return accountBalance;
    }
}
