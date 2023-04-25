package com.trading.journal.entry.entries;

import org.springframework.data.domain.Page;

import java.util.List;

public interface EntryService {

    Page<Entry> getAll(EntriesQuery all);

    Entry getById(String entryId);

    Entry save(Entry entry);

    void delete(String entryId);

    void updateImages(String entryId, List<EntryImage> entryImages);
}
