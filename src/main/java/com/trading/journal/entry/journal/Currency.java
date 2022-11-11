package com.trading.journal.entry.journal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Currency {

    DOLLAR("$"),
    EURO("€"),
    REAL("R$");

    private final String symbol;
}
