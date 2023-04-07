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

    @Mapping(target = "type", expression = "java(com.trading.journal.entry.entries.EntryType.TRADE)")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "journalId", source = "journalId")
    Entry toEditEntry(Trade trade, String journalId, String id);
}
