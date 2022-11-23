package com.trading.journal.entry.entries.taxes.impl;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.EntryService;
import com.trading.journal.entry.entries.taxes.Taxes;
import com.trading.journal.entry.entries.taxes.TaxesMapper;
import com.trading.journal.entry.entries.taxes.TaxesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TaxesServiceImpl implements TaxesService {

    private final EntryService entryService;

    @Override
    public Entry create(AccessTokenInfo accessTokenInfo, String journalId, Taxes taxes) {
        Entry entry = TaxesMapper.INSTANCE.toEntry(taxes);
        return entryService.save(accessTokenInfo, journalId, entry);
    }
}
