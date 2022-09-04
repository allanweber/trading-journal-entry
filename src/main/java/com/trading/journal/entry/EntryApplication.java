package com.trading.journal.entry;

import com.allanweber.jwttoken.data.JwtProperties;
import com.trading.journal.entry.query.impl.WithFilterPageableRepositoryImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
@EnableMongoRepositories(repositoryBaseClass = WithFilterPageableRepositoryImpl.class)
public class EntryApplication {

    public static void main(String[] args) {
        SpringApplication.run(EntryApplication.class, args);
    }

}
