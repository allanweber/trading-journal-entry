package com.trading.journal.entry.entries.trade;

import com.trading.journal.entry.entries.Entry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TradeMapper {

    TradeMapper INSTANCE = Mappers.getMapper(TradeMapper.class);

    @Mapping(target = "type",  expression = "java(com.trading.journal.entry.entries.EntryType.TRADE)")
    Entry toEntry(Trade trade);

    @Mapping(target = "type",  expression = "java(com.trading.journal.entry.entries.EntryType.TRADE)")
    @Mapping(target = "id", source = "id")
    Entry toEntry(Trade trade, String id);
}
