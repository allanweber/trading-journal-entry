package com.trading.journal.entry.entries;

import com.trading.journal.entry.query.data.PageResponse;
import com.trading.journal.entry.query.PageableRequest;

public interface EntryService {

    PageResponse<Entry> getAll(String tenancy, PageableRequest pageableRequest);

    Entry create(String tenancy, Entry entry);
}
