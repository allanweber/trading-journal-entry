package com.trading.journal.entry.queries;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CollectionNameTest {

    @DisplayName("Built name with tenancy name and entity name")
    @Test
    void name() {
        MongoEntityInformation<?, ?> entityInformation = mock(MongoEntityInformation.class);
        when(entityInformation.getCollectionName()).thenReturn("collection");

        AccessTokenInfo accessToken = new AccessTokenInfo("subject", 1L, "TENANCY", emptyList());

        CollectionName collectionName = new CollectionName(accessToken);

        assertThat(collectionName.collectionName(entityInformation)).isEqualTo("Tenancy_collection");
    }

    @DisplayName("Built name with tenancy name, middle name and entity name")
    @Test
    void middleName() {
        MongoEntityInformation<?, ?> entityInformation = mock(MongoEntityInformation.class);
        when(entityInformation.getCollectionName()).thenReturn("collection");

        AccessTokenInfo accessToken = new AccessTokenInfo("subject", 1L, "TENANCY", emptyList());

        CollectionName collectionName = new CollectionName(accessToken, "middle.name");

        assertThat(collectionName.collectionName(entityInformation)).isEqualTo("Tenancy_middle.name_collection");
    }

    @DisplayName("Built name with tenancy name, middle name with spaces and entity name")
    @Test
    void middleNameNoSpaces() {
        MongoEntityInformation<?, ?> entityInformation = mock(MongoEntityInformation.class);
        when(entityInformation.getCollectionName()).thenReturn("collection");

        AccessTokenInfo accessToken = new AccessTokenInfo("subject", 1L, "TENANCY", emptyList());

        CollectionName collectionName = new CollectionName(accessToken, " middle . na me ");

        assertThat(collectionName.collectionName(entityInformation)).isEqualTo("Tenancy_middle.name_collection");
    }
}