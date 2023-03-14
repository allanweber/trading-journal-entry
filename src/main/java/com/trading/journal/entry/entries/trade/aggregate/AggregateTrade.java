package com.trading.journal.entry.entries.trade.aggregate;

import lombok.Getter;

@Getter
public class AggregateTrade {

    private String from;

    private String until;
    private AggregateType aggregateType;

    private Long skip;

    private Long size;

    public AggregateTrade(AggregateType aggregateType, Long page, Long size) {
        this.aggregateType = aggregateType;
        this.skip = page > 0 ? page * size : 0;
        this.size = size;
    }

    public AggregateTrade(String from, String until) {
        this.from = from;
        this.until = until;
    }
}
