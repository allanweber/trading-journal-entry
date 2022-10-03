package com.trading.journal.entry.journal;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.balance.Balance;

import java.util.List;

public interface JournalService {

    List<Journal> getAll(AccessTokenInfo accessToken);

    Journal get(AccessTokenInfo accessToken, String journalId);

    Journal save(AccessTokenInfo accessToken, Journal journal);

    long delete(AccessTokenInfo accessToken, String journalId);

    void updateBalance(AccessTokenInfo accessToken, String journalId, Balance balance);
}
