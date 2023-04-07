package com.trading.journal.entry.strategy.impl;

import com.trading.journal.entry.ApplicationException;
import com.trading.journal.entry.strategy.Strategy;
import com.trading.journal.entry.strategy.StrategyRepository;
import com.trading.journal.entry.strategy.StrategyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StrategyServiceImpl implements StrategyService {

    private final StrategyRepository strategyRepository;

    @Override
    public Page<Strategy> getAll(Pageable pageable) {
        return strategyRepository.findAll(pageable);
    }

    @Override
    public Strategy save(Strategy strategy) {
        return strategyRepository.save(strategy);
    }

    @Override
    public Optional<Strategy> getById(String strategyId) {
        return strategyRepository.getById(strategyId);
    }

    @Override
    public void delete(String strategyId) {
        Strategy strategy = getById(strategyId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Strategy not found"));
        strategyRepository.delete(strategy);

        boolean hasItems = strategyRepository.hasItems();
        if (!hasItems) {
            strategyRepository.drop();
        }
    }
}
