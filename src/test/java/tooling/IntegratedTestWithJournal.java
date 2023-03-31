package tooling;

import com.trading.journal.entry.balance.Balance;
import com.trading.journal.entry.journal.Journal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public class IntegratedTestWithJournal extends IntegratedTest {

    public static String journalCollection = "TestTenancy_journals";

    public static String journalId;

    @Autowired
    public MongoTemplate mongoTemplate;

    @BeforeEach
    public void setUp() {
        Journal build = Journal.builder()
                .name("JOURNAL-1")
                .startBalance(BigDecimal.valueOf(10000))
                .startJournal(LocalDateTime.of(2022, 11, 10, 15, 25, 35))
                .currentBalance(
                        Balance.builder()
                                .accountBalance(BigDecimal.valueOf(10000).setScale(2, RoundingMode.HALF_EVEN))
                                .taxes(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                                .withdrawals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                                .deposits(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                                .closedPositions(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                                .build()
                )
                .build();
        Journal journal = mongoTemplate.save(build, journalCollection);
        journalId = journal.getId();
    }

    @AfterEach
    public void afterEach() {
        mongoTemplate.dropCollection(journalCollection);
    }
}
