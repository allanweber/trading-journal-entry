package com.trading.journal.entry.entries;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum EntryType {

    TRADE("Trade"),
    WITHDRAWAL("Withdrawal"),
    DEPOSIT("Deposit"),
    TAXES("Taxes"),
    SCALE_IN("Scale In"),
    SCALE_OUT("Scale Out");

    private final String label;
}
