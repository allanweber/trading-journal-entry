package com.trading.journal.entry.entries.trade.impl;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.EntryRepository;
import com.trading.journal.entry.entries.EntryService;
import com.trading.journal.entry.entries.EntryType;
import com.trading.journal.entry.entries.trade.Symbol;
import com.trading.journal.entry.entries.trade.Trade;
import com.trading.journal.entry.entries.trade.TradeMapper;
import com.trading.journal.entry.entries.trade.TradeService;
import com.trading.journal.entry.journal.Journal;
import com.trading.journal.entry.journal.JournalService;
import com.trading.journal.entry.queries.CollectionName;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.BiFunction;

@RequiredArgsConstructor
@Service
public class TradeServiceImpl implements TradeService {

    private final EntryService entryService;

    private final EntryRepository repository;

    private final JournalService journalService;

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

    @Override
    public long countOpen(AccessTokenInfo accessToken, String journalId) {
        CollectionName entriesCollection = collectionName().apply(accessToken, journalId);
        Criteria criteria = new Criteria("type").is(EntryType.TRADE).and("netResult").exists(false);
        return repository.count(Query.query(criteria), entriesCollection);
    }

    @Override
    public List<Symbol> symbols(AccessTokenInfo accessToken, String journalId) {
        CollectionName entriesCollection = collectionName().apply(accessToken, journalId);
        return repository.distinct("symbol", entriesCollection).stream().map(Symbol::new).toList();
    }

    private BiFunction<AccessTokenInfo, String, CollectionName> collectionName() {
        return (accessTokenInfo, journalId) -> {
            Journal journal = journalService.get(accessTokenInfo, journalId);
            return new CollectionName(accessTokenInfo, journal.getName());
        };
    }
}
