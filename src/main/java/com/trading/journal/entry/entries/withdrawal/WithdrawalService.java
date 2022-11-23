package com.trading.journal.entry.entries.withdrawal;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.entries.Entry;

public interface WithdrawalService {

    Entry create(AccessTokenInfo accessTokenInfo, String journalId, Withdrawal withdrawal);
}
