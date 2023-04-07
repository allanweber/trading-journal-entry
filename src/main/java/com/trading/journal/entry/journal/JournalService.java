package com.trading.journal.entry.journal;

import com.trading.journal.entry.balance.Balance;

import java.util.List;

public interface JournalService {

    List<Journal> getAll();

    Journal get(String journalId);

    Journal save(Journal journal);

    void delete(String journalId);

    void updateBalance(String journalId, Balance balance);
}
