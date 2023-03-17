package com.trading.journal.entry.entries;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EntryService {

    List<Entry> getAll(EntriesQuery all);

    Entry getById(AccessTokenInfo accessToken, String journalId, String entryId);

    Entry save(AccessTokenInfo accessToken, String journalId, Entry entry);

    void delete(AccessTokenInfo accessToken, String journalId, String entryId);

    void uploadImage(AccessTokenInfo accessToken, String journalId, String entryId, UploadType type, MultipartFile file);

    EntryImageResponse returnImage(AccessTokenInfo accessToken, String journalId, String entryId, UploadType type);
}
