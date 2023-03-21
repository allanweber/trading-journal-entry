package com.trading.journal.entry.strategy;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.queries.data.PageResponse;

import java.util.Optional;

public interface StrategyService {
    PageResponse<Strategy> getAll(AccessTokenInfo accessToken, int page, int size);

    Strategy save(AccessTokenInfo accessToken, Strategy strategy);

    Optional<Strategy> getById(AccessTokenInfo accessToken, String strategyId);

    void delete(AccessTokenInfo accessToken, String strategyId);
}
