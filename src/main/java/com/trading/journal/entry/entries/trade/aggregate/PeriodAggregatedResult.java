package com.trading.journal.entry.entries.trade.aggregate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PeriodAggregatedResult {
    private List<PeriodAggregated> items;

    private Long total;
}
