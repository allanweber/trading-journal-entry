package com.trading.journal.entry.api;

import com.trading.journal.entry.balance.Balance;
import com.trading.journal.entry.journal.Journal;
import com.trading.journal.entry.journal.JournalData;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/journals")
public interface JournalApi {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<List<JournalData>> getAll();

    @GetMapping("/{journal-id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<JournalData> get(@PathVariable(name = "journal-id") String journalId);

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<JournalData> save(@RequestBody @Valid Journal data);

    @DeleteMapping("/{journal-id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Void> delete(@PathVariable(name = "journal-id") String journalId);

    @GetMapping("/{journal-id}/balance")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Balance> balance(@PathVariable(name = "journal-id") String journalId);
}
