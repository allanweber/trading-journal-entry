package com.trading.journal.entry.entries.trade.aggregate;

import lombok.Getter;

@Getter
public class AggregateTrade {

    private String from;

    private String until;
    private AggregateType aggregateType;

    private final Long skip;

    private final Long size;

    public AggregateTrade(AggregateType aggregateType, Long page, Long size) {
        this.aggregateType = aggregateType;
        this.skip = page > 0 ? page * size : 0;
        this.size = size;
    }

    public AggregateTrade(Long skip, Long size) {
        this.skip = skip;
        this.size = size;
    }

    public AggregateTrade(Long page, Long size, String from, String until) {
        this(page, size);
        this.from = from;
        this.until = until;
    }
}
