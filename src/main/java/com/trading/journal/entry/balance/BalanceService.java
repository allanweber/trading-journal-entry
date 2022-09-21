package com.trading.journal.entry.balance;

import com.allanweber.jwttoken.data.AccessTokenInfo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface BalanceService {

    BigDecimal getCurrentBalance(AccessTokenInfo accessToken, String journalId);

    BigDecimal getCurrentBalance(AccessTokenInfo accessToken, String journalId, LocalDateTime date);

//    void balanceForward(AccessTokenInfo accessToken, String journalId, LocalDateTime date);
}
