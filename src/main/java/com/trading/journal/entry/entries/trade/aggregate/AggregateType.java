package com.trading.journal.entry.entries.trade.aggregate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AggregateType {
    DAY("%Y-%m-%d", "%Y-%m-%d %H:%M:%S"),
    WEEK("%Y-%U", "%Y-%m-%d"),
    MONTH("%Y-%m", "%Y-%m-%d");

    private final String groupBy;

    private final String orderBy;
}
