package com.trading.journal.entry;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.MongoDBContainer;

import static java.lang.String.format;

public class MongoDbContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    protected static final MongoDBContainer container;

    static {
        container = new MongoDBContainer("mongo:5.0.7");
        container.start();
    }

    @Override
    public void initialize(@NotNull ConfigurableApplicationContext configurableApplicationContext) {

        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(configurableApplicationContext,
                format("spring.data.mongodb.host=%s", container.getHost()));

        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(configurableApplicationContext,
                format("spring.data.mongodb.port=%s", container.getMappedPort(27017)));

        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(configurableApplicationContext,
                "spring.data.mongodb.database=trading-journal");
    }
}
