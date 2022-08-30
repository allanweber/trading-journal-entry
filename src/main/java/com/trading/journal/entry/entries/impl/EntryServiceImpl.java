package com.trading.journal.entry.entries.impl;

import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.EntryService;
import com.trading.journal.entry.pageable.Filter;
import com.trading.journal.entry.pageable.PageResponse;
import com.trading.journal.entry.pageable.PageableRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiredArgsConstructor
@Service
public class EntryServiceImpl implements EntryService {

    private final MongoTemplate repository;

    @Override
    public PageResponse<Entry> getAll(String tenancy, PageableRequest pageRequest) {
        Criteria criteria = Criteria.where("id").exists(true);
        if (pageRequest.hasFilter()) {
            for (Filter filter : pageRequest.filters()) {
                if ("date".equals(filter.field())) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime startDate = LocalDateTime.parse(filter.value().concat(" 00:00:00"), formatter);
                    LocalDateTime endDate = LocalDateTime.parse(filter.value().concat(" 23:59:59"), formatter);
                    criteria.and(filter.field()).gte(startDate).lt(endDate);
                } else {
                    criteria.and(filter.field()).is(filter.value());
                }
            }
        }
        List<Entry> items = repository.find(new Query(criteria).with(pageRequest.pageable()), Entry.class, tenancy);
        long total = repository.count(new Query(criteria), Entry.class, tenancy);
        return new PageResponse<>(pageRequest, total, items);
    }

    @Override
    public Entry create(String tenancy, Entry entry) {
        return repository.save(entry, tenancy);
    }
}
