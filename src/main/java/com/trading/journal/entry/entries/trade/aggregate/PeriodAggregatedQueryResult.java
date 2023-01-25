package com.trading.journal.entry.entries.trade.aggregate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class PeriodAggregatedQueryResult {
    private List<PeriodItems> result;

    private Long total;
}
