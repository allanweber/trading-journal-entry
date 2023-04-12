package com.trading.journal.entry.entries.impl;

import com.trading.journal.entry.ApplicationException;
import com.trading.journal.entry.balance.Balance;
import com.trading.journal.entry.balance.BalanceService;
import com.trading.journal.entry.entries.*;
import com.trading.journal.entry.strategy.Strategy;
import com.trading.journal.entry.strategy.StrategyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
@Service
@Slf4j
public class EntryServiceImpl implements EntryService {

    private final EntryRepository repository;

    private final BalanceService balanceService;

    private final StrategyService strategyService;

    @Override
    public Page<Entry> getAll(EntriesQuery entriesQuery) {
        Query query = entriesQuery.buildQuery();
        Page<Entry> page = repository.findAll(entriesQuery.getPageable(), query);
        List<Entry> entries = page.stream().map(this::loadStrategies).toList();
        return new PageImpl<>(entries, entriesQuery.getPageable(), page.getTotalElements());
    }

    @Override
    public Entry getById(String entryId) {
        Entry entry = get(entryId);
        return loadStrategies(entry);
    }

    @Override
    public Entry save(Entry entry) {
        List<Strategy> strategies = ofNullable(entry.getStrategyIds())
                .orElse(emptyList())
                .stream()
                .map(strategyId -> strategyService.getById(strategyId)
                        .orElseThrow(() -> new ApplicationException(HttpStatus.BAD_REQUEST, String.format("Invalid Strategy %s", strategyId)))
                ).toList();

        Balance balance = balanceService.getCurrentBalance(entry.getJournalId());
        CalculateEntry calculateEntry = new CalculateEntry(entry, balance.getAccountBalance());
        Entry calculated = calculateEntry.calculate();
        Entry saved = repository.save(calculated);
        if (saved.isFinished()) {
            balanceService.calculateCurrentBalance(entry.getJournalId());
        } else {
            balanceService.calculateAvailableBalance(entry.getJournalId());
        }
        if (!strategies.isEmpty()) {
            saved.setStrategies(strategies);
        }
        return saved;
    }

    @Override
    public void delete(String entryId) {
        Entry entry = get(entryId);
        repository.delete(entry);
        if (entry.isFinished()) {
            balanceService.calculateCurrentBalance(entry.getJournalId());
        }
    }

    private Entry get(String entryId) {
        return repository.getById(entryId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Entry not found"));
    }

    private Entry loadStrategies(Entry entry) {
        List<Strategy> strategies = ofNullable(entry.getStrategyIds())
                .orElse(emptyList())
                .stream()
                .map(id ->
                        strategyService.getById(id).orElse(null)
                )
                .filter(Objects::nonNull).toList();
        if (!strategies.isEmpty()) {
            entry.setStrategies(strategies);
        }
        return entry;
    }
}
