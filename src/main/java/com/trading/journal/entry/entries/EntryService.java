package com.trading.journal.entry.entries;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.queries.data.PageResponse;
import com.trading.journal.entry.queries.data.PageableRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EntryService {

    PageResponse<Entry> query(AccessTokenInfo accessToken, String journalId, PageableRequest pageableRequest);

    List<Entry> getAll(GetAll all);

    Entry save(AccessTokenInfo accessToken, String journalId, Entry entry);

    void delete(AccessTokenInfo accessToken, String journalId, String entryId);

    void uploadImage(AccessTokenInfo accessToken, String journalId, String entryId, UploadType type, MultipartFile file);

    EntryImageResponse returnImage(AccessTokenInfo accessToken, String journalId, String entryId, UploadType type);
}
