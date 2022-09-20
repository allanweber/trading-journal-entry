package com.trading.journal.entry.api;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.EntryService;
import com.trading.journal.entry.queries.QueryConverter;
import com.trading.journal.entry.queries.data.PageResponse;
import com.trading.journal.entry.queries.data.PageableRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;

@RequiredArgsConstructor
@RestController
public class EntryController implements EntryApi {

    private final EntryService entryService;

    @Override
    public ResponseEntity<PageResponse<Entry>> query(AccessTokenInfo accessTokenInfo, String journalId, Integer page, Integer size, String[] sort, String[] filter) {
        PageableRequest pageableRequest = PageableRequest.builder()
                .page(page)
                .size(size)
                .sort(QueryConverter.queryParamsToSort(sort))
                .filters(QueryConverter.queryParamsToFilter(filter))
                .build();
        PageResponse<Entry> pageResponse = entryService.query(accessTokenInfo, journalId, pageableRequest);
        return ok(pageResponse);
    }

    @Override
    public ResponseEntity<List<Entry>> getAll(AccessTokenInfo accessTokenInfo, String journalId) {
        List<Entry> entries = entryService.getAll(accessTokenInfo, journalId);
        return ok(entries);
    }

    @Override
    public ResponseEntity<Entry> create(AccessTokenInfo accessTokenInfo, String journalId, Entry entry) {
        Entry created = entryService.save(accessTokenInfo, journalId, entry);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.getId()).toUri();
        return created(uri).body(created);
    }
}
