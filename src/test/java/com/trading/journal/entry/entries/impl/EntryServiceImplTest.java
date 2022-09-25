package com.trading.journal.entry.entries.impl;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.ApplicationException;
import com.trading.journal.entry.balance.Balance;
import com.trading.journal.entry.balance.BalanceService;
import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.EntryDirection;
import com.trading.journal.entry.entries.EntryRepository;
import com.trading.journal.entry.entries.EntryType;
import com.trading.journal.entry.journal.Journal;
import com.trading.journal.entry.journal.JournalService;
import com.trading.journal.entry.queries.CollectionName;
import com.trading.journal.entry.queries.data.PageResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class EntryServiceImplTest {

    public static String journalId = "123456";
    private static AccessTokenInfo accessToken;

    private static CollectionName collectionName;

    @Mock
    EntryRepository repository;

    @Mock
    JournalService journalService;

    @Mock
    BalanceService balanceService;

    @InjectMocks
    EntryServiceImpl entryService;

    @BeforeAll
    static void setUp() {
        accessToken = new AccessTokenInfo("subject", 1L, "TENANCY", emptyList());
        collectionName = new CollectionName(accessToken, "my-journal");
    }

    @DisplayName("Query entries from a journal")
    @Test
    void query() {
        when(journalService.get(accessToken, journalId)).thenReturn(Journal.builder().name("my-journal").build());

        PageableRequest request = PageableRequest.builder().page(1).build();

        Page<Entry> page = new PageImpl<>(singletonList(Entry.builder().build()), request.pageable(), 1L);
        when(repository.findAll(collectionName, request)).thenReturn(page);

        PageResponse<Entry> response = entryService.query(accessToken, journalId, request);
        assertThat(response.getItems()).isNotEmpty();
        assertThat(response.getTotalItems()).isPositive();
    }

    @DisplayName("Get all entries from a journal")
    @Test
    void all() {
        when(journalService.get(accessToken, journalId)).thenReturn(Journal.builder().name("my-journal").build());

        PageableRequest request = PageableRequest.builder()
                .page(0)
                .size(Integer.MAX_VALUE)
                .sort(Sort.by("date").ascending())
                .build();

        Page<Entry> page = new PageImpl<>(singletonList(Entry.builder().build()), request.pageable(), 1L);
        when(repository.findAll(collectionName, request)).thenReturn(page);

        PageResponse<Entry> response = entryService.query(accessToken, journalId, request);
        assertThat(response.getItems()).isNotEmpty();
        assertThat(response.getTotalItems()).isPositive();
    }

    @DisplayName("Create a TRADE entry")
    @Test
    void createTrade() {
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

        when(journalService.get(accessToken, journalId)).thenReturn(Journal.builder().name("my-journal").build());
        when(balanceService.getCurrentBalance(accessToken, journalId, toSave.getDate())).thenReturn(Balance.builder().accountBalance(BigDecimal.valueOf(1000)).build());

        when(repository.save(collectionName, calculated)).thenReturn(calculated);

        Entry entry = entryService.save(accessToken, journalId, toSave);
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

        when(journalService.get(accessToken, journalId)).thenReturn(Journal.builder().name("my-journal").build());
        when(balanceService.getCurrentBalance(accessToken, journalId, toSave.getDate())).thenReturn(Balance.builder().accountBalance(BigDecimal.valueOf(1000)).build());

        when(repository.save(collectionName, calculated)).thenReturn(calculated);

        Entry entry = entryService.save(accessToken, journalId, toSave);
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

        when(journalService.get(accessToken, journalId)).thenReturn(Journal.builder().name("my-journal").build());
        when(balanceService.getCurrentBalance(accessToken, journalId, toSave.getDate())).thenReturn(Balance.builder().accountBalance(BigDecimal.valueOf(1000)).build());

        when(repository.save(collectionName, calculated)).thenReturn(calculated);

        Entry entry = entryService.save(accessToken, journalId, toSave);
        assertThat(entry).isNotNull();
    }

    @DisplayName("Create a TAXES entry")
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

        when(journalService.get(accessToken, journalId)).thenReturn(Journal.builder().name("my-journal").build());
        when(balanceService.getCurrentBalance(accessToken, journalId, toSave.getDate())).thenReturn(Balance.builder().accountBalance(BigDecimal.valueOf(1000)).build());

        when(repository.save(collectionName, calculated)).thenReturn(calculated);

        Entry entry = entryService.save(accessToken, journalId, toSave);
        assertThat(entry).isNotNull();
    }

    @DisplayName("Delete a entry")
    @Test
    void delete() {
        String entryId = UUID.randomUUID().toString();
        Entry entry = Entry.builder()
                .type(EntryType.DEPOSIT)
                .price(BigDecimal.valueOf(234.56))
                .date(LocalDateTime.of(2022, 9, 8, 15, 31, 23))
                .build();

        when(journalService.get(accessToken, journalId)).thenReturn(Journal.builder().name("my-journal").build());
        when(repository.getById(collectionName, entryId)).thenReturn(Optional.of(entry));

        entryService.delete(accessToken, journalId, entryId);

        verify(repository).delete(collectionName, entry);
    }

    @DisplayName("Delete a entry not found return an exception")
    @Test
    void deleteNotFound() {
        String entryId = UUID.randomUUID().toString();

        when(journalService.get(accessToken, journalId)).thenReturn(Journal.builder().name("my-journal").build());
        when(repository.getById(collectionName, entryId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> entryService.delete(accessToken, journalId, entryId));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getStatusText()).isEqualTo("Entry not found");

        verify(repository, never()).delete(any(),any());
    }
}