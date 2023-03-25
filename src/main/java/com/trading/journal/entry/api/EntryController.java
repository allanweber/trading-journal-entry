package com.trading.journal.entry.api;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.entries.*;
import com.trading.journal.entry.queries.data.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@RequiredArgsConstructor
@RestController
public class EntryController implements EntryApi {

    private final EntryService entryService;

    @Override
    public ResponseEntity<PageResponse<Entry>> getAll(
            AccessTokenInfo accessTokenInfo, String journalId, int page, int size,
            String symbol, EntryType type, EntryStatus status, String from,
            EntryDirection direction, EntryResult result, List<String> strategies
    ) {
        EntriesQuery entriesQuery = EntriesQuery.builder()
                .accessTokenInfo(accessTokenInfo)
                .journalId(journalId)
                .page(page)
                .size(size)
                .symbol(symbol)
                .type(type)
                .status(status)
                .from(from)
                .direction(direction)
                .result(result)
                .strategyIds(strategies)
                .build();
        PageResponse<Entry> entries = entryService.getAll(entriesQuery);
        return ok(entries);
    }

    @Override
    public ResponseEntity<Void> delete(AccessTokenInfo accessTokenInfo, String journalId, String entryId) {
        entryService.delete(accessTokenInfo, journalId, entryId);
        return ok().build();
    }

    @Override
    public ResponseEntity<Entry> get(AccessTokenInfo accessTokenInfo, String journalId, String entryId) {
        Entry entry = entryService.getById(accessTokenInfo, journalId, entryId);
        return ok(entry);
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
