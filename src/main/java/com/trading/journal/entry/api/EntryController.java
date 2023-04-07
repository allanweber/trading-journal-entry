package com.trading.journal.entry.api;

import com.trading.journal.entry.entries.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public ResponseEntity<PageWrapper<Entry>> getAll(
            String journalId, Pageable pageable,
            String symbol, EntryType type, EntryStatus status, String from,
            EntryDirection direction, EntryResult result, List<String> strategies
    ) {
        EntriesQuery entriesQuery = EntriesQuery.builder()
                .journalId(journalId)
                .symbol(symbol)
                .type(type)
                .status(status)
                .from(from)
                .direction(direction)
                .result(result)
                .strategyIds(strategies)
                .pageable(pageable)
                .build();
        Page<Entry> entries = entryService.getAll(entriesQuery);
        return ok(new PageWrapper<>(entries));
    }

    @Override
    public ResponseEntity<Void> delete(String entryId) {
        entryService.delete(entryId);
        return ok().build();
    }

    @Override
    public ResponseEntity<Entry> get(String entryId) {
        Entry entry = entryService.getById(entryId);
        return ok(entry);
    }

    @Override
    public ResponseEntity<Void> uploadImage(String entryId, UploadType type, MultipartFile file) {
        entryService.uploadImage(entryId, type, file);
        return ok().build();
    }

    @Override
    public ResponseEntity<EntryImageResponse> getImage(String entryId, UploadType type) {
        EntryImageResponse image = entryService.returnImage(entryId, type);
        return ok(image);
    }


}
