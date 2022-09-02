package com.trading.journal.entry.entries;

import com.trading.journal.entry.query.PageFilterRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntryRepository  extends PageFilterRepository<Entry, String> {
}
