package com.trading.journal.entry.entries;

import com.trading.journal.entry.queries.WithFilterPageableRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntryRepository extends WithFilterPageableRepository<Entry, String> {
}
