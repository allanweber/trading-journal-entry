package com.trading.journal.entry.entries.trade;

import com.trading.journal.entry.entries.Entry;

import java.util.List;

public interface TradeService {
    Entry open(String journalId, Trade trade);

    Entry update(String tradeId, Trade trade);

    Entry close(String tradeId, CloseTrade trade);

    long countOpen(String journalId);

    List<Symbol> symbols(String journalId);
}
