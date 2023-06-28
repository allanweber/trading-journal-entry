package com.trading.journal.entry.entries.impl;

import com.trading.journal.entry.ApplicationException;
import com.trading.journal.entry.balance.Balance;
import com.trading.journal.entry.balance.BalanceService;
import com.trading.journal.entry.entries.*;
import com.trading.journal.entry.journal.Journal;
import com.trading.journal.entry.journal.JournalService;
import com.trading.journal.entry.strategy.Strategy;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class EntryServiceImplTest {

    public static String JOURNAL_ID = "123456";

    @Mock
    EntryRepository repository;

    @Mock
    JournalService journalService;

    @Mock
    BalanceService balanceService;

    @InjectMocks
    EntryServiceImpl entryService;

    @DisplayName("Get all entries with multiple filter")
    @Test
    void all() {
        PageRequest pageable = PageRequest.of(0, Integer.MAX_VALUE);
        EntriesQuery entriesQuery = EntriesQuery.builder().journalId(JOURNAL_ID)
                .journalId("1")
                .symbol("MSFT")
                .type(EntryType.DEPOSIT)
                .from("2022-12-01 13:00:00")
                .status(EntryStatus.CLOSED)
                .pageable(pageable)
                .build();

        when(journalService.get(JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());

        Query query = new Query()
                .addCriteria(Criteria.where("journalId").is("1"))
                .addCriteria(Criteria.where("symbol").is("MSFT"))
                .addCriteria(Criteria.where("type").is("DEPOSIT"))
                .addCriteria(
                        new Criteria()
                                .orOperator(Criteria.where("type").is(EntryType.TRADE).and("exitDate").gte(LocalDateTime.parse("2022-12-01 13:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))),
                                        Criteria.where("type").ne(EntryType.TRADE).and("date").gte(LocalDateTime.parse("2022-12-01 13:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                                )
                )
                .addCriteria(Criteria.where("netResult").exists(true));
        Page<Entry> page = new PageImpl<>(singletonList(Entry.builder().build()), pageable, 1L);
        when(repository.findAll(pageable, query)).thenReturn(page);

        Page<Entry> response = entryService.getAll(entriesQuery);
        assertThat(response.get()).isNotEmpty();
    }

    @DisplayName("Save a TRADE entry")
    @Test
    void saveTrade() {
        Entry toSave = Entry.builder()
                .journalId(JOURNAL_ID)
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .profitPrice(BigDecimal.valueOf(240))
                .lossPrice(BigDecimal.valueOf(180))
                .exitPrice(BigDecimal.valueOf(240))
                .costs(BigDecimal.valueOf(5.59))
                .build();

        Entry calculated = Entry.builder()
                .journalId(JOURNAL_ID)
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .profitPrice(BigDecimal.valueOf(240))
                .lossPrice(BigDecimal.valueOf(180))
                .accountRisked(BigDecimal.valueOf(0.0400).setScale(4, RoundingMode.HALF_EVEN))
                .plannedRR(BigDecimal.valueOf(2.00).setScale(2, RoundingMode.HALF_EVEN))
                .exitPrice(BigDecimal.valueOf(240))
                .costs(BigDecimal.valueOf(5.59))
                .grossResult(BigDecimal.valueOf(80.00).setScale(2, RoundingMode.HALF_EVEN))
                .netResult(BigDecimal.valueOf(74.41))
                .accountChange(BigDecimal.valueOf(0.0744).setScale(4, RoundingMode.HALF_EVEN))
                .accountBalance(BigDecimal.valueOf(1074.41).setScale(2, RoundingMode.HALF_EVEN))
                .build();

        when(journalService.get(JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(balanceService.getCurrentBalance(JOURNAL_ID)).thenReturn(Balance.builder().accountBalance(BigDecimal.valueOf(1000)).build());

        when(repository.save(calculated)).thenReturn(calculated);
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(emptyList()));

        Entry entry = entryService.save(toSave);
        assertThat(entry).isNotNull();

        verify(balanceService).calculateCurrentBalance(anyString());
    }

    @DisplayName("Save a TRADE entry with strategies")
    @Test
    void saveTradeWithST() {
        Entry toSave = Entry.builder()
                .journalId(JOURNAL_ID)
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .profitPrice(BigDecimal.valueOf(240))
                .lossPrice(BigDecimal.valueOf(180))
                .exitPrice(BigDecimal.valueOf(240))
                .costs(BigDecimal.valueOf(5.59))
                .strategies(asList(Strategy.builder().id("ST1").name("Strategy 1").build(), (Strategy.builder().id("ST2").name("Strategy 2").build())))
                .build();

        Entry calculated = Entry.builder()
                .journalId(JOURNAL_ID)
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .profitPrice(BigDecimal.valueOf(240))
                .lossPrice(BigDecimal.valueOf(180))
                .accountRisked(BigDecimal.valueOf(0.0400).setScale(4, RoundingMode.HALF_EVEN))
                .plannedRR(BigDecimal.valueOf(2.00).setScale(2, RoundingMode.HALF_EVEN))
                .exitPrice(BigDecimal.valueOf(240))
                .costs(BigDecimal.valueOf(5.59))
                .grossResult(BigDecimal.valueOf(80.00).setScale(2, RoundingMode.HALF_EVEN))
                .netResult(BigDecimal.valueOf(74.41))
                .accountChange(BigDecimal.valueOf(0.0744).setScale(4, RoundingMode.HALF_EVEN))
                .accountBalance(BigDecimal.valueOf(1074.41).setScale(2, RoundingMode.HALF_EVEN))
                .strategies(asList(Strategy.builder().id("ST1").name("Strategy 1").build(), (Strategy.builder().id("ST2").name("Strategy 2").build())))
                .build();

        when(journalService.get(JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());

        when(balanceService.getCurrentBalance(JOURNAL_ID)).thenReturn(Balance.builder().accountBalance(BigDecimal.valueOf(1000)).build());
        when(repository.save(calculated)).thenReturn(calculated);
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(emptyList()));

        Entry entry = entryService.save(toSave);
        assertThat(entry).isNotNull();
        assertThat(entry.getStrategies()).extracting(Strategy::getName).containsExactlyInAnyOrder("Strategy 1", "Strategy 2");

        verify(balanceService).calculateCurrentBalance(anyString());
    }

    @DisplayName("Save a TRADE entry and other entries need balance")
    @Test
    void saveTradeBalancing() {
        Entry toSave = Entry.builder()
                .journalId(JOURNAL_ID)
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(10))
                .profitPrice(BigDecimal.valueOf(240))
                .lossPrice(BigDecimal.valueOf(180))
                .exitPrice(BigDecimal.valueOf(240))
                .costs(BigDecimal.valueOf(5.59))
                .build();

        Entry calculated = Entry.builder()
                .journalId(JOURNAL_ID)
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(10))
                .profitPrice(BigDecimal.valueOf(240))
                .lossPrice(BigDecimal.valueOf(180))
                .accountRisked(BigDecimal.valueOf(0.2000).setScale(4, RoundingMode.HALF_EVEN))
                .plannedRR(BigDecimal.valueOf(2.00).setScale(2, RoundingMode.HALF_EVEN))
                .exitPrice(BigDecimal.valueOf(240))
                .costs(BigDecimal.valueOf(5.59))
                .grossResult(BigDecimal.valueOf(400.00).setScale(2, RoundingMode.HALF_EVEN))
                .netResult(BigDecimal.valueOf(394.41))
                .accountChange(BigDecimal.valueOf(0.3944).setScale(4, RoundingMode.HALF_EVEN))
                .accountBalance(BigDecimal.valueOf(1394.41).setScale(2, RoundingMode.HALF_EVEN))
                .build();

        when(journalService.get(JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(balanceService.getCurrentBalance(JOURNAL_ID)).thenReturn(Balance.builder().accountBalance(BigDecimal.valueOf(1000)).build());
        when(repository.save(calculated)).thenReturn(calculated);

        when(balanceService.calculateCurrentBalance(JOURNAL_ID)).thenReturn(Balance.builder().accountBalance(BigDecimal.valueOf(1394.41)).build());

        Entry entry = entryService.save(toSave);
        assertThat(entry).isNotNull();
    }

    @DisplayName("Create a WITHDRAWAL entry")
    @Test
    void createWITHDRAWAL() {
        Entry toSave = Entry.builder()
                .journalId(JOURNAL_ID)
                .type(EntryType.WITHDRAWAL)
                .price(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .build();

        Entry calculated = Entry.builder()
                .journalId(JOURNAL_ID)
                .type(EntryType.WITHDRAWAL)
                .price(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .netResult(BigDecimal.valueOf(-234.56))
                .accountChange(BigDecimal.valueOf(-0.2346).setScale(4, RoundingMode.HALF_EVEN))
                .accountBalance(BigDecimal.valueOf(765.44).setScale(2, RoundingMode.HALF_EVEN))
                .build();

        when(journalService.get(JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(balanceService.getCurrentBalance(JOURNAL_ID)).thenReturn(Balance.builder().accountBalance(BigDecimal.valueOf(1000)).build());
        when(repository.save(calculated)).thenReturn(calculated);


        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(emptyList()));

        Entry entry = entryService.save(toSave);
        assertThat(entry).isNotNull();

        verify(balanceService).calculateCurrentBalance(anyString());
    }

    @DisplayName("Create a WITHDRAWAL entry and other entries need balance")
    @Test
    void createWITHDRAWALBalancing() {
        Entry toSave = Entry.builder()
                .journalId(JOURNAL_ID)
                .type(EntryType.WITHDRAWAL)
                .price(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .build();

        Entry calculated = Entry.builder()
                .journalId(JOURNAL_ID)
                .type(EntryType.WITHDRAWAL)
                .price(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .netResult(BigDecimal.valueOf(-234.56))
                .accountChange(BigDecimal.valueOf(-0.2346).setScale(4, RoundingMode.HALF_EVEN))
                .accountBalance(BigDecimal.valueOf(765.44).setScale(2, RoundingMode.HALF_EVEN))
                .build();

        when(journalService.get(JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(balanceService.getCurrentBalance(JOURNAL_ID)).thenReturn(Balance.builder().accountBalance(BigDecimal.valueOf(1000)).build());
        when(repository.save(calculated)).thenReturn(calculated);

        when(balanceService.calculateCurrentBalance(JOURNAL_ID)).thenReturn(Balance.builder().accountBalance(BigDecimal.valueOf(765.44)).build());

        Entry entry = entryService.save(toSave);
        assertThat(entry).isNotNull();
    }

    @DisplayName("Create a TAXES entry")
    @Test
    void createTAXES() {
        Entry toSave = Entry.builder()
                .journalId(JOURNAL_ID)
                .type(EntryType.TAXES)
                .price(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .build();

        Entry calculated = Entry.builder()
                .journalId(JOURNAL_ID)
                .type(EntryType.TAXES)
                .price(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .netResult(BigDecimal.valueOf(-234.56))
                .accountChange(BigDecimal.valueOf(-0.2346).setScale(4, RoundingMode.HALF_EVEN))
                .accountBalance(BigDecimal.valueOf(765.44).setScale(2, RoundingMode.HALF_EVEN))
                .build();

        when(journalService.get(JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(balanceService.getCurrentBalance(JOURNAL_ID)).thenReturn(Balance.builder().accountBalance(BigDecimal.valueOf(1000)).build());

        when(repository.save(calculated)).thenReturn(calculated);
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(emptyList()));

        Entry entry = entryService.save(toSave);
        assertThat(entry).isNotNull();

        verify(balanceService).calculateCurrentBalance(anyString());
    }

    @DisplayName("Create a TAXES entry and other entries need balance")
    @Test
    void createTAXESBalancing() {
        Entry toSave = Entry.builder()
                .journalId(JOURNAL_ID)
                .type(EntryType.TAXES)
                .price(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .build();

        Entry calculated = Entry.builder()
                .journalId(JOURNAL_ID)
                .type(EntryType.TAXES)
                .price(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .netResult(BigDecimal.valueOf(-234.56))
                .accountChange(BigDecimal.valueOf(-0.2346).setScale(4, RoundingMode.HALF_EVEN))
                .accountBalance(BigDecimal.valueOf(765.44).setScale(2, RoundingMode.HALF_EVEN))
                .build();

        when(journalService.get(JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(balanceService.getCurrentBalance(JOURNAL_ID)).thenReturn(Balance.builder().accountBalance(BigDecimal.valueOf(1000)).build());
        when(repository.save(calculated)).thenReturn(calculated);

        when(balanceService.calculateCurrentBalance(JOURNAL_ID)).thenReturn(Balance.builder().accountBalance(BigDecimal.valueOf(765.44)).build());

        Entry entry = entryService.save(toSave);
        assertThat(entry).isNotNull();
    }

    @DisplayName("Create a DEPOSIT entry")
    @Test
    void createDEPOSIT() {
        Entry toSave = Entry.builder()
                .journalId(JOURNAL_ID)
                .type(EntryType.DEPOSIT)
                .price(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .build();

        Entry calculated = Entry.builder()
                .journalId(JOURNAL_ID)
                .type(EntryType.DEPOSIT)
                .price(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .netResult(BigDecimal.valueOf(234.56))
                .accountChange(BigDecimal.valueOf(0.2346).setScale(4, RoundingMode.HALF_EVEN))
                .accountBalance(BigDecimal.valueOf(1234.56).setScale(2, RoundingMode.HALF_EVEN))
                .build();

        when(journalService.get(JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(balanceService.getCurrentBalance(JOURNAL_ID)).thenReturn(Balance.builder().accountBalance(BigDecimal.valueOf(1000)).build());

        when(repository.save(calculated)).thenReturn(calculated);
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(emptyList()));

        Entry entry = entryService.save(toSave);
        assertThat(entry).isNotNull();

        verify(balanceService).calculateCurrentBalance(anyString());
    }

    @DisplayName("Create a DEPOSIT entry and other entries need balance")
    @Test
    void createDEPOSITBalancing() {
        Entry toSave = Entry.builder()
                .journalId(JOURNAL_ID)
                .type(EntryType.DEPOSIT)
                .price(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .build();

        Entry calculated = Entry.builder()
                .journalId(JOURNAL_ID)
                .type(EntryType.DEPOSIT)
                .price(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .netResult(BigDecimal.valueOf(234.56))
                .accountChange(BigDecimal.valueOf(0.2346).setScale(4, RoundingMode.HALF_EVEN))
                .accountBalance(BigDecimal.valueOf(1234.56).setScale(2, RoundingMode.HALF_EVEN))
                .build();

        when(journalService.get(JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(balanceService.getCurrentBalance(JOURNAL_ID)).thenReturn(Balance.builder().accountBalance(BigDecimal.valueOf(1000)).build());
        when(repository.save(calculated)).thenReturn(calculated);

        when(balanceService.calculateCurrentBalance(JOURNAL_ID)).thenReturn(Balance.builder().accountBalance(BigDecimal.valueOf(1234.56)).build());

        Entry entry = entryService.save(toSave);
        assertThat(entry).isNotNull();
    }

    @DisplayName("Delete a entry")
    @Test
    void delete() {
        String entryId = UUID.randomUUID().toString();
        Entry entry = Entry.builder()
                .journalId(JOURNAL_ID)
                .type(EntryType.DEPOSIT)
                .price(BigDecimal.valueOf(234.56))
                .netResult(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .build();

        when(journalService.get(JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(repository.getById(entryId)).thenReturn(Optional.of(entry));
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(emptyList()));

        entryService.delete(entryId);

        verify(repository).delete(entry);

        verify(balanceService).calculateCurrentBalance(anyString());
    }

    @DisplayName("Delete a entry and other entries need balance")
    @Test
    void deleteBalancing() {
        String entryId = UUID.randomUUID().toString();
        Entry entry = Entry.builder()
                .type(EntryType.DEPOSIT)
                .price(BigDecimal.valueOf(234.56))
                .netResult(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .build();

        when(journalService.get(JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(repository.getById(entryId)).thenReturn(Optional.of(entry));

        when(balanceService.calculateCurrentBalance(JOURNAL_ID)).thenReturn(Balance.builder().accountBalance(BigDecimal.valueOf(765.44)).build());

        entryService.delete(entryId);

        verify(repository).delete(entry);
    }

    @DisplayName("Delete a entry not found return an exception")
    @Test
    void deleteNotFound() {
        String entryId = UUID.randomUUID().toString();

        when(journalService.get(JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(repository.getById(entryId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> entryService.delete(entryId));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getStatusText()).isEqualTo("Entry not found");

        verify(repository, never()).delete(any(Entry.class));
    }

    @DisplayName("Delete a not finished entry, do not balance other entries")
    @Test
    void deleteNotFinished() {
        String entryId = UUID.randomUUID().toString();
        Entry entry = Entry.builder()
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .build();

        when(journalService.get(JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(repository.getById(entryId)).thenReturn(Optional.of(entry));

        entryService.delete(entryId);

        verify(repository).delete(entry);

        verify(balanceService, never()).calculateCurrentBalance(anyString());
    }

    @DisplayName("Save a not finished entry, do not balance other entries")
    @Test
    void saveNotFinished() {
        Entry toSave = Entry.builder()
                .journalId(JOURNAL_ID)
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .profitPrice(BigDecimal.valueOf(240))
                .lossPrice(BigDecimal.valueOf(180))
                .build();

        Entry calculated = Entry.builder()
                .journalId(JOURNAL_ID)
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .profitPrice(BigDecimal.valueOf(240))
                .lossPrice(BigDecimal.valueOf(180))
                .accountRisked(BigDecimal.valueOf(0.0400).setScale(4, RoundingMode.HALF_EVEN))
                .plannedRR(BigDecimal.valueOf(2.00).setScale(2, RoundingMode.HALF_EVEN))
                .build();

        when(journalService.get(JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(balanceService.getCurrentBalance(JOURNAL_ID)).thenReturn(Balance.builder().accountBalance(BigDecimal.valueOf(1000)).build());

        when(repository.save(calculated)).thenReturn(calculated);
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(emptyList()));

        Entry entry = entryService.save(toSave);
        assertThat(entry).isNotNull();

        verify(balanceService, never()).calculateCurrentBalance(anyString());
    }

    @DisplayName("Get a entry by id")
    @Test
    void getById() {
        String entryId = UUID.randomUUID().toString();
        Entry entry = Entry.builder()
                .type(EntryType.DEPOSIT)
                .price(BigDecimal.valueOf(234.56))
                .netResult(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .build();

        when(journalService.get(JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(repository.getById(entryId)).thenReturn(Optional.of(entry));


        Entry byId = entryService.getById(entryId);

        assertThat(byId).isEqualTo(entry);
    }

    @DisplayName("Get a entry by id with strategies")
    @Test
    void getByIdWithStrategies() {
        String entryId = UUID.randomUUID().toString();
        Entry entry = Entry.builder()
                .type(EntryType.DEPOSIT)
                .price(BigDecimal.valueOf(234.56))
                .netResult(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .strategies(asList(Strategy.builder().id("ST1").name("Strategy 1").build(), (Strategy.builder().id("ST2").name("Strategy 2").build())))
                .build();

        when(journalService.get(JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(repository.getById(entryId)).thenReturn(Optional.of(entry));

        Entry byId = entryService.getById(entryId);

        assertThat(byId).isEqualTo(entry);
        assertThat(byId.getStrategies()).extracting(Strategy::getId).containsExactlyInAnyOrder("ST1", "ST2");
        assertThat(byId.getStrategies()).extracting(Strategy::getName).containsExactlyInAnyOrder("Strategy 1", "Strategy 2");
    }

    @DisplayName("Get a entry by id not found return an exception")
    @Test
    void getByIdNotFound() {
        String entryId = UUID.randomUUID().toString();

        when(journalService.get(JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(repository.getById(entryId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> entryService.getById(entryId));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getStatusText()).isEqualTo("Entry not found");
    }

    @DisplayName("Count entries by strategy")
    @Test
    void countByStrategy() {
        String strategyId = new ObjectId().toString();
        Criteria criteria = new Criteria("strategies._id").is(new ObjectId(strategyId));
        when(repository.count(Query.query(criteria))).thenReturn(1L);

        Long count = entryService.countByStrategy(strategyId);
        assertThat(count).isEqualTo(1L);
    }
}