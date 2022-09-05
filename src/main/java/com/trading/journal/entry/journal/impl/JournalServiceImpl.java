package com.trading.journal.entry.journal.impl;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.ApplicationException;
import com.trading.journal.entry.journal.Journal;
import com.trading.journal.entry.journal.JournalRepository;
import com.trading.journal.entry.journal.JournalService;
import com.trading.journal.entry.queries.CollectionName;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class JournalServiceImpl implements JournalService {

    private final JournalRepository journalRepository;

    @Override
    public List<Journal> getAll(AccessTokenInfo accessToken) {
        return journalRepository.getAll(new CollectionName(accessToken));
    }

    @Override
    public Journal get(AccessTokenInfo accessToken, String journalId) {
        return journalRepository.getById(new CollectionName(accessToken), journalId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Journal not found"));
    }

    @Override
    public Journal save(AccessTokenInfo accessToken, Journal journal) {
        return journalRepository.save(new CollectionName(accessToken), journal);
    }

    @Override
    public long delete(AccessTokenInfo accessToken, String journalId) {
        Journal journal = get(accessToken, journalId);
        return journalRepository.delete(new CollectionName(accessToken), journal);
    }
}
