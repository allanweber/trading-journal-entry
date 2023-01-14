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
public class AggregatedTrades {
    private String group;

    private Long count;

    private List<AggregatedItems> items;
}
