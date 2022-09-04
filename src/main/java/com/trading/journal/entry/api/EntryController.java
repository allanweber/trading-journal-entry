package com.trading.journal.entry.api;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.EntryService;
import com.trading.journal.entry.query.data.PageResponse;
import com.trading.journal.entry.query.PageableRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

@RequiredArgsConstructor
@RestController
public class EntryController implements EntryApi {

    private final EntryService entryService;

    @Override
    public ResponseEntity<PageResponse<Entry>> getAll(AccessTokenInfo accessTokenInfo, Integer page, Integer size, String[] sort, String[] filter) {
        PageableRequest pageableRequest = PageableRequest.builder()
                .page(page)
                .size(size)
                .sort(sort)
                .filter(filter)
                .build();
        PageResponse<Entry> pageResponse = entryService.getAll(accessTokenInfo.tenancyName(), pageableRequest);
        return ok(pageResponse);
    }

    @Override
    public ResponseEntity<Entry> create(AccessTokenInfo accessTokenInfo, Entry entry) {
        return ok(entryService.create(accessTokenInfo.tenancyName(), entry));
    }
}
