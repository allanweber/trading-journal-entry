package com.trading.journal.entry.hello;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface HelloRepository extends PagingAndSortingRepository<Hello, String> {
}
