package com.trading.journal.entry.entries.deposit;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.entries.Entry;

public interface DepositService {

    Entry create(AccessTokenInfo accessTokenInfo, String journalId, Deposit deposit);
}
