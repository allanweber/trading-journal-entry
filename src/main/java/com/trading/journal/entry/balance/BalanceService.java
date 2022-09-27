package com.trading.journal.entry.balance;

import com.allanweber.jwttoken.data.AccessTokenInfo;

public interface BalanceService {

    Balance getCurrentBalance(AccessTokenInfo accessToken, String journalId);
}
