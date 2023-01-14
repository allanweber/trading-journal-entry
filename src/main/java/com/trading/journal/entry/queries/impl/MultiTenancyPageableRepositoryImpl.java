package com.trading.journal.entry.queries.impl;

import com.trading.journal.entry.queries.CollectionName;
import com.trading.journal.entry.queries.QueryCriteriaBuilder;
import com.trading.journal.entry.queries.WithFilterPageableRepository;
import com.trading.journal.entry.queries.data.Filter;
import com.trading.journal.entry.queries.data.PageableRequest;
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

    public static final String FILTER_IS_REQUIRED = "Filters are required!";
    private final MongoEntityInformation<T, I> metadata;
    private final MongoOperations mongoOperations;

    private final QueryCriteriaBuilder<T> queryBuilder;

    public MultiTenancyPageableRepositoryImpl(MongoEntityInformation<T, I> metadata, MongoOperations mongoOperations) {
        super(metadata, mongoOperations);
        this.metadata = metadata;
        this.mongoOperations = mongoOperations;
        this.queryBuilder = new QueryCriteriaBuilder<>(metadata.getJavaType());
    }

    @Override
    public Page<T> findAll(CollectionName collectionName, PageableRequest pageRequest) {
        Assert.notNull(collectionName, COLLECTION_NAME_IS_REQUIRED);
        Assert.notNull(pageRequest, PAGE_REQUEST_IS_REQUIRED);

        Query query = queryBuilder.buildQuery(pageRequest.getFilters());

        long total = mongoOperations.count(query, metadata.getJavaType(), collectionName.collectionName(metadata));

        Pageable pageable = pageRequest.pageable();
        List<T> content = mongoOperations.find(query.with(pageable), metadata.getJavaType(), collectionName.collectionName(metadata));

        return new PageImpl<T>(content, pageable, total);
    }

    @Override
    public List<T> getAll(CollectionName collectionName) {
        Assert.notNull(collectionName, COLLECTION_NAME_IS_REQUIRED);
        return mongoOperations.findAll(metadata.getJavaType(), collectionName.collectionName(metadata));
    }

    @Override
    public List<T> query(CollectionName collectionName, List<Filter> filters) {
        Assert.notNull(collectionName, COLLECTION_NAME_IS_REQUIRED);
        Assert.notNull(filters, FILTER_IS_REQUIRED);
        Query query = queryBuilder.buildQuery(filters);
        return mongoOperations.find(query, metadata.getJavaType(), collectionName.collectionName(metadata));
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
