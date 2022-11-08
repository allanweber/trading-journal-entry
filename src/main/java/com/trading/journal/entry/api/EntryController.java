package com.trading.journal.entry.api;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.EntryImageResponse;
import com.trading.journal.entry.entries.EntryService;
import com.trading.journal.entry.entries.UploadType;
import com.trading.journal.entry.queries.QueryConverter;
import com.trading.journal.entry.queries.data.PageResponse;
import com.trading.journal.entry.queries.data.PageableRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;

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
    public ResponseEntity<Entry> save(AccessTokenInfo accessTokenInfo, String journalId, Entry entry) {
        boolean isNewEntry = Objects.isNull(entry.getId());
        Entry created = entryService.save(accessTokenInfo, journalId, entry);
        ResponseEntity<Entry> body;
        if (isNewEntry) {
            URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.getId()).toUri();
            body = created(uri).body(created);
        } else {
            body = ok(created);
        }
        return body;
    }

    @Override
    public ResponseEntity<Void> delete(AccessTokenInfo accessTokenInfo, String journalId, String entryId) {
        entryService.delete(accessTokenInfo, journalId, entryId);
        return ok().build();
    }

    @Override
    public ResponseEntity<Void> uploadImage(AccessTokenInfo accessTokenInfo, String journalId, String entryId, UploadType type, MultipartFile file) {
        entryService.uploadImage(accessTokenInfo, journalId, entryId, type, file);
        return ok().build();
    }

    @Override
    public ResponseEntity<EntryImageResponse> getImage(AccessTokenInfo accessTokenInfo, String journalId, String entryId, UploadType type) {
        EntryImageResponse image = entryService.returnImage(accessTokenInfo, journalId, entryId, type);
        return ok(image);
    }
}
