package com.trading.journal.entry.queries.impl;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.queries.CollectionName;
import com.trading.journal.entry.queries.TokenRequestScope;
import com.trading.journal.entry.queries.WithFilterPageableRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.UpdateDefinition;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public class MultiTenancyPageableRepositoryImpl<T, I extends Serializable> extends SimpleMongoRepository<T, I> implements WithFilterPageableRepository<T, I> {
    public static final String PAGE_REQUEST_IS_REQUIRED = "Page request is required!";
    public static final String QUERY_IS_REQUIRED = "Query is required!";

    private final MongoEntityInformation<T, I> metadata;
    private final MongoOperations mongoOperations;

    public MultiTenancyPageableRepositoryImpl(MongoEntityInformation<T, I> metadata, MongoOperations mongoOperations) {
        super(metadata, mongoOperations);
        this.metadata = metadata;
        this.mongoOperations = mongoOperations;
    }

    @NonNull
    @Override
    public Page<T> findAll(@NonNull Pageable pageable) {
        return findAll(pageable, new Query());
    }

    @Override
    public Page<T> findAll(Pageable pageable, Query query) {
        Assert.notNull(pageable, PAGE_REQUEST_IS_REQUIRED);

        String collection = getCollectionName();
        long total = mongoOperations.count(query, metadata.getJavaType(), collection);
        List<T> content = mongoOperations.find(query.with(pageable), metadata.getJavaType(), collection);

        return new PageImpl<T>(content, pageable, total);
    }

    @Override
    public List<T> find(Query query) {
        Assert.notNull(query, QUERY_IS_REQUIRED);
        return mongoOperations.find(query, metadata.getJavaType(), getCollectionName());
    }

    @Override
    public List<T> getAll() {
        return mongoOperations.findAll(metadata.getJavaType(), getCollectionName());
    }

    @Override
    public Optional<T> getById(I id) {
        T byId = mongoOperations.findById(id, metadata.getJavaType(), getCollectionName());
        return Optional.ofNullable(byId);
    }

    @NonNull
    @Override
    public <S extends T> S save(@NonNull S data) {
        return mongoOperations.save(data, getCollectionName());
    }

    @Override
    public void delete(@NonNull T data) {
        mongoOperations.remove(data, getCollectionName());
    }

    @Override
    public long delete(Query query) {
        return mongoOperations.remove(query, getCollectionName()).getDeletedCount();
    }

    @Override
    public boolean hasItems() {
        return mongoOperations.count(new Query(), metadata.getJavaType(), getCollectionName()) > 0;
    }

    @Override
    public void drop() {
        mongoOperations.dropCollection(getCollectionName());
    }


    @Override
    public long count(Query query) {
        return mongoOperations.count(query, getCollectionName());
    }

    @Override
    public List<String> distinct(String field, Query query) {
        return mongoOperations.findDistinct(query, field, getCollectionName(), metadata.getJavaType(), String.class);
    }

    @Override
    public <U> List<U> aggregate(Aggregation aggregation, Class<U> clazz) {
        return mongoOperations.aggregate(aggregation, getCollectionName(), clazz).getMappedResults();
    }

    @Override
    public long update(Query query, UpdateDefinition update) {
        return mongoOperations.updateFirst(query, update, getCollectionName()).getModifiedCount();
    }

    private String getCollectionName() {
        AccessTokenInfo accessTokenInfo = TokenRequestScope.get();
        CollectionName collectionName = new CollectionName(accessTokenInfo);
        return collectionName.collectionName(metadata);
    }
}
