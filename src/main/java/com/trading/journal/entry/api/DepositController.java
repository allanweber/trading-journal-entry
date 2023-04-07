package com.trading.journal.entry.api;

import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.deposit.Deposit;
import com.trading.journal.entry.entries.deposit.DepositService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

import static org.springframework.http.ResponseEntity.created;

@RequiredArgsConstructor
@RestController
public class DepositController implements DepositApi {

    private final DepositService depositService;

    @Override
    public ResponseEntity<Entry> create(String journalId, Deposit deposit) {
        Entry created = depositService.create(journalId, deposit);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.getId()).toUri();
        return created(uri).body(created);
    }
}
