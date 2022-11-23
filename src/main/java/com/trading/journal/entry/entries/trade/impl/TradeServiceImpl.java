package com.trading.journal.entry.entries.trade.impl;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.EntryService;
import com.trading.journal.entry.entries.trade.Trade;
import com.trading.journal.entry.entries.trade.TradeMapper;
import com.trading.journal.entry.entries.trade.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TradeServiceImpl implements TradeService {

    private final EntryService entryService;

    @Override
    public Entry create(AccessTokenInfo accessTokenInfo, String journalId, Trade trade) {
        Entry entry = TradeMapper.INSTANCE.toEntry(trade);
        return entryService.save(accessTokenInfo, journalId, entry);
    }

    @Override
    public Entry update(AccessTokenInfo accessTokenInfo, String journalId, String tradeId, Trade trade) {
        Entry entry = TradeMapper.INSTANCE.toEntry(trade, tradeId);
        return entryService.save(accessTokenInfo, journalId, entry);
    }
}
