package com.trading.journal.entry.entries.trade;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.trade.aggregate.AggregateTrade;
import com.trading.journal.entry.entries.trade.aggregate.AggregatedResult;

import java.util.List;

public interface TradeService {
    Entry open(AccessTokenInfo accessTokenInfo, String journalId, Trade trade);

    Entry update(AccessTokenInfo accessTokenInfo, String journalId, String tradeId, Trade trade);

    Entry close(AccessTokenInfo accessTokenInfo, String journalId, String tradeId, CloseTrade trade);

    long countOpen(AccessTokenInfo accessToken, String journalId);

    List<Symbol> symbols(AccessTokenInfo accessToken, String journalId);

    AggregatedResult aggregate(AccessTokenInfo accessToken, String journalId, AggregateTrade aggregateTrade);
}
