package com.trading.journal.entry.journal.impl;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.ApplicationException;
import com.trading.journal.entry.balance.Balance;
import com.trading.journal.entry.journal.Journal;
import com.trading.journal.entry.journal.JournalRepository;
import com.trading.journal.entry.journal.JournalService;
import com.trading.journal.entry.queries.CollectionName;
import com.trading.journal.entry.queries.data.Filter;
import com.trading.journal.entry.queries.data.FilterOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static java.util.Arrays.asList;

@RequiredArgsConstructor
@Service
public class JournalServiceImpl implements JournalService {

    private final JournalRepository journalRepository;

    private final MongoOperations mongoOperations;

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
        if (hasSameName(accessToken, journal)) {
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
        saved = journalRepository.save(new CollectionName(accessToken), journal);
        return saved;
    }

    @Override
    public long delete(AccessTokenInfo accessToken, String journalId) {
        Journal journal = get(accessToken, journalId);
        String entryCollectionName = new CollectionName(accessToken, journal.getName()).collectionName("entries");
        mongoOperations.dropCollection(entryCollectionName);
        CollectionName journalCollection = new CollectionName(accessToken);
        long deleted = journalRepository.delete(journalCollection, journal);
        if (!journalRepository.hasItems(journalCollection)) {
            journalRepository.drop(journalCollection);
        }
        return deleted;
    }

    @Override
    public void updateBalance(AccessTokenInfo accessToken, String journalId, Balance balance) {
        Journal journal = get(accessToken, journalId);
        journal.setCurrentBalance(balance);
        journalRepository.save(new CollectionName(accessToken), journal);
    }

    private boolean hasSameName(AccessTokenInfo accessToken, Journal journal) {
        List<Filter> filters = asList(
                Filter.builder().field("name").operation(FilterOperation.EQUAL).value(journal.getName()).build(),
                Filter.builder().field("id").operation(FilterOperation.NOT_EQUAL).value(journal.getId()).build()
        );
        List<Journal> query = journalRepository.query(new CollectionName(accessToken), filters);
        return !query.isEmpty();
    }
}
