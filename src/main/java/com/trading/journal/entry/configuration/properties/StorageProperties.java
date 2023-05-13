package com.trading.journal.entry.configuration.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties("journal.entries.storage")
@Configuration
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StorageProperties {
    private String accessKey;

    private String secret;

    private String endpoint;

    private String location;

    private String cdn;
}
