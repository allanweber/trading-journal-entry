package com.trading.journal.entry.queries;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface MultiTenancyRepository<T, I extends Serializable> extends MongoRepository<T, I> {

    List<T> getAll(CollectionName collectionName);

    Optional<T> getById(CollectionName collectionName, I id);

    T save(CollectionName collectionName, T data);

    long delete(CollectionName collectionName, T data);
}
