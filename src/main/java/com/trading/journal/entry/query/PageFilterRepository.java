package com.trading.journal.entry.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface PageFilterRepository<T, I extends Serializable> extends MongoRepository<T, I> {
    Page<T> findAll(String collectionName, Query query, Pageable pageable);

    T save(String collectionName, T data);

    long delete(String collectionName, T data);
}
