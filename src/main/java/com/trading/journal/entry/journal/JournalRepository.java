package com.trading.journal.entry.journal;

import com.trading.journal.entry.queries.MultiTenancyRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JournalRepository extends MultiTenancyRepository<Journal, String> {
}
