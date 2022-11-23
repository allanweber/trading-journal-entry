package com.trading.journal.entry.entries.withdrawal;

import com.trading.journal.entry.entries.Entry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WithdrawalMapper {

    WithdrawalMapper INSTANCE = Mappers.getMapper(WithdrawalMapper.class);

    @Mapping(target = "type", expression = "java(com.trading.journal.entry.entries.EntryType.WITHDRAWAL)")
    Entry toEntry(Withdrawal withdrawal);
}
