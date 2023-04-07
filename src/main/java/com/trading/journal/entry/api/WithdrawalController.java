package com.trading.journal.entry.api;

import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.withdrawal.Withdrawal;
import com.trading.journal.entry.entries.withdrawal.WithdrawalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

import static org.springframework.http.ResponseEntity.created;

@RequiredArgsConstructor
@RestController
public class WithdrawalController implements WithdrawalApi {

    private final WithdrawalService withdrawalService;

    @Override
    public ResponseEntity<Entry> create(String journalId, Withdrawal withdrawal) {
        Entry created = withdrawalService.create(journalId, withdrawal);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.getId()).toUri();
        return created(uri).body(created);
    }
}
