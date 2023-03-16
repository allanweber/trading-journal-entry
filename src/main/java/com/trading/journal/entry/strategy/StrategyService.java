package com.trading.journal.entry.strategy;

import com.allanweber.jwttoken.data.AccessTokenInfo;

import java.util.List;
import java.util.Optional;

public interface StrategyService {
    List<Strategy> getAll(AccessTokenInfo accessToken);

    Strategy save(AccessTokenInfo accessToken, Strategy strategy);

    Optional<Strategy> getById(AccessTokenInfo accessToken, String strategyId);

    void delete(AccessTokenInfo accessToken, String strategyId);
}
