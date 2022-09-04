package com.trading.journal.entry.entries;

import com.trading.journal.entry.query.WithFilterPageableRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntryPageableRepository extends WithFilterPageableRepository<Entry, String> {
}
