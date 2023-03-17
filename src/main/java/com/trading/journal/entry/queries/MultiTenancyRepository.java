package com.trading.journal.entry.queries;

import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface MultiTenancyRepository<T, I extends Serializable> extends MongoRepository<T, I> {

    List<T> find(CollectionName collectionName, Query query);

    List<T> getAll(CollectionName collectionName);

    Optional<T> getById(CollectionName collectionName, I id);

    T save(CollectionName collectionName, T data);

    long delete(CollectionName collectionName, T data);

    boolean hasItems(CollectionName collectionName);

    void drop(CollectionName collectionName);

    long count(Query query, CollectionName collectionName);

    List<String> distinct(String field, CollectionName collectionName);

    <U> List<U> aggregate(Aggregation aggregation, CollectionName collectionName, Class<U> clazz);
}
