package com.trading.journal.entry.entries.deposit;

import com.trading.journal.entry.entries.Entry;

public interface DepositService {

    Entry create(String journalId, Deposit deposit);
}
