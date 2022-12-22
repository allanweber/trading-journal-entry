package com.trading.journal.entry.entries;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum EntryStatus {

    OPEN("OPEN"),
    CLOSED("CLOSED");

    private final String label;
}
