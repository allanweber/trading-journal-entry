package com.trading.journal.entry.entries.impl;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.strategy.Strategy;
import com.trading.journal.entry.strategy.StrategyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@Service
@RequiredArgsConstructor
public class EntryStrategyService {

    private final StrategyService strategyService;

    public List<Strategy> saveStrategy(AccessTokenInfo accessToken, Entry entry) {
        return Optional.ofNullable(entry.getStrategies())
                .map(strategies -> strategies.stream()
                        .map(strategy -> {
                            if (Objects.nonNull(strategy.getId())) {
                                return strategyService.getById(accessToken, strategy.getId())
                                        .orElseGet(save(accessToken, strategy));
                            } else {
                                return save(accessToken, strategy).get();
                            }
                        })
                        .collect(Collectors.toList())
                ).orElse(emptyList());
    }

    private Supplier<Strategy> save(AccessTokenInfo accessToken, Strategy strategy) {
        return () -> strategyService.save(accessToken, strategy);
    }
}
