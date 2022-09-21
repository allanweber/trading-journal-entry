package com.trading.journal.entry.balance.impl;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.balance.BalanceService;
import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.EntryRepository;
import com.trading.journal.entry.journal.Journal;
import com.trading.journal.entry.journal.JournalService;
import com.trading.journal.entry.queries.CollectionName;
import com.trading.journal.entry.queries.QueryCriteriaBuilder;
import com.trading.journal.entry.queries.data.Filter;
import com.trading.journal.entry.queries.data.FilterOperation;
import com.trading.journal.entry.queries.data.PageableRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;

@RequiredArgsConstructor
@Service
public class BalanceServiceImpl implements BalanceService {

    private final EntryRepository entryRepository;

    private final JournalService journalService;

    @Override
    public BigDecimal getCurrentBalance(AccessTokenInfo accessToken, String journalId) {
        return calculateBalance(accessToken, journalId, LocalDateTime.now());
    }

    @Override
    public BigDecimal getCurrentBalance(AccessTokenInfo accessToken, String journalId, LocalDateTime date) {
        return calculateBalance(accessToken, journalId, date);
    }

//    @Override
//    public void balanceForward(AccessTokenInfo accessToken, String journalId, LocalDateTime date) {
//        BigDecimal balance = calculateBalance(accessToken, journalId, date);
//    }

    private BigDecimal calculateBalance(AccessTokenInfo accessToken, String journalId, LocalDateTime date) {
        Journal journal = journalService.get(accessToken, journalId);
        CollectionName collectionName = new CollectionName(accessToken, journal.getName());

        PageableRequest pageableRequest = PageableRequest.builder()
                .page(0)
                .size(Integer.MAX_VALUE)
                .sort(Sort.by("date").ascending())
                .filters(singletonList(Filter.builder().field("date").operation(FilterOperation.LESS_THAN_OR_EQUAL_TO).value(date.format(QueryCriteriaBuilder.DATE_FORMATTER)).build()))
                .build();

        Stream<Entry> entries = entryRepository.findAll(collectionName, pageableRequest).get();

        BigDecimal netResult = entries.map(Entry::getNetResult)
                .filter(Objects::nonNull)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);

        return netResult.add(journal.getStartBalance());
    }
}
