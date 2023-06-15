package com.trading.journal.entry.configuration;

import com.amazonaws.services.s3.AmazonS3;
import com.trading.journal.entry.configuration.properties.StorageProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class StorageClientTest {

    @Test
    void s3Client() {
        StorageProperties properties = new StorageProperties("key", "secret", "http://localhost:9000", "us-east-1", "http://localhost:9000");
        StorageClient storageClient = new StorageClient(properties);

        AmazonS3 client = storageClient.s3Client();
        assertThat(client).isNotNull();
        assertThat(client.getRegionName()).isEqualTo("us-east-1");
    }
}