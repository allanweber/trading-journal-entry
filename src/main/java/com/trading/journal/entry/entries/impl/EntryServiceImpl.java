package com.trading.journal.entry.entries.impl;

import com.trading.journal.entry.ApplicationException;
import com.trading.journal.entry.balance.Balance;
import com.trading.journal.entry.balance.BalanceService;
import com.trading.journal.entry.entries.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class EntryServiceImpl implements EntryService {

    private final EntryRepository repository;

    private final BalanceService balanceService;

    @Override
    public Page<Entry> getAll(EntriesQuery entriesQuery) {
        Query query = entriesQuery.buildQuery();
        Page<Entry> page = repository.findAll(entriesQuery.getPageable(), query);
        return new PageImpl<>(page.toList(), entriesQuery.getPageable(), page.getTotalElements());
    }

    @Override
    public Entry getById(String entryId) {
        return get(entryId);
    }

    @Override
    public Entry save(Entry entry) {
        Balance balance = balanceService.getCurrentBalance(entry.getJournalId());
        CalculateEntry calculateEntry = new CalculateEntry(entry, balance.getAccountBalance());
        Entry calculated = calculateEntry.calculate();
        Entry saved = repository.save(calculated);
        if (saved.isFinished()) {
            balanceService.calculateCurrentBalance(entry.getJournalId());
        } else {
            balanceService.calculateAvailableBalance(entry.getJournalId());
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

    @Override
    public void updateImages(String entryId, List<EntryImage> entryImages) {
        Query query = new Query(Criteria.where("_id").is(entryId));
        Update update = new Update().set("images", entryImages);
        if (entryImages == null || entryImages.isEmpty()) {
            update = new Update().unset("images");
        }
        repository.update(query, update);
    }

    @Override
    public Long countByStrategy(String strategyId) {
        Criteria criteria = Criteria.where("strategies._id").is(new ObjectId(strategyId));
        return repository.count(Query.query(criteria));
    }

    private Entry get(String entryId) {
        return repository.getById(entryId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Entry not found"));
    }
}
