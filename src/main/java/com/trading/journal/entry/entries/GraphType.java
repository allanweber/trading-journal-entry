package com.trading.journal.entry.entries;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum GraphType {

    CANDLESTICK("Candlestick"),
    RENKO("Renko"),
    KAGI("Kagi"),
    LINE("Line");

    private final String label;
}
