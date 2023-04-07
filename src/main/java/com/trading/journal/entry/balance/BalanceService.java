package com.trading.journal.entry.balance;

public interface BalanceService {

    Balance calculateCurrentBalance(String journalId);

    Balance getCurrentBalance(String journalId);

    Balance calculateAvailableBalance(String journalId);
}
