package com.trading.journal.entry.entries.impl;

import com.trading.journal.entry.ApplicationException;
import com.trading.journal.entry.balance.Balance;
import com.trading.journal.entry.balance.BalanceService;
import com.trading.journal.entry.entries.*;
import com.trading.journal.entry.journal.JournalService;
import com.trading.journal.entry.strategy.Strategy;
import com.trading.journal.entry.strategy.StrategyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RequiredArgsConstructor
@Service
@Slf4j
public class EntryServiceImpl implements EntryService {

    private final EntryRepository repository;

    private final JournalService journalService;

    private final BalanceService balanceService;

    private final StrategyService strategyService;

    @Override
    public Page<Entry> getAll(EntriesQuery entriesQuery) {
        Query query = entriesQuery.buildQuery();
        Page<Entry> page = repository.findAll(entriesQuery.getPageable(), query);
        List<Entry> entries = page.stream().map(this::loadStrategies).toList();
        return new PageImpl<>(entries, entriesQuery.getPageable(), page.getTotalElements());
    }

    @Override
    public Entry getById(String entryId) {
        Entry entry = get(entryId);
        return loadStrategies(entry);
    }

    @Override
    public Entry save(Entry entry) {
        List<Strategy> strategies = ofNullable(entry.getStrategyIds())
                .orElse(emptyList())
                .stream()
                .map(strategyId -> strategyService.getById(strategyId)
                        .orElseThrow(() -> new ApplicationException(HttpStatus.BAD_REQUEST, String.format("Invalid Strategy %s", strategyId)))
                ).toList();

        Balance balance = balanceService.getCurrentBalance(entry.getJournalId());
        CalculateEntry calculateEntry = new CalculateEntry(entry, balance.getAccountBalance());
        Entry calculated = calculateEntry.calculate();
        Entry saved = repository.save(calculated);
        if (saved.isFinished()) {
            balanceService.calculateCurrentBalance(entry.getJournalId());
        } else {
            balanceService.calculateAvailableBalance(entry.getJournalId());
        }
        if (!strategies.isEmpty()) {
            saved.setStrategies(strategies);
        }
        return saved;
    }

    @Override
    public void delete(String entryId) {
        Entry entry = get(entryId);
        repository.delete(entry);
        if (entry.isFinished()) {
            balanceService.calculateCurrentBalance(entry.getJournalId());
        }
    }

    @Override
    public void uploadImage(String entryId, UploadType type, MultipartFile file) {
        String base64File;
        try {
            base64File = Base64.getEncoder().encodeToString(file.getBytes());
        } catch (IOException e) {
            log.error("Error to base64 file {} ", file.getOriginalFilename(), e);
            throw (HttpClientErrorException) new HttpClientErrorException(INTERNAL_SERVER_ERROR, "There was an unexpected error to save the file.").initCause(e);
        }

        Entry entry = get(entryId);
        if (UploadType.IMAGE_BEFORE.equals(type)) {
            entry.setScreenshotBefore(base64File);
        } else {
            entry.setScreenshotAfter(base64File);
        }

        repository.save(entry);
    }

    @Override
    public EntryImageResponse returnImage(String entryId, UploadType type) {
        Entry entry = get(entryId);

        EntryImageResponse response;
        if (UploadType.IMAGE_BEFORE.equals(type)) {
            response = new EntryImageResponse(entry.getScreenshotBefore());
        } else {
            response = new EntryImageResponse(entry.getScreenshotAfter());
        }
        return response;
    }

    private Entry get(String entryId) {
        return repository.getById(entryId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Entry not found"));
    }

    private Entry loadStrategies(Entry entry) {
        List<Strategy> strategies = ofNullable(entry.getStrategyIds())
                .orElse(emptyList())
                .stream()
                .map(id ->
                        strategyService.getById(id).orElse(null)
                )
                .filter(Objects::nonNull).toList();
        if (!strategies.isEmpty()) {
            entry.setStrategies(strategies);
        }
        return entry;
    }
}
