package com.trading.journal.entry.entries;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.queries.data.PageResponse;
import com.trading.journal.entry.queries.data.PageableRequest;

public interface EntryService {

    PageResponse<Entry> getAll(AccessTokenInfo accessToken, String journalId, PageableRequest pageableRequest);

    Entry create(AccessTokenInfo accessToken, String journalId, Entry entry);
}
