package com.trading.journal.entry.api;

import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.deposit.Deposit;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/journals/{journal-id}/entries/deposit")
public interface DepositApi {

    @PostMapping()
    ResponseEntity<Entry> create(@PathVariable(name = "journal-id") String journalId, @RequestBody @Valid Deposit deposit);
}
