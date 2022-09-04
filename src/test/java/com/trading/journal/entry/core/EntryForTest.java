package com.trading.journal.entry.core;

import com.trading.journal.entry.entries.EntryDirection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EntryForTest {

    private String id;

    private LocalDateTime date;

    private String symbol;

    private EntryDirection direction;

    private Double price;

    private Integer someInteger;

    private BigDecimal someDecimal;
}
