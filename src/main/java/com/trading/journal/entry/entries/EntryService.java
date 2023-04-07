package com.trading.journal.entry.entries;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface EntryService {

    Page<Entry> getAll(EntriesQuery all);

    Entry getById(String entryId);

    Entry save(Entry entry);

    void delete(String entryId);

    void uploadImage(String entryId, UploadType type, MultipartFile file);

    EntryImageResponse returnImage(String entryId, UploadType type);
}
