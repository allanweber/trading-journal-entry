package com.trading.journal.entry.entries.taxes;

import com.trading.journal.entry.entries.Entry;

public interface TaxesService {

    Entry create(String journalId, Taxes deposit);
}
