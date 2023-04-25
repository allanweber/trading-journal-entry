package com.trading.journal.entry.journal.impl;

import com.trading.journal.entry.ApplicationException;
import com.trading.journal.entry.balance.Balance;
import com.trading.journal.entry.entries.EntryRepository;
import com.trading.journal.entry.journal.Journal;
import com.trading.journal.entry.journal.JournalRepository;
import com.trading.journal.entry.journal.JournalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class JournalServiceImpl implements JournalService {

    private final JournalRepository journalRepository;

    private final EntryRepository entryRepository;

    @Override
    public List<Journal> getAll() {
        return journalRepository.getAll();
    }

    @Override
    public Journal get(String journalId) {
        return journalRepository.getById(journalId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Journal not found"));
    }

    @Override
    public Journal save(Journal journal) {
        if (hasSameName(journal)) {
            throw new ApplicationException(HttpStatus.CONFLICT, "There is already another journal with the same name");
        }
        Journal saved;
        if (Objects.isNull(journal.getCurrentBalance())) {
            Balance balance = Balance.builder()
                    .accountBalance(journal.getStartBalance().setScale(2, RoundingMode.HALF_EVEN))
                    .taxes(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                    .withdrawals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                    .deposits(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                    .closedPositions(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                    .openedPositions(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                    .available(journal.getStartBalance().setScale(2, RoundingMode.HALF_EVEN))
                    .build();
            journal.setLastBalance(LocalDateTime.now());
            journal.setCurrentBalance(balance);
        }
        saved = journalRepository.save(journal);
        return saved;
    }

    @Override
    public void delete(String journalId) {
        Journal journal = get(journalId);
        Query query = new Query(Criteria.where("journalId").is(journalId));
        entryRepository.delete(query);
        journalRepository.delete(journal);

        long count = journalRepository.count();
        if (count == 0) {
            entryRepository.drop();
            journalRepository.drop();
        }
    }

    @Override
    public void updateBalance(String journalId, Balance balance) {
        Journal journal = get(journalId);
        journal.setCurrentBalance(balance);
        journalRepository.save(journal);
    }

    private boolean hasSameName(Journal journal) {
        Query query = Query.query(Criteria.where("name").is(journal.getName()).and("id").ne(journal.getId()));
        List<Journal> journals = journalRepository.find(query);
        return !journals.isEmpty();
    }
}
