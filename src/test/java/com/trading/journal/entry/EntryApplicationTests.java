package com.trading.journal.entry;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import tooling.MongoDbContainerInitializer;

@SpringBootTest
@Testcontainers
@ContextConfiguration(initializers = MongoDbContainerInitializer.class)
class EntryApplicationTests {

	@Test
	void contextLoads() {
	}
}

