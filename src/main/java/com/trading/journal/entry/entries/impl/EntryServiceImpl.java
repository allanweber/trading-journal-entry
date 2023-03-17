package com.trading.journal.entry.entries.impl;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.ApplicationException;
import com.trading.journal.entry.balance.Balance;
import com.trading.journal.entry.balance.BalanceService;
import com.trading.journal.entry.entries.*;
import com.trading.journal.entry.journal.Journal;
import com.trading.journal.entry.journal.JournalService;
import com.trading.journal.entry.queries.CollectionName;
import com.trading.journal.entry.queries.data.PageableRequest;
import com.trading.journal.entry.strategy.Strategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.function.BiFunction;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RequiredArgsConstructor
@Service
@Slf4j
public class EntryServiceImpl implements EntryService {

    private final EntryRepository repository;

    private final JournalService journalService;

    private final BalanceService balanceService;

    private final EntryStrategyService entryStrategyService;

    @Override
    public List<Entry> getAll(EntriesQuery entriesQuery) {
        CollectionName collectionName = collectionName().apply(entriesQuery.getAccessTokenInfo(), entriesQuery.getJournalId());
        PageableRequest pageableRequest = PageableRequest.builder()
                .page(0)
                .size(Integer.MAX_VALUE)
                .sort(Sort.by(entriesQuery.sortBy()).ascending())
                .build();
        Query query = entriesQuery.buildQuery();
        Page<Entry> entries = repository.findAll(collectionName, pageableRequest, query);
        return entries.stream().toList();
    }

    @Override
    public Entry save(AccessTokenInfo accessToken, String journalId, Entry entry) {
        Balance balance = balanceService.getCurrentBalance(accessToken, journalId);

        List<Strategy> strategies = entryStrategyService.saveStrategy(accessToken, entry);
        if(!strategies.isEmpty()){
            entry.setStrategies(strategies);
        }

        CalculateEntry calculateEntry = new CalculateEntry(entry, balance.getAccountBalance());
        Entry calculated = calculateEntry.calculate();
        CollectionName entriesCollection = collectionName().apply(accessToken, journalId);
        Entry saved = repository.save(entriesCollection, calculated);
        if (saved.isFinished()) {
            balanceService.calculateCurrentBalance(accessToken, journalId);
        } else {
            balanceService.calculateAvailableBalance(accessToken, journalId);
        }
        return saved;
    }

    @Override
    public Entry getById(AccessTokenInfo accessToken, String journalId, String entryId) {
        CollectionName entriesCollection = collectionName().apply(accessToken, journalId);
        return get(entriesCollection, entryId);
    }

    @Override
    public void delete(AccessTokenInfo accessToken, String journalId, String entryId) {
        CollectionName entriesCollection = collectionName().apply(accessToken, journalId);
        Entry entry = get(entriesCollection, entryId);
        repository.delete(entriesCollection, entry);
        if (entry.isFinished()) {
            balanceService.calculateCurrentBalance(accessToken, journalId);
        }
    }

    @Override
    public void uploadImage(AccessTokenInfo accessToken, String journalId, String entryId, UploadType type, MultipartFile file) {
        String base64File;
        try {
            base64File = Base64.getEncoder().encodeToString(file.getBytes());
        } catch (IOException e) {
            log.error("Error to base64 file {} ", file.getOriginalFilename(), e);
            throw (HttpClientErrorException) new HttpClientErrorException(INTERNAL_SERVER_ERROR, "There was an unexpected error to save the file.").initCause(e);
        }

        CollectionName entriesCollection = collectionName().apply(accessToken, journalId);
        Entry entry = get(entriesCollection, entryId);
        if (UploadType.IMAGE_BEFORE.equals(type)) {
            entry.setScreenshotBefore(base64File);
        } else {
            entry.setScreenshotAfter(base64File);
        }

        repository.save(entriesCollection, entry);
    }

    @Override
    public EntryImageResponse returnImage(AccessTokenInfo accessToken, String journalId, String entryId, UploadType type) {
        CollectionName entriesCollection = collectionName().apply(accessToken, journalId);
        Entry entry = get(entriesCollection, entryId);

        EntryImageResponse response;
        if (UploadType.IMAGE_BEFORE.equals(type)) {
            response = new EntryImageResponse(entry.getScreenshotBefore());
        } else {
            response = new EntryImageResponse(entry.getScreenshotAfter());
        }
        return response;
    }

    private Entry get(CollectionName collectionName, String entryId) {
        return repository.getById(collectionName, entryId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Entry not found"));
    }

    private BiFunction<AccessTokenInfo, String, CollectionName> collectionName() {
        return (accessTokenInfo, journalId) -> {
            Journal journal = journalService.get(accessTokenInfo, journalId);
            return new CollectionName(accessTokenInfo, journal.getName());
        };
    }
}
