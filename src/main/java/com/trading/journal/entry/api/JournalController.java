package com.trading.journal.entry.api;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.journal.Journal;
import com.trading.journal.entry.journal.JournalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class JournalController implements JournalApi {

    private final JournalService journalService;

    @Override
    public ResponseEntity<List<Journal>> getAll(AccessTokenInfo accessTokenInfo) {
        List<Journal> journals = journalService.getAll(accessTokenInfo);
        return ok(journals);
    }

    @Override
    public ResponseEntity<Journal> get(AccessTokenInfo accessTokenInfo, String journalId) {
        Journal journal = journalService.get(accessTokenInfo, journalId);
        return ok(journal);
    }

    @Override
    public ResponseEntity<Journal> create(AccessTokenInfo accessTokenInfo, Journal data) {
        Journal created = journalService.save(accessTokenInfo, data);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.getId()).toUri();
        return created(uri).body(created);
    }

    @Override
    public ResponseEntity<Journal> delete(AccessTokenInfo accessTokenInfo, String journalId) {
        journalService.delete(accessTokenInfo, journalId);
        return ok().build();
    }
}
