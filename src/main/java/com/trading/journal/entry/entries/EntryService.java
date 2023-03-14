package com.trading.journal.entry.entries;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EntryService {

    List<Entry> getAll(GetAll all);

    Entry save(AccessTokenInfo accessToken, String journalId, Entry entry);

    Entry getById(AccessTokenInfo accessToken, String journalId, String entryId);

    void delete(AccessTokenInfo accessToken, String journalId, String entryId);

    void uploadImage(AccessTokenInfo accessToken, String journalId, String entryId, UploadType type, MultipartFile file);

    EntryImageResponse returnImage(AccessTokenInfo accessToken, String journalId, String entryId, UploadType type);
}
