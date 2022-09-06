package com.trading.journal.entry.entries;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.queries.data.PageResponse;
import com.trading.journal.entry.queries.data.PageableRequest;

import java.util.List;

public interface EntryService {

    PageResponse<Entry> query(AccessTokenInfo accessToken, String journalId, PageableRequest pageableRequest);

    List<Entry> getAll(AccessTokenInfo accessToken, String journalId);

    Entry create(AccessTokenInfo accessToken, String journalId, Entry entry);
}
