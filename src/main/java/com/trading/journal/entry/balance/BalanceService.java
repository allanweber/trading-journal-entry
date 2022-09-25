package com.trading.journal.entry.balance;

import com.allanweber.jwttoken.data.AccessTokenInfo;

import java.time.LocalDateTime;

public interface BalanceService {

    Balance getCurrentBalance(AccessTokenInfo accessToken, String journalId);

    Balance getCurrentBalance(AccessTokenInfo accessToken, String journalId, LocalDateTime date);
}
