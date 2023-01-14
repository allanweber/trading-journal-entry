package com.trading.journal.entry.queries.impl;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.mongodb.client.result.DeleteResult;
import com.trading.journal.entry.EntryForTest;
import com.trading.journal.entry.queries.CollectionName;
import com.trading.journal.entry.queries.data.PageableRequest;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.bson.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class MultiTenancyPageableRepositoryImplTest {

    private static CollectionName collectionName;

    private static String collection;

    @Mock
    MongoEntityInformation<EntryForTest, String> metadata;

    @Mock
    MongoOperations mongoOperations;

    MultiTenancyPageableRepositoryImpl<EntryForTest, String> repository;

    @BeforeAll
    static void setUp() {
        collectionName = new CollectionName(new AccessTokenInfo("subject", 1L, "TENANCY", emptyList()), "my-journal");
        collection = "Tenancy_my-journal_entries";
    }

    @BeforeEach
    public void setUpMetadata() {
        when(metadata.getCollectionName()).thenReturn("entries");
        when(metadata.getJavaType()).thenReturn(EntryForTest.class);
        repository = new MultiTenancyPageableRepositoryImpl<>(metadata, mongoOperations);
    }

    @DisplayName("Find all using queries")
    @Test
    void findAll() {
        PageableRequest pageableRequest = PageableRequest.builder().page(1).build();
        when(mongoOperations.count(any(), eq(EntryForTest.class), eq(collection))).thenReturn(1L);
        when(mongoOperations.find(any(), eq(EntryForTest.class), eq(collection))).thenReturn(singletonList(new EntryForTest()));

        Page<EntryForTest> all = repository.findAll(collectionName, pageableRequest);
        assertThat(all.get()).isNotEmpty();
    }

    @DisplayName("Find all")
    @Test
    void findAllSimple() {
        when(mongoOperations.findAll(EntryForTest.class, collection)).thenReturn(singletonList(new EntryForTest()));

        List<EntryForTest> all = repository.getAll(collectionName);
        assertThat(all).isNotEmpty();
    }

    @DisplayName("Get by id")
    @Test
    void getById() {
        when(mongoOperations.findById("123", EntryForTest.class, collection)).thenReturn(new EntryForTest());

        Optional<EntryForTest> byId = repository.getById(collectionName, "123");
        assertThat(byId).isNotEmpty();
    }

    @DisplayName("Save")
    @Test
    void save() {
        EntryForTest save = new EntryForTest();
        when(mongoOperations.save(save, collection)).thenReturn(save);

        EntryForTest saved = repository.save(collectionName, save);
        assertThat(saved).isNotNull();
    }

    @DisplayName("Delete")
    @Test
    void delete() {
        EntryForTest delete = new EntryForTest();
        when(mongoOperations.remove(delete, collection)).thenReturn(DeleteResult.acknowledged(1L));

        long count = repository.delete(collectionName, delete);
        assertThat(count).isPositive();
    }

    @DisplayName("Has items")
    @Test
    void hasItems() {
        when(mongoOperations.count(any(), eq(EntryForTest.class), eq(collection))).thenReturn(1L);

        boolean hasItems = repository.hasItems(collectionName);
        assertThat(hasItems).isTrue();
    }

    @DisplayName("Has no items")
    @Test
    void hasNoItems() {
        when(mongoOperations.count(any(), eq(EntryForTest.class), eq(collection))).thenReturn(0L);

        boolean hasItems = repository.hasItems(collectionName);
        assertThat(hasItems).isFalse();
    }

    @DisplayName("Drop collection")
    @Test
    void drop() {
        repository.drop(collectionName);
        verify(mongoOperations).dropCollection(collection);
    }

    @DisplayName("Count by query")
    @Test
    void count() {
        when(mongoOperations.count(any(), eq(collection))).thenReturn(1L);
        long count = repository.count(new Query(), collectionName);
        assertThat(count).isEqualTo(1L);
    }

    @DisplayName("Find distinct")
    @Test
    void distinctQuery() {
        when(mongoOperations.findDistinct(any(), eq("a"), eq(collection), any(), eq(String.class)))
                .thenReturn(asList("A", "b"));
        List<String> distinct = repository.distinct("a", collectionName);
        assertThat(distinct).hasSize(2);
    }

    @DisplayName("Aggregation")
    @Test
    void aggregation() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.project("a")
        );
        Document document = Document.parse("{}");

        when(mongoOperations.aggregate(aggregation, collection, Aggregated.class))
                .thenReturn(new AggregationResults<>(asList(new Aggregated("1", "2"), new Aggregated("1", "2")), document));

        List<Aggregated> list = repository.aggregate(aggregation, collectionName, Aggregated.class);
        assertThat(list).hasSize(2);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class Aggregated {
        private String field;
        private String other;
    }
}