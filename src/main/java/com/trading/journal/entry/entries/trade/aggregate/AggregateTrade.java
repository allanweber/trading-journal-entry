package com.trading.journal.entry.entries.trade.aggregate;

public class AggregateTrade {
    private final AggregateType aggregateType;

    private final Long skip;

    private final Long size;

    public AggregateTrade(AggregateType aggregateType, Long page, Long size) {
        this.aggregateType = aggregateType;
        this.skip = page > 0 ? page * size : 0;
        this.size = size;
    }

    public AggregateType getAggregateType() {
        return aggregateType;
    }

    public Long getSkip() {
        return skip;
    }

    public Long getSize() {
        return size;
    }
}
