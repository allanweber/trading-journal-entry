package com.trading.journal.entry.entries.deposit.impl;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.EntryService;
import com.trading.journal.entry.entries.deposit.Deposit;
import com.trading.journal.entry.entries.deposit.DepositMapper;
import com.trading.journal.entry.entries.deposit.DepositService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class DepositServiceImpl implements DepositService {

    private final EntryService entryService;

    @Override
    public Entry create(AccessTokenInfo accessTokenInfo, String journalId, Deposit deposit) {
        Entry entry = DepositMapper.INSTANCE.toEntry(deposit);
        return entryService.save(accessTokenInfo, journalId, entry);
    }
}
