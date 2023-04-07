package com.trading.journal.entry.entries.withdrawal;

import com.trading.journal.entry.entries.Entry;

public interface WithdrawalService {

    Entry create(String journalId, Withdrawal withdrawal);
}
