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
import com.trading.journal.entry.queries.data.Filter;
import com.trading.journal.entry.queries.data.FilterOperation;
import com.trading.journal.entry.queries.data.PageableRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
@Service
public class BalanceServiceImpl implements BalanceService {

    private final EntryRepository entryRepository;

    private final JournalService journalService;

    @Override
    public Balance calculateCurrentBalance(AccessTokenInfo accessToken, String journalId) {
        return calculateBalance(accessToken, journalId);
    }

    @Override
    public Balance getCurrentBalance(AccessTokenInfo accessToken, String journalId) {
        Journal journal = journalService.get(accessToken, journalId);
        return journal.getCurrentBalance();
    }

    private Balance calculateBalance(AccessTokenInfo accessToken, String journalId) {
        Journal journal = journalService.get(accessToken, journalId);
        CollectionName collectionName = new CollectionName(accessToken, journal.getName());

        PageableRequest pageableRequest = PageableRequest.builder()
                .page(0)
                .size(Integer.MAX_VALUE)
                .sort(Sort.by("date").ascending())
                .filters(singletonList(
                        Filter.builder().field("netResult").operation(FilterOperation.EXISTS).value("true").build()
                ))
                .build();

        List<Entry> entries = entryRepository.findAll(collectionName, pageableRequest).get().toList();

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

        Balance balance = Balance.builder()
                .accountBalance(accountBalance.setScale(2, RoundingMode.HALF_EVEN))
                .closedPositions(closedPositions.setScale(2, RoundingMode.HALF_EVEN))
                .deposits(deposits.setScale(2, RoundingMode.HALF_EVEN))
                .withdrawals(withdrawals.setScale(2, RoundingMode.HALF_EVEN))
                .taxes(taxes.setScale(2, RoundingMode.HALF_EVEN))
                .build();

        journalService.updateBalance(accessToken, journalId, balance);

        return balance;
    }
}
