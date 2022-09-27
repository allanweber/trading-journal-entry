package com.trading.journal.entry.entries.impl;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.ApplicationException;
import com.trading.journal.entry.balance.Balance;
import com.trading.journal.entry.balance.BalanceService;
import com.trading.journal.entry.entries.CalculateEntry;
import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.EntryRepository;
import com.trading.journal.entry.entries.EntryService;
import com.trading.journal.entry.journal.Journal;
import com.trading.journal.entry.journal.JournalService;
import com.trading.journal.entry.queries.CollectionName;
import com.trading.journal.entry.queries.data.PageResponse;
import com.trading.journal.entry.queries.data.PageableRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.BiFunction;

@RequiredArgsConstructor
@Service
public class EntryServiceImpl implements EntryService {

    private final EntryRepository repository;

    private final JournalService journalService;

    private final BalanceService balanceService;

    @Override
    public PageResponse<Entry> query(AccessTokenInfo accessToken, String journalId, PageableRequest pageRequest) {
        CollectionName collectionName = collectionName().apply(accessToken, journalId);
        Page<Entry> page = repository.findAll(collectionName, pageRequest);
        return new PageResponse<>(page);
    }

    @Override
    public List<Entry> getAll(AccessTokenInfo accessToken, String journalId) {
        CollectionName collectionName = collectionName().apply(accessToken, journalId);
        PageableRequest pageableRequest = PageableRequest.builder()
                .page(0)
                .size(Integer.MAX_VALUE)
                .sort(Sort.by("date").ascending())
                .build();
        Page<Entry> all = repository.findAll(collectionName, pageableRequest);
        return all.stream().toList();
    }

    @Override
    public Entry save(AccessTokenInfo accessToken, String journalId, Entry entry) {
        Balance balance = balanceService.getCurrentBalance(accessToken, journalId);

        CalculateEntry calculateEntry = new CalculateEntry(entry, balance.getAccountBalance());
        Entry calculated = calculateEntry.calculate();

        CollectionName collectionName = collectionName().apply(accessToken, journalId);
        return repository.save(collectionName, calculated);
//        if (saved.isFinished()) {
//            balanceEntries(accessToken, journalId, entry, collectionName);
//        }
    }

    @Override
    public void delete(AccessTokenInfo accessToken, String journalId, String entryId) {
        CollectionName collectionName = collectionName().apply(accessToken, journalId);
        Entry entry = get(collectionName, entryId);
//        if (entry.isFinished()) {
//            balanceEntries(accessToken, journalId, entry, collectionName);
//        }
        repository.delete(collectionName, entry);
    }

//    private void balanceEntries(AccessTokenInfo accessToken, String journalId, Entry changedEntry, CollectionName collectionName) {
//        PageableRequest pageableRequest = PageableRequest.builder()
//                .page(0)
//                .size(Integer.MAX_VALUE)
//                .sort(Sort.by("date").ascending())
//                .filters(singletonList(
//                        Filter.builder().field("netResult").operation(FilterOperation.EXISTS).value("true").build()
//                ))
//                .build();
//
//        List<Entry> entries = repository.findAll(collectionName, pageableRequest).get().toList();
//        Balance balance = balanceService.getCurrentBalance(accessToken, journalId);
//
//        entries.forEach(entry -> {
//            CalculateEntry calculateEntry = new CalculateEntry(entry, balance.getAccountBalance());
//            Entry calculated = calculateEntry.calculate();
//            repository.save(calculated);
//        });
//    }

    private Entry get(CollectionName collectionName, String entryId) {
        return repository.getById(collectionName, entryId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Entry not found"));
    }

    private BiFunction<AccessTokenInfo, String, CollectionName> collectionName() {
        return (accessTokenInfo, journalId) -> {
            Journal journal = journalService.get(accessTokenInfo, journalId);
            return new CollectionName(accessTokenInfo, journal.getName());
        };
    }
}
