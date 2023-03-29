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
        Balance balance = journal.getCurrentBalance();
        balance.setStartBalance(journal.getStartBalance().setScale(2, RoundingMode.HALF_EVEN));
        balance.setCurrency(journal.getCurrency());
        balance.setStartJournal(journal.getStartJournal());
        return balance;
    }

    @Override
    public Balance calculateAvailableBalance(AccessTokenInfo accessToken, String journalId) {
        return calculateAvailable(accessToken, journalId);
    }

    private Balance calculateBalance(AccessTokenInfo accessToken, String journalId) {
        Journal journal = journalService.get(accessToken, journalId);
        CollectionName collectionName = new CollectionName(accessToken, journal.getName());
        Pageable page = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("date").ascending());
        List<Entry> entries = entryRepository.findAll(collectionName, page).get().toList();

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
                deposits = deposits.add(ofNullable(entry.getPrice()).orElse(BigDecimal.ZERO));
            }
            if (EntryType.WITHDRAWAL.equals(entry.getType())) {
                withdrawals = withdrawals.add(ofNullable(entry.getPrice()).orElse(BigDecimal.ZERO));
            }
            if (EntryType.TAXES.equals(entry.getType())) {
                taxes = taxes.add(ofNullable(entry.getPrice()).orElse(BigDecimal.ZERO));
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

        journalService.updateBalance(accessToken, journalId, balance);

        return balance;
    }

    private Balance calculateAvailable(AccessTokenInfo accessToken, String journalId) {
        Journal journal = journalService.get(accessToken, journalId);
        CollectionName collectionName = new CollectionName(accessToken, journal.getName());
        Pageable page = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("date").ascending());
        Query query = new Query(Criteria.where("netResult").exists(false));
        List<Entry> openedEntries = entryRepository.findAll(collectionName, page, query).get().toList();

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

        journalService.updateBalance(accessToken, journalId, balance);

        return balance;
    }
}
