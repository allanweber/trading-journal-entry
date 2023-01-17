package com.trading.journal.entry.entries.trade.impl;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.journal.Journal;
import com.trading.journal.entry.journal.JournalService;
import com.trading.journal.entry.queries.CollectionName;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TradeCollectionName {

    private final JournalService journalService;

    public CollectionName collectionName(AccessTokenInfo accessTokenInfo, String journalId) {
        Journal journal = journalService.get(accessTokenInfo, journalId);
        return new CollectionName(accessTokenInfo, journal.getName());
    }
}
