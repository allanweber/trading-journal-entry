package com.trading.journal.entry.entries.trade.impl;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.mongodb.BasicDBObject;
import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.EntryRepository;
import com.trading.journal.entry.entries.EntryService;
import com.trading.journal.entry.entries.EntryType;
import com.trading.journal.entry.entries.trade.*;
import com.trading.journal.entry.entries.trade.aggregate.AggregateTrade;
import com.trading.journal.entry.entries.trade.aggregate.AggregatedResult;
import com.trading.journal.entry.entries.trade.aggregate.AggregatedTrades;
import com.trading.journal.entry.journal.Journal;
import com.trading.journal.entry.journal.JournalService;
import com.trading.journal.entry.queries.CollectionName;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
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
    public Entry open(AccessTokenInfo accessTokenInfo, String journalId, Trade trade) {
        Entry entry = TradeMapper.INSTANCE.toEntry(trade);
        return entryService.save(accessTokenInfo, journalId, entry);
    }

    @Override
    public Entry update(AccessTokenInfo accessTokenInfo, String journalId, String tradeId, Trade trade) {
        Entry entry = TradeMapper.INSTANCE.toEntry(trade, tradeId);
        return entryService.save(accessTokenInfo, journalId, entry);
    }

    @Override
    public Entry close(AccessTokenInfo accessTokenInfo, String journalId, String tradeId, CloseTrade trade) {
        Entry entry = entryService.getById(accessTokenInfo, journalId, tradeId);
        Entry closeTrade = TradeMapper.INSTANCE.toEntryFromClose(entry, trade);
        return entryService.save(accessTokenInfo, journalId, closeTrade);
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

    @Override
    public AggregatedResult aggregate(AccessTokenInfo accessToken, String journalId, AggregateTrade aggregateTrade) {
        CollectionName entriesCollection = collectionName().apply(accessToken, journalId);

        String query = "{ _id: { $dateToString: { format: '" + aggregateTrade.getAggregateType().getGroupBy() + "', date: '$date'} }, " +
                "items: { $push: {'tradeId':{ $convert: { input: '$_id', to: 'string' } }, " +
                "'symbol' : '$symbol', " +
                "order: { $dateToString: { format: '" + aggregateTrade.getAggregateType().getOrderBy() + "', date: '$date'} }, " +
                "'date':'$date', " +
                "'exitDate':'$exitDate', " +
                "'result':'$netResult'} } }";

        AggregationOperation group = aggregationOperationContext -> new Document("$group", BasicDBObject.parse(query));

        Aggregation aggregation = Aggregation.newAggregation(
                group,
                Aggregation.unwind("$items"),
                Aggregation.sort(Sort.Direction.DESC, "items.order"),
                Aggregation.group("$_id").push("$items").as("items").count().as("count"),
                Aggregation.sort(Sort.Direction.DESC, "_id"),
                Aggregation.project().andExclude("_id").and("$_id").as("group").andInclude("items", "count")
        );

        List<AggregatedTrades> items = repository.aggregate(aggregation, entriesCollection, AggregatedTrades.class);
        return new AggregatedResult(items, aggregateTrade.getAggregateType());
    }

    private BiFunction<AccessTokenInfo, String, CollectionName> collectionName() {
        return (accessTokenInfo, journalId) -> {
            Journal journal = journalService.get(accessTokenInfo, journalId);
            return new CollectionName(accessTokenInfo, journal.getName());
        };
    }
}
