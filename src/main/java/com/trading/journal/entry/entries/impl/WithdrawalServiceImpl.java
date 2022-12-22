package com.trading.journal.entry.entries.impl;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.EntryService;
import com.trading.journal.entry.entries.withdrawal.Withdrawal;
import com.trading.journal.entry.entries.withdrawal.WithdrawalMapper;
import com.trading.journal.entry.entries.withdrawal.WithdrawalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class WithdrawalServiceImpl implements WithdrawalService {

    private final EntryService entryService;

    @Override
    public Entry create(AccessTokenInfo accessTokenInfo, String journalId, Withdrawal withdrawal) {
        Entry entry = WithdrawalMapper.INSTANCE.toEntry(withdrawal);
        return entryService.save(accessTokenInfo, journalId, entry);
    }
}
