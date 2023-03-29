package com.trading.journal.entry.queries.impl;

import com.trading.journal.entry.queries.CollectionName;
import com.trading.journal.entry.queries.WithFilterPageableRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public class MultiTenancyPageableRepositoryImpl<T, I extends Serializable> extends SimpleMongoRepository<T, I> implements WithFilterPageableRepository<T, I> {

    public static final String COLLECTION_NAME_IS_REQUIRED = "Collection Name is required!";
    public static final String PAGE_REQUEST_IS_REQUIRED = "Page request is required!";

    public static final String QUERY_IS_REQUIRED = "Query is required!";

    private final MongoEntityInformation<T, I> metadata;
    private final MongoOperations mongoOperations;

    public MultiTenancyPageableRepositoryImpl(MongoEntityInformation<T, I> metadata, MongoOperations mongoOperations) {
        super(metadata, mongoOperations);
        this.metadata = metadata;
        this.mongoOperations = mongoOperations;
    }

    @Override
    public Page<T> findAll(CollectionName collectionName, Pageable pageable) {
        return findAll(collectionName, pageable, new Query());
    }

    @Override
    public Page<T> findAll(CollectionName collectionName, Pageable pageable, Query query) {
        Assert.notNull(collectionName, COLLECTION_NAME_IS_REQUIRED);
        Assert.notNull(pageable, PAGE_REQUEST_IS_REQUIRED);
        long total = mongoOperations.count(query, metadata.getJavaType(), collectionName.collectionName(metadata));
        List<T> content = mongoOperations.find(query.with(pageable), metadata.getJavaType(), collectionName.collectionName(metadata));

        return new PageImpl<T>(content, pageable, total);
    }

    @Override
    public List<T> find(CollectionName collectionName, Query query) {
        Assert.notNull(collectionName, COLLECTION_NAME_IS_REQUIRED);
        Assert.notNull(query, QUERY_IS_REQUIRED);
        return mongoOperations.find(query, metadata.getJavaType(), collectionName.collectionName(metadata));
    }

    @Override
    public List<T> getAll(CollectionName collectionName) {
        Assert.notNull(collectionName, COLLECTION_NAME_IS_REQUIRED);
        return mongoOperations.findAll(metadata.getJavaType(), collectionName.collectionName(metadata));
    }

    @Override
    public Optional<T> getById(CollectionName collectionName, I id) {
        Assert.notNull(collectionName, COLLECTION_NAME_IS_REQUIRED);
        T byId = mongoOperations.findById(id, metadata.getJavaType(), collectionName.collectionName(metadata));
        return Optional.ofNullable(byId);
    }

    @Override
    public T save(CollectionName collectionName, T data) {
        Assert.notNull(collectionName, COLLECTION_NAME_IS_REQUIRED);
        return mongoOperations.save(data, collectionName.collectionName(metadata));
    }

    @Override
    public long delete(CollectionName collectionName, T data) {
        Assert.notNull(collectionName, COLLECTION_NAME_IS_REQUIRED);
        return mongoOperations.remove(data, collectionName.collectionName(metadata)).getDeletedCount();
    }

    @Override
    public boolean hasItems(CollectionName collectionName) {
        Assert.notNull(collectionName, COLLECTION_NAME_IS_REQUIRED);
        return mongoOperations.count(new Query(), metadata.getJavaType(), collectionName.collectionName(metadata)) > 0;
    }

    @Override
    public void drop(CollectionName collectionName) {
        Assert.notNull(collectionName, COLLECTION_NAME_IS_REQUIRED);
        mongoOperations.dropCollection(collectionName.collectionName(metadata));
    }

    @Override
    public long count(Query query, CollectionName collectionName) {
        Assert.notNull(collectionName, COLLECTION_NAME_IS_REQUIRED);
        return mongoOperations.count(query, collectionName.collectionName(metadata));
    }

    @Override
    public List<String> distinct(String field, CollectionName collectionName) {
        Assert.notNull(collectionName, COLLECTION_NAME_IS_REQUIRED);
        return mongoOperations.findDistinct(new Query(), field, collectionName.collectionName(metadata), metadata.getJavaType(), String.class);
    }

    @Override
    public <U> List<U> aggregate(Aggregation aggregation, CollectionName collectionName, Class<U> clazz) {
        Assert.notNull(collectionName, COLLECTION_NAME_IS_REQUIRED);
        return mongoOperations.aggregate(aggregation, collectionName.collectionName(metadata), clazz).getMappedResults();
    }
}
