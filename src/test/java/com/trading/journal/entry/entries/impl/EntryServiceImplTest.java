package com.trading.journal.entry.entries.impl;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.ApplicationException;
import com.trading.journal.entry.balance.Balance;
import com.trading.journal.entry.balance.BalanceService;
import com.trading.journal.entry.entries.*;
import com.trading.journal.entry.journal.Journal;
import com.trading.journal.entry.journal.JournalService;
import com.trading.journal.entry.queries.CollectionName;
import com.trading.journal.entry.queries.data.PageableRequest;
import com.trading.journal.entry.strategy.Strategy;
import com.trading.journal.entry.strategy.StrategyService;
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
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class EntryServiceImplTest {

    public static String JOURNAL_ID = "123456";
    private static final AccessTokenInfo ACCESS_TOKEN = new AccessTokenInfo("subject", 1L, "TENANCY", emptyList());

    private static CollectionName collectionName;

    @Mock
    EntryRepository repository;

    @Mock
    JournalService journalService;

    @Mock
    BalanceService balanceService;

    @Mock
    StrategyService strategyService;

    @InjectMocks
    EntryServiceImpl entryService;

    @BeforeAll
    static void setUp() {
        collectionName = new CollectionName(ACCESS_TOKEN, "my-journal");
    }

    @DisplayName("Get all entries with no filter")
    @Test
    void all() {
        EntriesQuery entriesQuery = EntriesQuery.builder().accessTokenInfo(ACCESS_TOKEN).journalId(JOURNAL_ID).build();

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());

        PageableRequest request = PageableRequest.builder()
                .page(0)
                .size(Integer.MAX_VALUE)
                .sort(Sort.by("date").ascending())
                .build();

        Page<Entry> page = new PageImpl<>(singletonList(Entry.builder().build()), request.pageable(), 1L);
        when(repository.findAll(collectionName, request, new Query())).thenReturn(page);

        List<Entry> response = entryService.getAll(entriesQuery);
        assertThat(response).isNotEmpty();
    }

    @DisplayName("Get all entries by symbol")
    @Test
    void allBySymbol() {
        EntriesQuery entriesQuery = EntriesQuery.builder().accessTokenInfo(ACCESS_TOKEN).journalId(JOURNAL_ID)
                .symbol("MSFT")
                .build();

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());

        PageableRequest request = PageableRequest.builder()
                .page(0)
                .size(Integer.MAX_VALUE)
                .sort(Sort.by("date").ascending())
                .build();

        Query query = new Query().addCriteria(Criteria.where("symbol").is("MSFT"));
        Page<Entry> page = new PageImpl<>(singletonList(Entry.builder().build()), request.pageable(), 1L);
        when(repository.findAll(collectionName, request, query)).thenReturn(page);

        List<Entry> response = entryService.getAll(entriesQuery);
        assertThat(response).isNotEmpty();
    }

    @DisplayName("Get all entries by type")
    @Test
    void allByType() {
        EntriesQuery entriesQuery = EntriesQuery.builder().accessTokenInfo(ACCESS_TOKEN).journalId(JOURNAL_ID)
                .type(EntryType.DEPOSIT)
                .build();

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());

        PageableRequest request = PageableRequest.builder()
                .page(0)
                .size(Integer.MAX_VALUE)
                .sort(Sort.by("date").ascending())
                .build();

        Query query = new Query().addCriteria(Criteria.where("type").is("DEPOSIT"));
        Page<Entry> page = new PageImpl<>(singletonList(Entry.builder().build()), request.pageable(), 1L);
        when(repository.findAll(collectionName, request, query)).thenReturn(page);

        List<Entry> response = entryService.getAll(entriesQuery);
        assertThat(response).isNotEmpty();
    }

    @DisplayName("Get all entries by from")
    @Test
    void allByFrom() {
        EntriesQuery entriesQuery = EntriesQuery.builder().accessTokenInfo(ACCESS_TOKEN).journalId(JOURNAL_ID)
                .from("2022-12-01 13:00:00")
                .build();

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());

        PageableRequest request = PageableRequest.builder()
                .page(0)
                .size(Integer.MAX_VALUE)
                .sort(Sort.by("date").ascending())
                .build();

        Query query = new Query().addCriteria(Criteria.where("date").gte(LocalDateTime.parse("2022-12-01 13:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        Page<Entry> page = new PageImpl<>(singletonList(Entry.builder().build()), request.pageable(), 1L);
        when(repository.findAll(collectionName, request, query)).thenReturn(page);

        List<Entry> response = entryService.getAll(entriesQuery);
        assertThat(response).isNotEmpty();
    }

    @DisplayName("Get all entries by OPEN status")
    @Test
    void allByOpen() {
        EntriesQuery entriesQuery = EntriesQuery.builder().accessTokenInfo(ACCESS_TOKEN).journalId(JOURNAL_ID)
                .status(EntryStatus.OPEN)
                .build();

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());

        PageableRequest request = PageableRequest.builder()
                .page(0)
                .size(Integer.MAX_VALUE)
                .sort(Sort.by("date").ascending())
                .build();

        Query query = new Query().addCriteria(Criteria.where("netResult").exists(false));
        Page<Entry> page = new PageImpl<>(singletonList(Entry.builder().build()), request.pageable(), 1L);
        when(repository.findAll(collectionName, request, query)).thenReturn(page);

        List<Entry> response = entryService.getAll(entriesQuery);
        assertThat(response).isNotEmpty();
    }

    @DisplayName("Get all entries by CLOSED status")
    @Test
    void allByClosed() {
        EntriesQuery entriesQuery = EntriesQuery.builder().accessTokenInfo(ACCESS_TOKEN).journalId(JOURNAL_ID)
                .status(EntryStatus.CLOSED)
                .build();

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());

        PageableRequest request = PageableRequest.builder()
                .page(0)
                .size(Integer.MAX_VALUE)
                .sort(Sort.by("exitDate").ascending())
                .build();

        Query query = new Query().addCriteria(Criteria.where("netResult").exists(true));
        Page<Entry> page = new PageImpl<>(singletonList(Entry.builder().build()), request.pageable(), 1L);
        when(repository.findAll(collectionName, request, query)).thenReturn(page);

        List<Entry> response = entryService.getAll(entriesQuery);
        assertThat(response).isNotEmpty();
    }

    @DisplayName("Get all entries by LONG Direction")
    @Test
    void allByLong() {
        EntriesQuery entriesQuery = EntriesQuery.builder().accessTokenInfo(ACCESS_TOKEN).journalId(JOURNAL_ID)
                .direction(EntryDirection.LONG)
                .build();

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());

        PageableRequest request = PageableRequest.builder()
                .page(0)
                .size(Integer.MAX_VALUE)
                .sort(Sort.by("date").ascending())
                .build();

        Query query = new Query().addCriteria(Criteria.where("direction").is("LONG"));
        Page<Entry> page = new PageImpl<>(singletonList(Entry.builder().build()), request.pageable(), 1L);
        when(repository.findAll(collectionName, request, query)).thenReturn(page);

        List<Entry> response = entryService.getAll(entriesQuery);
        assertThat(response).isNotEmpty();
    }

    @DisplayName("Get all entries by SHORT Direction")
    @Test
    void allByShort() {
        EntriesQuery entriesQuery = EntriesQuery.builder().accessTokenInfo(ACCESS_TOKEN).journalId(JOURNAL_ID)
                .direction(EntryDirection.SHORT)
                .build();

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());

        PageableRequest request = PageableRequest.builder()
                .page(0)
                .size(Integer.MAX_VALUE)
                .sort(Sort.by("date").ascending())
                .build();

        Query query = new Query().addCriteria(Criteria.where("direction").is("SHORT"));
        Page<Entry> page = new PageImpl<>(singletonList(Entry.builder().build()), request.pageable(), 1L);
        when(repository.findAll(collectionName, request, query)).thenReturn(page);

        List<Entry> response = entryService.getAll(entriesQuery);
        assertThat(response).isNotEmpty();
    }

    @DisplayName("Get all WIN Trades")
    @Test
    void allWin() {
        EntriesQuery entriesQuery = EntriesQuery.builder().accessTokenInfo(ACCESS_TOKEN).journalId(JOURNAL_ID)
                .result(EntryResult.WIN)
                .build();

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());

        PageableRequest request = PageableRequest.builder()
                .page(0)
                .size(Integer.MAX_VALUE)
                .sort(Sort.by("date").ascending())
                .build();

        Query query = new Query().addCriteria(Criteria.where("netResult").gte(BigDecimal.ZERO));
        Page<Entry> page = new PageImpl<>(singletonList(Entry.builder().build()), request.pageable(), 1L);
        when(repository.findAll(collectionName, request, query)).thenReturn(page);

        List<Entry> response = entryService.getAll(entriesQuery);
        assertThat(response).isNotEmpty();
    }

    @DisplayName("Get all LOSE Trades")
    @Test
    void allLose() {
        EntriesQuery entriesQuery = EntriesQuery.builder().accessTokenInfo(ACCESS_TOKEN).journalId(JOURNAL_ID)
                .result(EntryResult.LOSE)
                .build();

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());

        PageableRequest request = PageableRequest.builder()
                .page(0)
                .size(Integer.MAX_VALUE)
                .sort(Sort.by("date").ascending())
                .build();

        Query query = new Query().addCriteria(Criteria.where("netResult").lt(BigDecimal.ZERO));
        Page<Entry> page = new PageImpl<>(singletonList(Entry.builder().build()), request.pageable(), 1L);
        when(repository.findAll(collectionName, request, query)).thenReturn(page);

        List<Entry> response = entryService.getAll(entriesQuery);
        assertThat(response).isNotEmpty();
    }

    @DisplayName("Get all entries by multiple filter")
    @Test
    void allByMultiples() {
        EntriesQuery entriesQuery = EntriesQuery.builder().accessTokenInfo(ACCESS_TOKEN).journalId(JOURNAL_ID)
                .symbol("MSFT")
                .type(EntryType.DEPOSIT)
                .from("2022-12-01 13:00:00")
                .status(EntryStatus.CLOSED)
                .build();

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());

        PageableRequest request = PageableRequest.builder()
                .page(0)
                .size(Integer.MAX_VALUE)
                .sort(Sort.by("exitDate").ascending())
                .build();

        Query query = new Query().addCriteria(Criteria.where("symbol").is("MSFT"))
                .addCriteria(Criteria.where("type").is("DEPOSIT"))
                .addCriteria(Criteria.where("exitDate").gte(LocalDateTime.parse("2022-12-01 13:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
                .addCriteria(Criteria.where("netResult").exists(true));
        Page<Entry> page = new PageImpl<>(singletonList(Entry.builder().build()), request.pageable(), 1L);
        when(repository.findAll(collectionName, request, query)).thenReturn(page);

        List<Entry> response = entryService.getAll(entriesQuery);
        assertThat(response).isNotEmpty();
    }

    @DisplayName("Save a TRADE entry")
    @Test
    void saveTrade() {
        Entry toSave = Entry.builder()
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

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(balanceService.getCurrentBalance(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Balance.builder().accountBalance(BigDecimal.valueOf(1000)).build());

        when(repository.save(collectionName, calculated)).thenReturn(calculated);
        when(repository.findAll(eq(collectionName), any(PageableRequest.class))).thenReturn(new PageImpl<>(emptyList()));

        Entry entry = entryService.save(ACCESS_TOKEN, JOURNAL_ID, toSave);
        assertThat(entry).isNotNull();

        verify(balanceService).calculateCurrentBalance(any(), anyString());
    }

    @DisplayName("Save a TRADE entry with strategies")
    @Test
    void saveTradeWithST() {
        Entry toSave = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .profitPrice(BigDecimal.valueOf(240))
                .lossPrice(BigDecimal.valueOf(180))
                .exitPrice(BigDecimal.valueOf(240))
                .costs(BigDecimal.valueOf(5.59))
                .strategyIds(asList("ST1", "ST2"))
                .build();

        Entry calculated = Entry.builder()
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
                .strategyIds(asList("ST1", "ST2"))
                .build();

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());

        when(strategyService.getById(ACCESS_TOKEN, "ST1")).thenReturn(Optional.of(Strategy.builder().id("ST1").name("Strategy 1").build()));
        when(strategyService.getById(ACCESS_TOKEN, "ST2")).thenReturn(Optional.of(Strategy.builder().id("ST2").name("Strategy 2").build()));

        when(balanceService.getCurrentBalance(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Balance.builder().accountBalance(BigDecimal.valueOf(1000)).build());
        when(repository.save(collectionName, calculated)).thenReturn(calculated);
        when(repository.findAll(eq(collectionName), any(PageableRequest.class))).thenReturn(new PageImpl<>(emptyList()));

        Entry entry = entryService.save(ACCESS_TOKEN, JOURNAL_ID, toSave);
        assertThat(entry).isNotNull();
        assertThat(entry.getStrategies()).extracting(Strategy::getName).containsExactlyInAnyOrder("Strategy 1", "Strategy 2");

        verify(balanceService).calculateCurrentBalance(any(), anyString());
    }

    @DisplayName("Save a TRADE entry with invalid strategy throw an exception")
    @Test
    void saveWithInvalidST() {
        Entry toSave = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .profitPrice(BigDecimal.valueOf(240))
                .lossPrice(BigDecimal.valueOf(180))
                .exitPrice(BigDecimal.valueOf(240))
                .costs(BigDecimal.valueOf(5.59))
                .strategyIds(asList("ST1", "ST2"))
                .build();

        Entry calculated = Entry.builder()
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
                .strategyIds(asList("ST1", "ST2"))
                .build();

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());

        when(strategyService.getById(ACCESS_TOKEN, "ST1")).thenReturn(Optional.of(Strategy.builder().id("ST1").name("Strategy 1").build()));
        when(strategyService.getById(ACCESS_TOKEN, "ST2")).thenReturn(Optional.empty());

        when(balanceService.getCurrentBalance(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Balance.builder().accountBalance(BigDecimal.valueOf(1000)).build());
        when(repository.save(collectionName, calculated)).thenReturn(calculated);
        when(repository.findAll(eq(collectionName), any(PageableRequest.class))).thenReturn(new PageImpl<>(emptyList()));

        ApplicationException exception = assertThrows(ApplicationException.class, () -> entryService.save(ACCESS_TOKEN, JOURNAL_ID, toSave));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getStatusText()).isEqualTo("Invalid Strategy ST2");

        verify(balanceService, never()).getCurrentBalance(any(), anyString());
        verify(repository, never()).save(any(),any());
        verify(repository, never()).findAll(eq(collectionName), any(PageableRequest.class));

    }

    @DisplayName("Save a TRADE entry and other entries need balance")
    @Test
    void saveTradeBalancing() {
        Entry toSave = Entry.builder()
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

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(balanceService.getCurrentBalance(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Balance.builder().accountBalance(BigDecimal.valueOf(1000)).build());
        when(repository.save(collectionName, calculated)).thenReturn(calculated);

        when(balanceService.calculateCurrentBalance(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Balance.builder().accountBalance(BigDecimal.valueOf(1394.41)).build());

        Entry entry = entryService.save(ACCESS_TOKEN, JOURNAL_ID, toSave);
        assertThat(entry).isNotNull();
    }

    @DisplayName("Create a WITHDRAWAL entry")
    @Test
    void createWITHDRAWAL() {
        Entry toSave = Entry.builder()
                .type(EntryType.WITHDRAWAL)
                .price(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .build();

        Entry calculated = Entry.builder()
                .type(EntryType.WITHDRAWAL)
                .price(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .netResult(BigDecimal.valueOf(-234.56))
                .accountChange(BigDecimal.valueOf(-0.2346).setScale(4, RoundingMode.HALF_EVEN))
                .accountBalance(BigDecimal.valueOf(765.44).setScale(2, RoundingMode.HALF_EVEN))
                .build();

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(balanceService.getCurrentBalance(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Balance.builder().accountBalance(BigDecimal.valueOf(1000)).build());
        when(repository.save(collectionName, calculated)).thenReturn(calculated);


        when(repository.findAll(eq(collectionName), any(PageableRequest.class))).thenReturn(new PageImpl<>(emptyList()));

        Entry entry = entryService.save(ACCESS_TOKEN, JOURNAL_ID, toSave);
        assertThat(entry).isNotNull();

        verify(balanceService).calculateCurrentBalance(any(), anyString());
    }

    @DisplayName("Create a WITHDRAWAL entry and other entries need balance")
    @Test
    void createWITHDRAWALBalancing() {
        Entry toSave = Entry.builder()
                .type(EntryType.WITHDRAWAL)
                .price(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .build();

        Entry calculated = Entry.builder()
                .type(EntryType.WITHDRAWAL)
                .price(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .netResult(BigDecimal.valueOf(-234.56))
                .accountChange(BigDecimal.valueOf(-0.2346).setScale(4, RoundingMode.HALF_EVEN))
                .accountBalance(BigDecimal.valueOf(765.44).setScale(2, RoundingMode.HALF_EVEN))
                .build();

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(balanceService.getCurrentBalance(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Balance.builder().accountBalance(BigDecimal.valueOf(1000)).build());
        when(repository.save(collectionName, calculated)).thenReturn(calculated);

        when(balanceService.calculateCurrentBalance(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Balance.builder().accountBalance(BigDecimal.valueOf(765.44)).build());

        Entry entry = entryService.save(ACCESS_TOKEN, JOURNAL_ID, toSave);
        assertThat(entry).isNotNull();
    }

    @DisplayName("Create a TAXES entry")
    @Test
    void createTAXES() {
        Entry toSave = Entry.builder()
                .type(EntryType.TAXES)
                .price(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .build();

        Entry calculated = Entry.builder()
                .type(EntryType.TAXES)
                .price(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .netResult(BigDecimal.valueOf(-234.56))
                .accountChange(BigDecimal.valueOf(-0.2346).setScale(4, RoundingMode.HALF_EVEN))
                .accountBalance(BigDecimal.valueOf(765.44).setScale(2, RoundingMode.HALF_EVEN))
                .build();

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(balanceService.getCurrentBalance(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Balance.builder().accountBalance(BigDecimal.valueOf(1000)).build());

        when(repository.save(collectionName, calculated)).thenReturn(calculated);
        when(repository.findAll(eq(collectionName), any(PageableRequest.class))).thenReturn(new PageImpl<>(emptyList()));

        Entry entry = entryService.save(ACCESS_TOKEN, JOURNAL_ID, toSave);
        assertThat(entry).isNotNull();

        verify(balanceService).calculateCurrentBalance(any(), anyString());
    }

    @DisplayName("Create a TAXES entry and other entries need balance")
    @Test
    void createTAXESBalancing() {
        Entry toSave = Entry.builder()
                .type(EntryType.TAXES)
                .price(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .build();

        Entry calculated = Entry.builder()
                .type(EntryType.TAXES)
                .price(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .netResult(BigDecimal.valueOf(-234.56))
                .accountChange(BigDecimal.valueOf(-0.2346).setScale(4, RoundingMode.HALF_EVEN))
                .accountBalance(BigDecimal.valueOf(765.44).setScale(2, RoundingMode.HALF_EVEN))
                .build();

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(balanceService.getCurrentBalance(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Balance.builder().accountBalance(BigDecimal.valueOf(1000)).build());
        when(repository.save(collectionName, calculated)).thenReturn(calculated);

        when(balanceService.calculateCurrentBalance(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Balance.builder().accountBalance(BigDecimal.valueOf(765.44)).build());

        Entry entry = entryService.save(ACCESS_TOKEN, JOURNAL_ID, toSave);
        assertThat(entry).isNotNull();
    }

    @DisplayName("Create a DEPOSIT entry")
    @Test
    void createDEPOSIT() {
        Entry toSave = Entry.builder()
                .type(EntryType.DEPOSIT)
                .price(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .build();

        Entry calculated = Entry.builder()
                .type(EntryType.DEPOSIT)
                .price(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .netResult(BigDecimal.valueOf(234.56))
                .accountChange(BigDecimal.valueOf(0.2346).setScale(4, RoundingMode.HALF_EVEN))
                .accountBalance(BigDecimal.valueOf(1234.56).setScale(2, RoundingMode.HALF_EVEN))
                .build();

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(balanceService.getCurrentBalance(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Balance.builder().accountBalance(BigDecimal.valueOf(1000)).build());

        when(repository.save(collectionName, calculated)).thenReturn(calculated);
        when(repository.findAll(eq(collectionName), any(PageableRequest.class))).thenReturn(new PageImpl<>(emptyList()));

        Entry entry = entryService.save(ACCESS_TOKEN, JOURNAL_ID, toSave);
        assertThat(entry).isNotNull();

        verify(balanceService).calculateCurrentBalance(any(), anyString());
    }

    @DisplayName("Create a DEPOSIT entry and other entries need balance")
    @Test
    void createDEPOSITBalancing() {
        Entry toSave = Entry.builder()
                .type(EntryType.DEPOSIT)
                .price(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .build();

        Entry calculated = Entry.builder()
                .type(EntryType.DEPOSIT)
                .price(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .netResult(BigDecimal.valueOf(234.56))
                .accountChange(BigDecimal.valueOf(0.2346).setScale(4, RoundingMode.HALF_EVEN))
                .accountBalance(BigDecimal.valueOf(1234.56).setScale(2, RoundingMode.HALF_EVEN))
                .build();

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(balanceService.getCurrentBalance(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Balance.builder().accountBalance(BigDecimal.valueOf(1000)).build());
        when(repository.save(collectionName, calculated)).thenReturn(calculated);

        when(balanceService.calculateCurrentBalance(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Balance.builder().accountBalance(BigDecimal.valueOf(1234.56)).build());

        Entry entry = entryService.save(ACCESS_TOKEN, JOURNAL_ID, toSave);
        assertThat(entry).isNotNull();
    }

    @DisplayName("Delete a entry")
    @Test
    void delete() {
        String entryId = UUID.randomUUID().toString();
        Entry entry = Entry.builder()
                .type(EntryType.DEPOSIT)
                .price(BigDecimal.valueOf(234.56))
                .netResult(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .build();

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(repository.getById(collectionName, entryId)).thenReturn(Optional.of(entry));
        when(repository.findAll(eq(collectionName), any(PageableRequest.class))).thenReturn(new PageImpl<>(emptyList()));

        entryService.delete(ACCESS_TOKEN, JOURNAL_ID, entryId);

        verify(repository).delete(collectionName, entry);

        verify(balanceService).calculateCurrentBalance(any(), anyString());
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

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(repository.getById(collectionName, entryId)).thenReturn(Optional.of(entry));

        when(balanceService.calculateCurrentBalance(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Balance.builder().accountBalance(BigDecimal.valueOf(765.44)).build());

        entryService.delete(ACCESS_TOKEN, JOURNAL_ID, entryId);

        verify(repository).delete(collectionName, entry);
    }

    @DisplayName("Delete a entry not found return an exception")
    @Test
    void deleteNotFound() {
        String entryId = UUID.randomUUID().toString();

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(repository.getById(collectionName, entryId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> entryService.delete(ACCESS_TOKEN, JOURNAL_ID, entryId));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getStatusText()).isEqualTo("Entry not found");

        verify(repository, never()).delete(any(), any());
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

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(repository.getById(collectionName, entryId)).thenReturn(Optional.of(entry));

        entryService.delete(ACCESS_TOKEN, JOURNAL_ID, entryId);

        verify(repository).delete(collectionName, entry);

        verify(balanceService, never()).calculateCurrentBalance(any(), anyString());
    }

    @DisplayName("Save a not finished entry, do not balance other entries")
    @Test
    void saveNotFinished() {
        Entry toSave = Entry.builder()
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .type(EntryType.TRADE)
                .direction(EntryDirection.LONG)
                .price(BigDecimal.valueOf(200))
                .size(BigDecimal.valueOf(2))
                .profitPrice(BigDecimal.valueOf(240))
                .lossPrice(BigDecimal.valueOf(180))
                .build();

        Entry calculated = Entry.builder()
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

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(balanceService.getCurrentBalance(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Balance.builder().accountBalance(BigDecimal.valueOf(1000)).build());

        when(repository.save(collectionName, calculated)).thenReturn(calculated);
        when(repository.findAll(eq(collectionName), any(PageableRequest.class))).thenReturn(new PageImpl<>(emptyList()));

        Entry entry = entryService.save(ACCESS_TOKEN, JOURNAL_ID, toSave);
        assertThat(entry).isNotNull();

        verify(balanceService, never()).calculateCurrentBalance(any(), anyString());
    }

    @DisplayName("Save image before")
    @Test
    void imageBefore() throws IOException {
        String entryId = UUID.randomUUID().toString();
        Entry entry = Entry.builder()
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .build();
        MultipartFile file = mock(MultipartFile.class);

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(repository.getById(collectionName, entryId)).thenReturn(Optional.of(entry));
        when(file.getBytes()).thenReturn(new byte[]{0});
        when(repository.save(argThat(save -> nonNull(entry.getScreenshotBefore()) && isNull(entry.getScreenshotAfter())))).thenReturn(entry);

        entryService.uploadImage(ACCESS_TOKEN, JOURNAL_ID, entryId, UploadType.IMAGE_BEFORE, file);
    }

    @DisplayName("Save image after")
    @Test
    void imageAfter() throws IOException {
        String entryId = UUID.randomUUID().toString();
        Entry entry = Entry.builder()
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .build();
        MultipartFile file = mock(MultipartFile.class);

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(repository.getById(collectionName, entryId)).thenReturn(Optional.of(entry));
        when(file.getBytes()).thenReturn(new byte[]{0});
        when(repository.save(argThat(save -> isNull(entry.getScreenshotBefore()) && nonNull(entry.getScreenshotAfter())))).thenReturn(entry);

        entryService.uploadImage(ACCESS_TOKEN, JOURNAL_ID, entryId, UploadType.IMAGE_AFTER, file);
    }

    @DisplayName("Return image before")
    @Test
    void getImageBefore() {
        String entryId = UUID.randomUUID().toString();
        Entry entry = Entry.builder()
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .screenshotBefore("image")
                .build();

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(repository.getById(collectionName, entryId)).thenReturn(Optional.of(entry));

        EntryImageResponse response = entryService.returnImage(ACCESS_TOKEN, JOURNAL_ID, entryId, UploadType.IMAGE_BEFORE);
        assertThat(response.getImage()).isNotNull();
    }

    @DisplayName("Return image after")
    @Test
    void getImageAfter() {
        String entryId = UUID.randomUUID().toString();
        Entry entry = Entry.builder()
                .type(EntryType.TRADE)
                .price(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .screenshotAfter("image")
                .build();

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(repository.getById(collectionName, entryId)).thenReturn(Optional.of(entry));

        EntryImageResponse response = entryService.returnImage(ACCESS_TOKEN, JOURNAL_ID, entryId, UploadType.IMAGE_AFTER);
        assertThat(response.getImage()).isNotNull();
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

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(repository.getById(collectionName, entryId)).thenReturn(Optional.of(entry));


        Entry byId = entryService.getById(ACCESS_TOKEN, JOURNAL_ID, entryId);

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
                .strategyIds(asList("ST1", "ST2"))
                .build();

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(repository.getById(collectionName, entryId)).thenReturn(Optional.of(entry));
        when(strategyService.getById(ACCESS_TOKEN, "ST1")).thenReturn(Optional.of(Strategy.builder().id("ST1").name("Strategy 1").build()));
        when(strategyService.getById(ACCESS_TOKEN, "ST2")).thenReturn(Optional.of(Strategy.builder().id("ST2").name("Strategy 2").build()));

        Entry byId = entryService.getById(ACCESS_TOKEN, JOURNAL_ID, entryId);

        assertThat(byId).isEqualTo(entry);
        assertThat(byId.getStrategies()).extracting(Strategy::getId).containsExactlyInAnyOrder("ST1", "ST2");
        assertThat(byId.getStrategies()).extracting(Strategy::getName).containsExactlyInAnyOrder("Strategy 1", "Strategy 2");
    }

    @DisplayName("Get a entry by id with strategies but one does not exist, return only one")
    @Test
    void getByIdWithStrategiesInvalidOne() {
        String entryId = UUID.randomUUID().toString();
        Entry entry = Entry.builder()
                .type(EntryType.DEPOSIT)
                .price(BigDecimal.valueOf(234.56))
                .netResult(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .strategyIds(asList("ST1", "ST2"))
                .build();

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(repository.getById(collectionName, entryId)).thenReturn(Optional.of(entry));
        when(strategyService.getById(ACCESS_TOKEN, "ST1")).thenReturn(Optional.of(Strategy.builder().id("ST1").name("Strategy 1").build()));
        when(strategyService.getById(ACCESS_TOKEN, "ST2")).thenReturn(Optional.empty());

        Entry byId = entryService.getById(ACCESS_TOKEN, JOURNAL_ID, entryId);

        assertThat(byId).isEqualTo(entry);
        assertThat(byId.getStrategies()).extracting(Strategy::getId).containsExactly("ST1");
        assertThat(byId.getStrategies()).extracting(Strategy::getName).containsExactly("Strategy 1");
    }

    @DisplayName("Get a entry by id with strategies none exist return strategies empty")
    @Test
    void getByIdWithStrategiesAllInvalid() {
        String entryId = UUID.randomUUID().toString();
        Entry entry = Entry.builder()
                .type(EntryType.DEPOSIT)
                .price(BigDecimal.valueOf(234.56))
                .netResult(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .strategyIds(asList("ST1", "ST2"))
                .build();

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(repository.getById(collectionName, entryId)).thenReturn(Optional.of(entry));
        when(strategyService.getById(ACCESS_TOKEN, "ST1")).thenReturn(Optional.empty());
        when(strategyService.getById(ACCESS_TOKEN, "ST2")).thenReturn(Optional.empty());

        Entry byId = entryService.getById(ACCESS_TOKEN, JOURNAL_ID, entryId);

        assertThat(byId).isEqualTo(entry);
        assertThat(byId.getStrategies()).isNull();
    }

    @DisplayName("Get a entry by id not found return an exception")
    @Test
    void getByIdNotFound() {
        String entryId = UUID.randomUUID().toString();

        when(journalService.get(ACCESS_TOKEN, JOURNAL_ID)).thenReturn(Journal.builder().name("my-journal").build());
        when(repository.getById(collectionName, entryId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> entryService.getById(ACCESS_TOKEN, JOURNAL_ID, entryId));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getStatusText()).isEqualTo("Entry not found");
    }
}