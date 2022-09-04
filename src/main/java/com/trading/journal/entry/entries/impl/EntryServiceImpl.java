package com.trading.journal.entry.entries.impl;

import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.EntryPageableRepository;
import com.trading.journal.entry.entries.EntryService;
import com.trading.journal.entry.query.PageableRequest;
import com.trading.journal.entry.query.data.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class EntryServiceImpl implements EntryService {

    private final EntryPageableRepository repository;

    public EntryServiceImpl(EntryPageableRepository repository) {
        this.repository = repository;
    }

    @Override
    public PageResponse<Entry> getAll(String tenancy, PageableRequest pageRequest) {
        Page<Entry> page = repository.findAll(tenancy, pageRequest);
        return new PageResponse<>(page);
    }

    @Override
    public Entry create(String tenancy, Entry entry) {
        return repository.save(tenancy, entry);
    }
}
