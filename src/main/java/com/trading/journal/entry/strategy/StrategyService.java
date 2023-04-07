package com.trading.journal.entry.strategy;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface StrategyService {
    Page<Strategy> getAll(Pageable pageable);

    Strategy save(Strategy strategy);

    Optional<Strategy> getById(String strategyId);

    void delete(String strategyId);
}
