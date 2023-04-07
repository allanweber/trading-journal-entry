package com.trading.journal.entry.entries.trade.impl;

import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.EntryRepository;
import com.trading.journal.entry.entries.EntryService;
import com.trading.journal.entry.entries.EntryType;
import com.trading.journal.entry.entries.trade.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class TradeServiceImpl implements TradeService {

    private final EntryService entryService;

    private final EntryRepository repository;


    @Override
    public Entry open(String journalId, Trade trade) {
        Entry entry = TradeMapper.INSTANCE.toEntry(trade, journalId);
        return entryService.save(entry);
    }

    @Override
    public Entry update(String journalId, String tradeId, Trade trade) {
        Entry entry = TradeMapper.INSTANCE.toEditEntry(trade, journalId, tradeId);
        return entryService.save(entry);
    }

    @Override
    public Entry close(String journalId, String tradeId, CloseTrade trade) {
        Entry entry = entryService.getById(tradeId);
        Entry closeTrade = TradeMapper.INSTANCE.toEntryFromClose(entry, trade);
        return entryService.save(closeTrade);
    }

    @Override
    public long countOpen(String journalId) {
        Criteria criteria = new Criteria("journalId").is(journalId)
                .and("type").is(EntryType.TRADE)
                .and("netResult").exists(false);
        return repository.count(Query.query(criteria));
    }

    @Override
    public List<Symbol> symbols(String journalId) {
        Query query = new Query(new Criteria("journalId").is(journalId));
        return repository.distinct("symbol", query).stream().map(Symbol::new).toList();
    }
}
