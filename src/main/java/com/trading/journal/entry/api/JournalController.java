package com.trading.journal.entry.api;

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
    public ResponseEntity<List<JournalData>> getAll() {
        List<Journal> journals = journalService.getAll();
        return ok(journals.stream().map(JournalData::fromJournal).toList());
    }

    @Override
    public ResponseEntity<JournalData> get(String journalId) {
        Journal journal = journalService.get(journalId);
        return ok(JournalData.fromJournal(journal));
    }

    @Override
    public ResponseEntity<JournalData> save(Journal journal) {
        Journal saved = journalService.save(journal);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(saved.getId()).toUri();
        return created(uri).body(JournalData.fromJournal(saved));
    }

    @Override
    public ResponseEntity<Void> delete(String journalId) {
        journalService.delete(journalId);
        return ok().build();
    }

    @Override
    public ResponseEntity<Balance> balance(String journalId) {
        Balance balance = balanceService.getCurrentBalance(journalId);
        return ok(balance);
    }
}
