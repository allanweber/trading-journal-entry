package com.trading.journal.entry.entries.trade;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.entries.Entry;

import java.util.List;

public interface TradeService {
    Entry create(AccessTokenInfo accessTokenInfo, String journalId, Trade trade);

    Entry update(AccessTokenInfo accessTokenInfo, String journalId, String tradeId, Trade trade);

    long countOpen(AccessTokenInfo accessToken, String journalId);

    List<Symbol> symbols(AccessTokenInfo accessToken, String journalId);
}
