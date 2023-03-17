package com.trading.journal.entry.strategy.impl;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.queries.CollectionName;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StrategyCollectionName {

    private final AccessTokenInfo accessTokenInfo;

    public CollectionName collectionName() {
        return new CollectionName(accessTokenInfo);
    }
}
