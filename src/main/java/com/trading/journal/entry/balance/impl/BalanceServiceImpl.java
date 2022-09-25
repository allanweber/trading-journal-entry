package com.trading.journal.entry.balance.impl;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.balance.Balance;
import com.trading.journal.entry.balance.BalanceService;
import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.EntryRepository;
import com.trading.journal.entry.entries.EntryType;
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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
@Service
public class BalanceServiceImpl implements BalanceService {

    private static final String DATE = "date";

    private final EntryRepository entryRepository;

    private final JournalService journalService;

    @Override
    public Balance getCurrentBalance(AccessTokenInfo accessToken, String journalId) {
        return calculateBalance(accessToken, journalId, LocalDateTime.now());
    }

    @Override
    public Balance getCurrentBalance(AccessTokenInfo accessToken, String journalId, LocalDateTime date) {
        return calculateBalance(accessToken, journalId, date);
    }

    private Balance calculateBalance(AccessTokenInfo accessToken, String journalId, LocalDateTime date) {
        Journal journal = journalService.get(accessToken, journalId);
        CollectionName collectionName = new CollectionName(accessToken, journal.getName());

        PageableRequest pageableRequest = PageableRequest.builder()
                .page(0)
                .size(Integer.MAX_VALUE)
                .sort(Sort.by(DATE).ascending())
                .filters(singletonList(Filter.builder().field(DATE).operation(FilterOperation.LESS_THAN_OR_EQUAL_TO).value(date.format(QueryCriteriaBuilder.DATE_FORMATTER)).build()))
                .build();

        List<Entry> entries = entryRepository.findAll(collectionName, pageableRequest).get()
                .filter(entry -> Objects.nonNull(entry.getNetResult())).toList();

        BigDecimal closedPositions = BigDecimal.ZERO;
        BigDecimal deposits = BigDecimal.ZERO;
        BigDecimal withdrawals = BigDecimal.ZERO;
        BigDecimal taxes = BigDecimal.ZERO;

        for (Entry entry : entries) {
            if (EntryType.TRADE.equals(entry.getType())) {
                closedPositions = closedPositions.add(ofNullable(entry.getNetResult()).orElse(BigDecimal.ZERO));
            }
            if (EntryType.DEPOSIT.equals(entry.getType())) {
                deposits = deposits.add(ofNullable(entry.getPrice()).orElse(BigDecimal.ZERO));
            }
            if (EntryType.WITHDRAWAL.equals(entry.getType())) {
                withdrawals = withdrawals.add(ofNullable(entry.getPrice()).orElse(BigDecimal.ZERO));
            }
            if (EntryType.TAXES.equals(entry.getType())) {
                taxes = taxes.add(ofNullable(entry.getPrice()).orElse(BigDecimal.ZERO));
            }
        }

        BigDecimal accountBalance = journal.getStartBalance().add(closedPositions).add(deposits).subtract(withdrawals).subtract(taxes);

        return Balance.builder()
                .accountBalance(accountBalance.setScale(2, RoundingMode.HALF_EVEN))
                .closedPositions(closedPositions.setScale(2, RoundingMode.HALF_EVEN))
                .deposits(deposits.setScale(2, RoundingMode.HALF_EVEN))
                .withdrawals(withdrawals.setScale(2, RoundingMode.HALF_EVEN))
                .taxes(taxes.setScale(2, RoundingMode.HALF_EVEN))
                .build();
    }
}
