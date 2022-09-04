package com.trading.journal.entry.query;

import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface WithFilterPageableRepository<T, I extends Serializable> extends MongoRepository<T, I> {
    Page<T> findAll(String collectionName, PageableRequest pageRequest);

    T save(String collectionName, T data);

    long delete(String collectionName, T data);
}
