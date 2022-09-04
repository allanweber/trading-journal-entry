package com.trading.journal.entry.query.impl;

import com.trading.journal.entry.query.WithFilterPageableRepository;
import com.trading.journal.entry.query.PageableRequest;
import com.trading.journal.entry.query.QueryCriteriaBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.List;

public class WithFilterPageableRepositoryImpl<T, I extends Serializable> extends SimpleMongoRepository<T, I> implements WithFilterPageableRepository<T, I> {

    private final MongoEntityInformation<T, I> metadata;
    private final MongoOperations mongoOperations;

    private final QueryCriteriaBuilder<T> queryBuilder;

    public WithFilterPageableRepositoryImpl(MongoEntityInformation<T, I> metadata, MongoOperations mongoOperations) {
        super(metadata, mongoOperations);
        this.metadata = metadata;
        this.mongoOperations = mongoOperations;
        this.queryBuilder = new QueryCriteriaBuilder<>(metadata.getJavaType());
    }

    @Override
    public Page<T> findAll(String collectionName, PageableRequest pageRequest) {
        Assert.notNull(collectionName, "Collection name is required!");
        Assert.notNull(pageRequest, "Page request is required!");

        Query query = queryBuilder.buildQuery(pageRequest.getFilters());

        long total = mongoOperations.count(query, metadata.getJavaType(), collectionName);

        Pageable pageable = pageRequest.pageable();
        List<T> content = mongoOperations.find(query.with(pageable), metadata.getJavaType(), collectionName);

        return new PageImpl<T>(content, pageable, total);
    }

    @Override
    public T save(String collectionName, T data) {
        return mongoOperations.save(data, collectionName);
    }

    @Override
    public long delete(String collectionName, T data) {
        return mongoOperations.remove(data, collectionName).getDeletedCount();
    }
}
