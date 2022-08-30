package com.trading.journal.entry.entries;

import com.trading.journal.entry.pageable.PageResponse;
import com.trading.journal.entry.pageable.PageableRequest;

public interface EntryService {

    PageResponse<Entry> getAll(String tenancy, PageableRequest pageableRequest);

    Entry create(String tenancy, Entry entry);
}
