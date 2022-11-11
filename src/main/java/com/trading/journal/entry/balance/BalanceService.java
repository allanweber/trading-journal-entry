package com.trading.journal.entry.balance;

import com.allanweber.jwttoken.data.AccessTokenInfo;

public interface BalanceService {

    Balance calculateCurrentBalance(AccessTokenInfo accessToken, String journalId);

    Balance getCurrentBalance(AccessTokenInfo accessToken, String journalId);

    Balance calculateAvailableBalance(AccessTokenInfo accessToken, String journalId);
}
