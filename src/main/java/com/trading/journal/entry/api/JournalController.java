package com.trading.journal.entry.api;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.balance.Balance;
import com.trading.journal.entry.balance.BalanceService;
import com.trading.journal.entry.journal.Journal;
import com.trading.journal.entry.journal.JournalData;
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

    private final BalanceService balanceService;

    @Override
    public ResponseEntity<List<JournalData>> getAll(AccessTokenInfo accessTokenInfo) {
        List<Journal> journals = journalService.getAll(accessTokenInfo);
        return ok(journals.stream().map(JournalData::fromJournal).toList());
    }

    @Override
    public ResponseEntity<JournalData> get(AccessTokenInfo accessTokenInfo, String journalId) {
        Journal journal = journalService.get(accessTokenInfo, journalId);
        return ok(JournalData.fromJournal(journal));
    }

    @Override
    public ResponseEntity<JournalData> save(AccessTokenInfo accessTokenInfo, Journal journal) {
        Journal saved = journalService.save(accessTokenInfo, journal);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(saved.getId()).toUri();
        return created(uri).body(JournalData.fromJournal(saved));
    }

    @Override
    public ResponseEntity<Void> delete(AccessTokenInfo accessTokenInfo, String journalId) {
        journalService.delete(accessTokenInfo, journalId);
        return ok().build();
    }

    @Override
    public ResponseEntity<Balance> balance(AccessTokenInfo accessTokenInfo, String journalId) {
        Balance balance = balanceService.getCurrentBalance(accessTokenInfo, journalId);
        return ok(balance);
    }
}
