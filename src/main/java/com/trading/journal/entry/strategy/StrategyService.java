package com.trading.journal.entry.strategy;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface StrategyService {
    Page<Strategy> getAll(AccessTokenInfo accessToken, Pageable pageable);

    Strategy save(AccessTokenInfo accessToken, Strategy strategy);

    Optional<Strategy> getById(AccessTokenInfo accessToken, String strategyId);

    void delete(AccessTokenInfo accessToken, String strategyId);
}
