package com.trading.journal.entry.entries.impl;

import com.trading.journal.entry.query.QueryCriteriaBuilder;
import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.EntryRepository;
import com.trading.journal.entry.entries.EntryService;
import com.trading.journal.entry.query.data.PageResponse;
import com.trading.journal.entry.query.PageableRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class EntryServiceImpl implements EntryService {

    private final EntryRepository repository;
    private final QueryCriteriaBuilder<Entry> queryBuilder;

    public EntryServiceImpl(EntryRepository repository) {
        this.queryBuilder = new QueryCriteriaBuilder<>(Entry.class);
        this.repository = repository;
    }

    @Override
    public PageResponse<Entry> getAll(String tenancy, PageableRequest pageRequest) {
//        Criteria criteria = Criteria.where("id").exists(true);
//        if (pageRequest.hasFilter()) {
//            for (Filter filter : pageRequest.getFilters()) {
//                if ("date".equals(filter.field())) {
//                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//                    LocalDateTime startDate = LocalDateTime.parse(filter.value().concat(" 00:00:00"), formatter);
//                    LocalDateTime endDate = LocalDateTime.parse(filter.value().concat(" 23:59:59"), formatter);
//                    criteria.and(filter.field()).gte(startDate).lt(endDate);
//                } else {
//                    criteria.and(filter.field()).is(filter.value());
//                }
//            }
//        }
        Query query = queryBuilder.buildQuery(pageRequest.getFilters());
        Page<Entry> page = repository.findAll(tenancy, query, pageRequest.pageable());
        return new PageResponse<>(page);
    }

    @Override
    public Entry create(String tenancy, Entry entry) {
        return repository.save(tenancy, entry);
    }
}
