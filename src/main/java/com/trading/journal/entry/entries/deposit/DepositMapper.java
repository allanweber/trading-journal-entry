package com.trading.journal.entry.entries.deposit;

import com.trading.journal.entry.entries.Entry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DepositMapper {

    DepositMapper INSTANCE = Mappers.getMapper(DepositMapper.class);

    @Mapping(target = "type", expression = "java(com.trading.journal.entry.entries.EntryType.DEPOSIT)")
    Entry toEntry(Deposit deposit);
}
