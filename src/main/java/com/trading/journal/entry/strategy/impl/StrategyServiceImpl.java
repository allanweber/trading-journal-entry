package com.trading.journal.entry.strategy.impl;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.ApplicationException;
import com.trading.journal.entry.queries.CollectionName;
import com.trading.journal.entry.queries.data.PageResponse;
import com.trading.journal.entry.queries.data.PageableRequest;
import com.trading.journal.entry.strategy.Strategy;
import com.trading.journal.entry.strategy.StrategyRepository;
import com.trading.journal.entry.strategy.StrategyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StrategyServiceImpl implements StrategyService {

    private final StrategyRepository strategyRepository;

    @Override
    public PageResponse<Strategy> getAll(AccessTokenInfo accessToken, int page, int size) {
        CollectionName collectionName = new StrategyCollectionName(accessToken).collectionName();
        PageableRequest request = PageableRequest.builder().page(page).size(size).build();
        Page<Strategy> strategies = strategyRepository.findAll(collectionName, request);
        return new PageResponse<>(strategies);
    }

    @Override
    public Strategy save(AccessTokenInfo accessToken, Strategy strategy) {
        CollectionName collectionName = new StrategyCollectionName(accessToken).collectionName();
        return strategyRepository.save(collectionName, strategy);
    }

    @Override
    public Optional<Strategy> getById(AccessTokenInfo accessToken, String strategyId) {
        CollectionName collectionName = new StrategyCollectionName(accessToken).collectionName();
        return strategyRepository.getById(collectionName, strategyId);
    }

    @Override
    public void delete(AccessTokenInfo accessToken, String strategyId) {
        CollectionName collectionName = new StrategyCollectionName(accessToken).collectionName();
        Strategy strategy = getById(accessToken, strategyId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Strategy not found"));
        strategyRepository.delete(collectionName, strategy);

        boolean hasItems = strategyRepository.hasItems(collectionName);
        if (!hasItems) {
            strategyRepository.drop(collectionName);
        }
    }
}
