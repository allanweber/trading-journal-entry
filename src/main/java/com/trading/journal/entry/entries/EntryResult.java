package com.trading.journal.entry.entries;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum EntryResult {
    WIN("WIN"),
    LOSE("LOSE");

    private final String label;
}
