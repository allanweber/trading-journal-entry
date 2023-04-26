package com.trading.journal.entry.storage.impl;

import com.amazonaws.services.s3.AmazonS3Client;
import com.trading.journal.entry.configuration.properties.StorageProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class FileStorageBeanTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(new ConditionEvaluationReportLoggingListener())
            .withUserConfiguration(S3FileStorage.class, LocalFileStorage.class, AmazonS3Client.class, StorageProperties.class);

    @Test
    @DisplayName("Application properties has s3 option")
    void isS3() {
        contextRunner
                .withPropertyValues("journal.entries.storage.option:s3")
                .run(context -> {
                    assertThat(context).hasSingleBean(S3FileStorage.class);
                    assertThat(context).doesNotHaveBean(LocalFileStorage.class);
                });
    }

    @Test
    @DisplayName("Application properties has another option")
    void isLocal() {
        contextRunner
                .withPropertyValues("journal.entries.storage.option:local")
                .run(context -> {
                    assertThat(context).hasSingleBean(LocalFileStorage.class);
                    assertThat(context).doesNotHaveBean(S3FileStorage.class);
                });
    }

    @Test
    @DisplayName("Application properties has another option")
    void isLocalNoProperty() {
        contextRunner
                .run(context -> {
                    assertThat(context).hasSingleBean(LocalFileStorage.class);
                    assertThat(context).doesNotHaveBean(S3FileStorage.class);
                });
    }
}