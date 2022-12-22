package com.trading.journal.entry.queries;

import com.trading.journal.entry.queries.data.Filter;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface MultiTenancyRepository<T, I extends Serializable> extends MongoRepository<T, I> {

    List<T> getAll(CollectionName collectionName);

    List<T> query(CollectionName collectionName, List<Filter> filters);

    Optional<T> getById(CollectionName collectionName, I id);

    T save(CollectionName collectionName, T data);

    long delete(CollectionName collectionName, T data);

    boolean hasItems(CollectionName collectionName);

    void drop(CollectionName collectionName);

    long count(Query query, CollectionName collectionName);
}
