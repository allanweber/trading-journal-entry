package com.trading.journal.entry.balance.impl;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.balance.Balance;
import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.EntryRepository;
import com.trading.journal.entry.entries.EntryType;
import com.trading.journal.entry.journal.Journal;
import com.trading.journal.entry.journal.JournalService;
import com.trading.journal.entry.queries.CollectionName;
import com.trading.journal.entry.queries.data.PageableRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class BalanceServiceImplTest {

    private static AccessTokenInfo accessToken;

    private static CollectionName collectionName;

    @Mock
    EntryRepository entryRepository;

    @Mock
    JournalService journalService;

    @InjectMocks
    BalanceServiceImpl balanceService;

    @BeforeAll
    static void setUp() {
        accessToken = new AccessTokenInfo("subject", 1L, "TENANCY", emptyList());
        collectionName = new CollectionName(accessToken, "journal");
    }

    @DisplayName("Start balance positive and many entries with different net results return a positive balance")
    @Test
    void balancePositive() {
        String journalId = "123456";
        BigDecimal startBalance = BigDecimal.valueOf(100);

        when(journalService.get(accessToken, journalId)).thenReturn(Journal.builder().name("journal").startBalance(startBalance).build());

        PageableRequest pageableRequest = PageableRequest.builder()
                .page(0)
                .size(Integer.MAX_VALUE)
                .sort(Sort.by("date").ascending())
                .build();

        List<Entry> entries = asList(
                //Closed Positions
                Entry.builder().type(EntryType.TRADE).netResult(BigDecimal.valueOf(50.31)).build(),
                Entry.builder().type(EntryType.TRADE).netResult(BigDecimal.valueOf(-35.59)).build(),
                Entry.builder().type(EntryType.DEPOSIT).price(BigDecimal.valueOf(71.23)).netResult(BigDecimal.valueOf(71.23)).build(),
                Entry.builder().type(EntryType.WITHDRAWAL).price(BigDecimal.valueOf(12.67)).netResult(BigDecimal.valueOf(-12.67)).build(),
                Entry.builder().type(EntryType.TRADE).netResult(BigDecimal.valueOf(22.67)).build(),
                Entry.builder().type(EntryType.TAXES).price(BigDecimal.valueOf(55.99)).netResult(BigDecimal.valueOf(-55.99)).build(),

                //Opened positions
                Entry.builder().type(EntryType.TRADE).price(BigDecimal.valueOf(123.45)).size(BigDecimal.valueOf(1.2)).build(),
                Entry.builder().type(EntryType.TRADE).price(BigDecimal.valueOf(2345.67)).size(BigDecimal.valueOf(2)).build(),
                Entry.builder().type(EntryType.TRADE).price(BigDecimal.valueOf(891.23)).size(BigDecimal.valueOf(3.1)).build(),
                Entry.builder().type(EntryType.TRADE).price(BigDecimal.valueOf(45.89)).size(BigDecimal.valueOf(4)).build()
        );

        Page<Entry> page = new PageImpl<>(entries, pageableRequest.pageable(), 1L);
        when(entryRepository.findAll(collectionName, pageableRequest)).thenReturn(page);

        Balance balance = balanceService.calculateCurrentBalance(accessToken, journalId);

        assertThat(balance.getAccountBalance()).isEqualTo(BigDecimal.valueOf(139.96));
        assertThat(balance.getClosedPositions()).isEqualTo(BigDecimal.valueOf(37.39));
        assertThat(balance.getOpenedPositions()).isEqualTo(BigDecimal.valueOf(7785.85));
        assertThat(balance.getAvailable()).isEqualTo(BigDecimal.valueOf(-7645.89));
        assertThat(balance.getDeposits()).isEqualTo(BigDecimal.valueOf(71.23));
        assertThat(balance.getTaxes()).isEqualTo(BigDecimal.valueOf(55.99));
        assertThat(balance.getWithdrawals()).isEqualTo(BigDecimal.valueOf(12.67));
    }

    @DisplayName("Start balance positive and many entries with different net results return a negative balance")
    @Test
    void balanceNegative() {
        String journalId = "123456";
        BigDecimal startBalance = BigDecimal.valueOf(100);

        when(journalService.get(accessToken, journalId)).thenReturn(Journal.builder().name("journal").startBalance(startBalance).build());

        PageableRequest pageableRequest = PageableRequest.builder()
                .page(0)
                .size(Integer.MAX_VALUE)
                .sort(Sort.by("date").ascending())
                .build();

        List<Entry> entries = asList(
                Entry.builder().type(EntryType.TRADE).netResult(BigDecimal.valueOf(50.31)).build(),
                Entry.builder().type(EntryType.TRADE).netResult(BigDecimal.valueOf(-35.59)).build(),
                Entry.builder().type(EntryType.WITHDRAWAL).price(BigDecimal.valueOf(71.23)).netResult(BigDecimal.valueOf(-71.23)).build(),
                Entry.builder().type(EntryType.TAXES).price(BigDecimal.valueOf(12.67)).netResult(BigDecimal.valueOf(-12.67)).build(),
                Entry.builder().type(EntryType.TRADE).netResult(BigDecimal.valueOf(-22.67)).build(),
                Entry.builder().type(EntryType.WITHDRAWAL).price(BigDecimal.valueOf(52.29)).netResult(BigDecimal.valueOf(-52.29)).build(),

                //Opened positions
                Entry.builder().type(EntryType.TRADE).price(BigDecimal.valueOf(123.45)).size(BigDecimal.valueOf(1.2)).build(),
                Entry.builder().type(EntryType.TRADE).price(BigDecimal.valueOf(2345.67)).size(BigDecimal.valueOf(2)).build(),
                Entry.builder().type(EntryType.TRADE).price(BigDecimal.valueOf(891.23)).size(BigDecimal.valueOf(3.1)).build(),
                Entry.builder().type(EntryType.TRADE).price(BigDecimal.valueOf(45.89)).size(BigDecimal.valueOf(4)).build()
        );

        Page<Entry> page = new PageImpl<>(entries, pageableRequest.pageable(), 1L);
        when(entryRepository.findAll(collectionName, pageableRequest)).thenReturn(page);

        Balance balance = balanceService.calculateCurrentBalance(accessToken, journalId);

        assertThat(balance.getAccountBalance()).isEqualTo(BigDecimal.valueOf(-44.14));
        assertThat(balance.getClosedPositions()).isEqualTo(BigDecimal.valueOf(-7.95));
        assertThat(balance.getOpenedPositions()).isEqualTo(BigDecimal.valueOf(7785.85));
        assertThat(balance.getAvailable()).isEqualTo(BigDecimal.valueOf(-7829.99));
        assertThat(balance.getDeposits()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
        assertThat(balance.getTaxes()).isEqualTo(BigDecimal.valueOf(12.67));
        assertThat(balance.getWithdrawals()).isEqualTo(BigDecimal.valueOf(123.52));
    }

    @DisplayName("Start balance negative and many entries with different net results return a positive balance")
    @Test
    void balancePositiveWithStartBalanceNegative() {
        String journalId = "123456";
        BigDecimal startBalance = BigDecimal.valueOf(-100);

        when(journalService.get(accessToken, journalId)).thenReturn(Journal.builder().name("journal").startBalance(startBalance).build());

        PageableRequest pageableRequest = PageableRequest.builder()
                .page(0)
                .size(Integer.MAX_VALUE)
                .sort(Sort.by("date").ascending())
                .build();

        List<Entry> entries = asList(
                Entry.builder().type(EntryType.TRADE).netResult(BigDecimal.valueOf(50.31)).build(),
                Entry.builder().type(EntryType.TRADE).netResult(BigDecimal.valueOf(35.59)).build(),
                Entry.builder().type(EntryType.DEPOSIT).price(BigDecimal.valueOf(71.23)).netResult(BigDecimal.valueOf(71.23)).build(),
                Entry.builder().type(EntryType.TAXES).price(BigDecimal.valueOf(12.67)).netResult(BigDecimal.valueOf(-12.67)).build(),
                Entry.builder().type(EntryType.TRADE).netResult(BigDecimal.valueOf(22.67)).build(),
                Entry.builder().type(EntryType.TRADE).netResult(BigDecimal.valueOf(33.88)).build(),
                Entry.builder().type(EntryType.WITHDRAWAL).price(BigDecimal.valueOf(55.99)).netResult(BigDecimal.valueOf(-55.99)).build(),
                Entry.builder().type(EntryType.WITHDRAWAL).price(BigDecimal.valueOf(45.01)).netResult(BigDecimal.valueOf(-45.01)).build(),

                //Opened positions
                Entry.builder().type(EntryType.TRADE).price(BigDecimal.valueOf(123.45)).size(BigDecimal.valueOf(1.2)).build(),
                Entry.builder().type(EntryType.TRADE).price(BigDecimal.valueOf(2345.67)).size(BigDecimal.valueOf(2)).build(),
                Entry.builder().type(EntryType.TRADE).price(BigDecimal.valueOf(891.23)).size(BigDecimal.valueOf(3.1)).build(),
                Entry.builder().type(EntryType.TRADE).price(BigDecimal.valueOf(45.89)).size(BigDecimal.valueOf(4)).build()
        );

        Page<Entry> page = new PageImpl<>(entries, pageableRequest.pageable(), 1L);
        when(entryRepository.findAll(collectionName, pageableRequest)).thenReturn(page);

        Balance balance = balanceService.calculateCurrentBalance(accessToken, journalId);

        assertThat(balance.getAccountBalance()).isEqualTo(BigDecimal.valueOf(0.01));
        assertThat(balance.getClosedPositions()).isEqualTo(BigDecimal.valueOf(142.45));
        assertThat(balance.getOpenedPositions()).isEqualTo(BigDecimal.valueOf(7785.85));
        assertThat(balance.getAvailable()).isEqualTo(BigDecimal.valueOf(-7785.84));
        assertThat(balance.getDeposits()).isEqualTo(BigDecimal.valueOf(71.23));
        assertThat(balance.getTaxes()).isEqualTo(BigDecimal.valueOf(12.67));
        assertThat(balance.getWithdrawals()).isEqualTo(BigDecimal.valueOf(101.00).setScale(2, RoundingMode.HALF_EVEN));
    }

    @DisplayName("Start balance positive and many entries with different net results return a positive balance and available positive")
    @Test
    void balanceWithAvailablePositive() {
        String journalId = "123456";
        BigDecimal startBalance = BigDecimal.valueOf(15000);

        when(journalService.get(accessToken, journalId)).thenReturn(Journal.builder().name("journal").startBalance(startBalance).build());

        PageableRequest pageableRequest = PageableRequest.builder()
                .page(0)
                .size(Integer.MAX_VALUE)
                .sort(Sort.by("date").ascending())
                .build();

        List<Entry> entries = asList(
                Entry.builder().type(EntryType.TRADE).netResult(BigDecimal.valueOf(50.31)).build(),
                Entry.builder().type(EntryType.TRADE).netResult(BigDecimal.valueOf(35.59)).build(),
                Entry.builder().type(EntryType.DEPOSIT).price(BigDecimal.valueOf(71.23)).netResult(BigDecimal.valueOf(71.23)).build(),
                Entry.builder().type(EntryType.TAXES).price(BigDecimal.valueOf(12.67)).netResult(BigDecimal.valueOf(-12.67)).build(),
                Entry.builder().type(EntryType.TRADE).netResult(BigDecimal.valueOf(22.67)).build(),
                Entry.builder().type(EntryType.TRADE).netResult(BigDecimal.valueOf(33.88)).build(),
                Entry.builder().type(EntryType.WITHDRAWAL).price(BigDecimal.valueOf(55.99)).netResult(BigDecimal.valueOf(-55.99)).build(),
                Entry.builder().type(EntryType.WITHDRAWAL).price(BigDecimal.valueOf(45.01)).netResult(BigDecimal.valueOf(-45.01)).build(),

                //Opened positions
                Entry.builder().type(EntryType.TRADE).price(BigDecimal.valueOf(123.45)).size(BigDecimal.valueOf(1.2)).build(),
                Entry.builder().type(EntryType.TRADE).price(BigDecimal.valueOf(2345.67)).size(BigDecimal.valueOf(2)).build(),
                Entry.builder().type(EntryType.TRADE).price(BigDecimal.valueOf(891.23)).size(BigDecimal.valueOf(3.1)).build(),
                Entry.builder().type(EntryType.TRADE).price(BigDecimal.valueOf(45.89)).size(BigDecimal.valueOf(4)).build()
        );

        Page<Entry> page = new PageImpl<>(entries, pageableRequest.pageable(), 1L);
        when(entryRepository.findAll(collectionName, pageableRequest)).thenReturn(page);

        Balance balance = balanceService.calculateCurrentBalance(accessToken, journalId);

        assertThat(balance.getAccountBalance()).isEqualTo(BigDecimal.valueOf(15100.01));
        assertThat(balance.getClosedPositions()).isEqualTo(BigDecimal.valueOf(142.45));
        assertThat(balance.getOpenedPositions()).isEqualTo(BigDecimal.valueOf(7785.85));
        assertThat(balance.getAvailable()).isEqualTo(BigDecimal.valueOf(7314.16));
        assertThat(balance.getDeposits()).isEqualTo(BigDecimal.valueOf(71.23));
        assertThat(balance.getTaxes()).isEqualTo(BigDecimal.valueOf(12.67));
        assertThat(balance.getWithdrawals()).isEqualTo(BigDecimal.valueOf(101.00).setScale(2, RoundingMode.HALF_EVEN));
    }

    @DisplayName("Calculate balance available positive")
    @Test
    void availablePositive() {
        String journalId = "123456";
        BigDecimal startBalance = BigDecimal.valueOf(15000);
        Journal journal = Journal.builder()
                .name("journal")
                .startBalance(startBalance)
                .currentBalance(Balance.builder()
                        .accountBalance(startBalance)
                        .closedPositions(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                        .deposits(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                        .withdrawals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                        .taxes(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                        .openedPositions(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                        .available(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                        .build())
                .build();
        when(journalService.get(accessToken, journalId)).thenReturn(journal);

        PageableRequest pageableRequest = PageableRequest.builder()
                .page(0)
                .size(Integer.MAX_VALUE)
                .sort(Sort.by("date").ascending())
                .build();

        List<Entry> entries = asList(
                //Opened positions
                Entry.builder().type(EntryType.TRADE).price(BigDecimal.valueOf(123.45)).size(BigDecimal.valueOf(1.2)).build(),
                Entry.builder().type(EntryType.TRADE).price(BigDecimal.valueOf(2345.67)).size(BigDecimal.valueOf(2)).build(),
                Entry.builder().type(EntryType.TRADE).price(BigDecimal.valueOf(891.23)).size(BigDecimal.valueOf(3.1)).build(),
                Entry.builder().type(EntryType.TRADE).price(BigDecimal.valueOf(45.89)).size(BigDecimal.valueOf(4)).build()
        );

        Page<Entry> page = new PageImpl<>(entries, pageableRequest.pageable(), 1L);
        Query query = new Query(Criteria.where("netResult").exists(false));
        when(entryRepository.findAll(collectionName, pageableRequest, query)).thenReturn(page);

        Balance balance = balanceService.calculateAvailableBalance(accessToken, journalId);

        assertThat(balance.getAccountBalance()).isEqualTo(BigDecimal.valueOf(15000.00).setScale(2, RoundingMode.HALF_EVEN));
        assertThat(balance.getClosedPositions()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
        assertThat(balance.getOpenedPositions()).isEqualTo(BigDecimal.valueOf(7785.85).setScale(2, RoundingMode.HALF_EVEN));
        assertThat(balance.getAvailable()).isEqualTo(BigDecimal.valueOf(7214.15).setScale(2, RoundingMode.HALF_EVEN));
        assertThat(balance.getDeposits()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
        assertThat(balance.getTaxes()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
        assertThat(balance.getWithdrawals()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
    }

    @DisplayName("Calculate balance available negative")
    @Test
    void availableNegative() {
        String journalId = "123456";
        BigDecimal startBalance = BigDecimal.valueOf(5000);
        Journal journal = Journal.builder()
                .name("journal")
                .startBalance(startBalance)
                .currentBalance(Balance.builder()
                        .accountBalance(startBalance)
                        .closedPositions(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                        .deposits(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                        .withdrawals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                        .taxes(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                        .openedPositions(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                        .available(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                        .build())
                .build();
        when(journalService.get(accessToken, journalId)).thenReturn(journal);

        PageableRequest pageableRequest = PageableRequest.builder()
                .page(0)
                .size(Integer.MAX_VALUE)
                .sort(Sort.by("date").ascending())
                .build();

        List<Entry> entries = asList(
                //Opened positions
                Entry.builder().type(EntryType.TRADE).price(BigDecimal.valueOf(123.45)).size(BigDecimal.valueOf(1.2)).build(),
                Entry.builder().type(EntryType.TRADE).price(BigDecimal.valueOf(2345.67)).size(BigDecimal.valueOf(2)).build(),
                Entry.builder().type(EntryType.TRADE).price(BigDecimal.valueOf(891.23)).size(BigDecimal.valueOf(3.1)).build(),
                Entry.builder().type(EntryType.TRADE).price(BigDecimal.valueOf(45.89)).size(BigDecimal.valueOf(4)).build()
        );

        Page<Entry> page = new PageImpl<>(entries, pageableRequest.pageable(), 1L);
        Query query = new Query(Criteria.where("netResult").exists(false));
        when(entryRepository.findAll(collectionName, pageableRequest, query)).thenReturn(page);

        Balance balance = balanceService.calculateAvailableBalance(accessToken, journalId);

        assertThat(balance.getAccountBalance()).isEqualTo(BigDecimal.valueOf(5000.00).setScale(2, RoundingMode.HALF_EVEN));
        assertThat(balance.getClosedPositions()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
        assertThat(balance.getOpenedPositions()).isEqualTo(BigDecimal.valueOf(7785.85).setScale(2, RoundingMode.HALF_EVEN));
        assertThat(balance.getAvailable()).isEqualTo(BigDecimal.valueOf(-2785.85).setScale(2, RoundingMode.HALF_EVEN));
        assertThat(balance.getDeposits()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
        assertThat(balance.getTaxes()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
        assertThat(balance.getWithdrawals()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
    }

    @DisplayName("Get current balance from journal")
    @Test
    void getCurrentBalance() {
        String journalId = "123456";
        BigDecimal startBalance = BigDecimal.valueOf(-100);

        Balance balance = Balance.builder()
                .accountBalance(BigDecimal.valueOf(100))
                .taxes(BigDecimal.valueOf(100))
                .withdrawals(BigDecimal.valueOf(100))
                .deposits(BigDecimal.valueOf(100))
                .closedPositions(BigDecimal.valueOf(100))
                .build();

        Journal journal = Journal.builder()
                .name("journal")
                .startBalance(startBalance)
                .lastBalance(LocalDateTime.now())
                .currentBalance(balance)
                .build();
        when(journalService.get(accessToken, journalId)).thenReturn(journal);

        Balance currentBalance = balanceService.getCurrentBalance(accessToken, journalId);
        assertThat(currentBalance).isEqualTo(balance);
    }
}