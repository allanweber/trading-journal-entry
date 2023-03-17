package com.trading.journal.entry.strategy;

import com.trading.journal.entry.queries.MultiTenancyRepository;

public interface StrategyRepository  extends MultiTenancyRepository<Strategy, String> {
}
