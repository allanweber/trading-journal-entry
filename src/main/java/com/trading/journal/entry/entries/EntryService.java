package com.trading.journal.entry.entries;

import org.springframework.data.domain.Page;

public interface EntryService {

    Page<Entry> getAll(EntriesQuery all);

    Entry getById(String entryId);

    Entry save(Entry entry);

    void delete(String entryId);
}
