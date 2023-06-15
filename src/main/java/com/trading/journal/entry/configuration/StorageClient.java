package com.trading.journal.entry.configuration;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.trading.journal.entry.configuration.properties.StorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class StorageClient {

    private final StorageProperties properties;

    @ConditionalOnProperty(prefix = "journal.entries.storage", name = "option", havingValue = "s3")
    @Bean
    public AmazonS3 s3Client() {
        ClientConfiguration clientConfig = new ClientConfiguration()
                .withRequestTimeout(5000)
                .withTcpKeepAlive(true);

        AWSCredentials credentials = new BasicAWSCredentials(
                properties.getAccessKey(),
                properties.getSecret()
        );

        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(properties.getEndpoint(), properties.getLocation()))
                .withPathStyleAccessEnabled(true)
                .withClientConfiguration(clientConfig)
                .build();
    }
}
