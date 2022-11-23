package com.trading.journal.entry.entries.taxes;

import com.trading.journal.entry.entries.Entry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaxesMapper {

    TaxesMapper INSTANCE = Mappers.getMapper(TaxesMapper.class);

    @Mapping(target = "type", expression = "java(com.trading.journal.entry.entries.EntryType.TAXES)")
    Entry toEntry(Taxes taxes);
}
