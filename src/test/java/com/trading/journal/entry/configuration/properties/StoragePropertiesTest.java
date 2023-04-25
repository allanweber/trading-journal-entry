package com.trading.journal.entry.configuration.properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@EnableConfigurationProperties(StorageProperties.class)
@PropertySource(value = "application.properties")
@ExtendWith(SpringExtension.class)
class StoragePropertiesTest {

    @Autowired
    StorageProperties properties;

    @DisplayName("Storage properties are filled")
    @Test
    void storage() {
        assertThat(properties.getAccessKey()).isEqualTo("123456789");
        assertThat(properties.getSecret()).isEqualTo("storeme");
        assertThat(properties.getEndpoint()).isEqualTo("http://store.com");
        assertThat(properties.getLocation()).isEqualTo("here");
    }
}