package com.trading.journal.entry.entries.impl;

import com.allanweber.jwttoken.data.AccessTokenInfo;
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
import org.springframework.stereotype.Service;

import java.util.function.BiFunction;

@RequiredArgsConstructor
@Service
public class EntryServiceImpl implements EntryService {

    private final EntryRepository repository;

    private final JournalService journalService;

    @Override
    public PageResponse<Entry> getAll(AccessTokenInfo accessToken, String journalId, PageableRequest pageRequest) {
        CollectionName collectionName = collectionName().apply(accessToken, journalId);
        Page<Entry> page = repository.findAll(collectionName, pageRequest);
        return new PageResponse<>(page);
    }

    @Override
    public Entry create(AccessTokenInfo accessToken, String journalId, Entry entry) {
        CollectionName collectionName = collectionName().apply(accessToken, journalId);
        return repository.save(collectionName, entry);
    }

    private BiFunction<AccessTokenInfo, String, CollectionName> collectionName() {
        return (accessTokenInfo, journalId) -> {
            Journal journal = journalService.get(accessTokenInfo, journalId);
            return new CollectionName(accessTokenInfo, journal.getName());
        };
    }
}
