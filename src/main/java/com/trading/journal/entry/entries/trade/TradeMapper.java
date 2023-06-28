package com.trading.journal.entry.entries.trade;

import com.trading.journal.entry.entries.Entry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TradeMapper {

    TradeMapper INSTANCE = Mappers.getMapper(TradeMapper.class);

    @Mapping(target = "type", expression = "java(com.trading.journal.entry.entries.EntryType.TRADE)")
    @Mapping(target = "journalId", source = "journalId")
    Entry toEntry(Trade trade, String journalId);

    @Mapping(target = "type", expression = "java(com.trading.journal.entry.entries.EntryType.TRADE)")
    @Mapping(target = "id", source = "entry.id")
    @Mapping(target = "exitPrice", source = "closeTrade.exitPrice")
    @Mapping(target = "exitDate", source = "closeTrade.exitDate")
    Entry toEntryFromClose(Entry entry, CloseTrade closeTrade);

    @Mapping(target = "id", source = "entry.id")
    @Mapping(target = "journalId", source = "entry.journalId")
    @Mapping(target = "type", expression = "java(com.trading.journal.entry.entries.EntryType.TRADE)")
    @Mapping(target = "date", source = "trade.date")
    @Mapping(target = "price", source = "trade.price")
    @Mapping(target = "graphType", source = "trade.graphType")
    @Mapping(target = "graphMeasure", source = "trade.graphMeasure")
    @Mapping(target = "symbol", source = "trade.symbol")
    @Mapping(target = "direction", source = "trade.direction")
    @Mapping(target = "size", source = "trade.size")
    @Mapping(target = "profitPrice", source = "trade.profitPrice")
    @Mapping(target = "lossPrice", source = "trade.lossPrice")
    @Mapping(target = "costs", source = "trade.costs")
    @Mapping(target = "notes", source = "trade.notes")
    @Mapping(target = "strategies", source = "trade.strategies")
    Entry toEditEntry(Entry entry, Trade trade);
}
