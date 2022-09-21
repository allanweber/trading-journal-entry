package com.trading.journal.entry.entries;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum EntryType {

    TRADE("Trade"),
    WITHDRAWAL("Withdrawal"),
    DEPOSIT("Deposit"),
    TAXES("Taxes");

    private final String label;
}
