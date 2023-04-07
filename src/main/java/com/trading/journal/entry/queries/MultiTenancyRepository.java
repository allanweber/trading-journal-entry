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

    List<T> find(Query query);

    List<T> getAll();

    Optional<T> getById(I id);

    boolean hasItems();

    void drop();

    long count(Query query);

    long delete(Query query);

    List<String> distinct(String field, Query query);

    <U> List<U> aggregate(Aggregation aggregation, Class<U> clazz);
}
