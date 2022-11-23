package com.trading.journal.entry.entries.taxes;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.entries.Entry;

public interface TaxesService {

    Entry create(AccessTokenInfo accessTokenInfo, String journalId, Taxes deposit);
}
