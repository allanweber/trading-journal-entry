package com.trading.journal.entry.query.impl;

import com.trading.journal.entry.query.PageFilterRepository;
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

public class PageFilterRepositoryImpl<T, I extends Serializable> extends SimpleMongoRepository<T, I> implements PageFilterRepository<T, I> {

    private final MongoEntityInformation<T, I> metadata;
    private final MongoOperations mongoOperations;

    /**
     * Creates a new {@link SimpleMongoRepository} for the given {@link MongoEntityInformation}
     *
     * @param metadata        must not be {@literal null}.
     * @param mongoOperations must not be {@literal null}.
     */
    public PageFilterRepositoryImpl(MongoEntityInformation<T, I> metadata, MongoOperations mongoOperations) {
        super(metadata, mongoOperations);
        this.metadata = metadata;
        this.mongoOperations = mongoOperations;
    }

    @Override
    public Page<T> findAll(String collectionName, Query query, Pageable pageable) {
        Assert.notNull(query, "Query is required!");
        Assert.notNull(pageable, "Pageable is required!");

        long total = mongoOperations.count(query, metadata.getJavaType(), collectionName);
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
