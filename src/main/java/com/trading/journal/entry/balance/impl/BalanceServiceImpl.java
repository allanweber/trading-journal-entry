package com.trading.journal.entry.balance.impl;

import com.trading.journal.entry.balance.Balance;
import com.trading.journal.entry.balance.BalanceService;
import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.EntryRepository;
import com.trading.journal.entry.entries.EntryType;
import com.trading.journal.entry.journal.Journal;
import com.trading.journal.entry.journal.JournalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.springframework.data.util.Lazy.of;

@RequiredArgsConstructor
@Service
public class BalanceServiceImpl implements BalanceService {

    private final EntryRepository entryRepository;

    private final JournalService journalService;

    @Override
    public Balance calculateCurrentBalance(String journalId) {
        return calculateBalance(journalId);
    }

    @Override
    public Balance getCurrentBalance(String journalId) {
        Journal journal = journalService.get(journalId);
        return journal.getCurrentBalance();
    }

    @Override
    public Balance calculateAvailableBalance(String journalId) {
        return calculateAvailable(journalId);
    }

    private Balance calculateBalance(String journalId) {
        Journal journal = journalService.get(journalId);
        Pageable page = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("date").ascending());
        Query query = new Query(new Criteria("journalId").is(journalId));
        List<Entry> entries = entryRepository.findAll(page, query).get().toList();

        BigDecimal closedPositions = BigDecimal.ZERO;
        BigDecimal deposits = BigDecimal.ZERO;
        BigDecimal withdrawals = BigDecimal.ZERO;
        BigDecimal taxes = BigDecimal.ZERO;

        List<Entry> finished = entries.stream().filter(Entry::isFinished).toList();
        for (Entry entry : finished) {
            if (EntryType.TRADE.equals(entry.getType())) {
                closedPositions = closedPositions.add(ofNullable(entry.getNetResult()).orElse(BigDecimal.ZERO));
            }
            if (EntryType.DEPOSIT.equals(entry.getType())) {
                deposits = deposits.add(of(entry.getPrice()).orElse(BigDecimal.ZERO));
            }
            if (EntryType.WITHDRAWAL.equals(entry.getType())) {
                withdrawals = withdrawals.add(Optional.of(entry.getPrice()).orElse(BigDecimal.ZERO));
            }
            if (EntryType.TAXES.equals(entry.getType())) {
                taxes = taxes.add(Optional.of(entry.getPrice()).orElse(BigDecimal.ZERO));
            }
        }

        BigDecimal openedPositions = entries.stream().filter(entry -> !entry.isFinished())
                .map(entry -> entry.getPrice().multiply(entry.getSize()))
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);

        BigDecimal accountBalance = journal.getStartBalance().add(closedPositions).add(deposits).subtract(withdrawals).subtract(taxes);
        BigDecimal available = accountBalance.subtract(openedPositions);

        Balance balance = Balance.builder()
                .accountBalance(accountBalance.setScale(2, RoundingMode.HALF_EVEN))
                .closedPositions(closedPositions.setScale(2, RoundingMode.HALF_EVEN))
                .openedPositions(openedPositions.setScale(2, RoundingMode.HALF_EVEN))
                .available(available.setScale(2, RoundingMode.HALF_EVEN))
                .deposits(deposits.setScale(2, RoundingMode.HALF_EVEN))
                .withdrawals(withdrawals.setScale(2, RoundingMode.HALF_EVEN))
                .taxes(taxes.setScale(2, RoundingMode.HALF_EVEN))
                .build();

        journalService.updateBalance(journalId, balance);

        return balance;
    }

    private Balance calculateAvailable(String journalId) {
        Journal journal = journalService.get(journalId);
        Pageable page = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("date").ascending());
        Query query = new Query(Criteria.where("journalId").is(journalId).and("netResult").exists(false));
        List<Entry> openedEntries = entryRepository.findAll(page, query).get().toList();

        BigDecimal openedPositions = openedEntries.stream().map(entry -> entry.getPrice().multiply(entry.getSize()))
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
        BigDecimal available = journal.getCurrentBalance().getAccountBalance().subtract(openedPositions);

        Balance balance = Balance.builder()
                .accountBalance(journal.getCurrentBalance().getAccountBalance().setScale(2, RoundingMode.HALF_EVEN))
                .closedPositions(journal.getCurrentBalance().getClosedPositions().setScale(2, RoundingMode.HALF_EVEN))
                .deposits(journal.getCurrentBalance().getDeposits().setScale(2, RoundingMode.HALF_EVEN))
                .withdrawals(journal.getCurrentBalance().getWithdrawals().setScale(2, RoundingMode.HALF_EVEN))
                .taxes(journal.getCurrentBalance().getTaxes().setScale(2, RoundingMode.HALF_EVEN))
                .openedPositions(openedPositions.setScale(2, RoundingMode.HALF_EVEN))
                .available(available.setScale(2, RoundingMode.HALF_EVEN))
                .build();

        journalService.updateBalance(journalId, balance);

        return balance;
    }
}
