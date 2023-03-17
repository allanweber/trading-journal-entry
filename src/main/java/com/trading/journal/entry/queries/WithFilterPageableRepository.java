package com.trading.journal.entry.queries;

import com.trading.journal.entry.queries.data.PageableRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface WithFilterPageableRepository<T, I extends Serializable> extends MultiTenancyRepository<T, I> {

    Page<T> findAll(CollectionName collectionName, PageableRequest pageRequest);
    Page<T> findAll(CollectionName collectionName, PageableRequest pageRequest, Query query);
}
