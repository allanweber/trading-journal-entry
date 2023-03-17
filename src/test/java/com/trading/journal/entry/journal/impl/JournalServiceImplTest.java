package com.trading.journal.entry.journal.impl;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.ApplicationException;
import com.trading.journal.entry.balance.Balance;
import com.trading.journal.entry.journal.Currency;
import com.trading.journal.entry.journal.Journal;
import com.trading.journal.entry.journal.JournalRepository;
import com.trading.journal.entry.queries.CollectionName;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class JournalServiceImplTest {

    private static AccessTokenInfo accessToken;

    private static CollectionName collectionName;

    @Mock
    JournalRepository journalRepository;

    @Mock
    MongoOperations mongoOperations;

    @InjectMocks
    JournalServiceImpl journalService;

    @BeforeAll
    static void setUp() {
        accessToken = new AccessTokenInfo("subject", 1L, "TENANCY", emptyList());
        collectionName = new CollectionName(accessToken);
    }

    @DisplayName("Get all journals")
    @Test
    void getAll() {
        Journal mockJournal = buildJournal("1", "journal", 1);
        when(journalRepository.getAll(collectionName)).thenReturn(singletonList(mockJournal));
        List<Journal> journals = journalService.getAll(accessToken);
        assertThat(journals).isNotEmpty();
    }

    @DisplayName("Get a journal by id")
    @Test
    void geById() {
        Journal mockJournal = buildJournal("1", "journal", 1);
        when(journalRepository.getById(collectionName, "123456")).thenReturn(Optional.of(mockJournal));
        Journal journal = journalService.get(accessToken, "123456");
        assertThat(journal).isNotNull();
    }

    @DisplayName("Get a journal by id does not exist")
    @Test
    void geByIdNotExist() {
        when(journalRepository.getById(collectionName, "123456")).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> journalService.get(accessToken, "123456"));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getStatusText()).isEqualTo("Journal not found");
    }

    @DisplayName("Save a journal")
    @Test
    void save() {
        Journal mockJournal = buildJournal("1", "journal", 1);
        when(journalRepository.save(collectionName, mockJournal)).thenReturn(mockJournal);
        Journal journal = journalService.save(accessToken, mockJournal);
        assertThat(journal).isNotNull();
    }

    @DisplayName("Save a journal with same name throw an exception")
    @Test
    void saveSameName() {
        Journal to_save = buildJournal(null, "to save", 1);

        Query query = Query.query(Criteria.where("name").is("to save").and("id").ne(null));
        when(journalRepository.find(collectionName, query)).thenReturn(singletonList(buildJournal("1", "journal", 1)));

        ApplicationException exception = assertThrows(ApplicationException.class, () -> journalService.save(accessToken, to_save));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getStatusText()).isEqualTo("There is already another journal with the same name");

        verify(journalRepository, never()).save(any(), any());
    }

    @DisplayName("Delete a journal and do not drop collection because there is more items in it")
    @Test
    void delete() {
        Journal to_delete = buildJournal("123", "to_delete", 1);
        when(journalRepository.getById(collectionName, "123")).thenReturn(Optional.of(to_delete));
        when(journalRepository.delete(collectionName, to_delete)).thenReturn(1L);
        when(journalRepository.hasItems(collectionName)).thenReturn(true);

        long delete = journalService.delete(accessToken, "123");
        assertThat(delete).isPositive();

        verify(mongoOperations).dropCollection("Tenancy_to_delete_entries");
        verify(journalRepository, never()).drop(collectionName);
    }

    @DisplayName("Delete a journal and drop collection because there is no more items in it")
    @Test
    void deleteAndDrop() {
        Journal to_delete = buildJournal("123", "to_delete", 1);
        when(journalRepository.getById(collectionName, "123")).thenReturn(Optional.of(to_delete));
        when(journalRepository.delete(collectionName, to_delete)).thenReturn(1L);
        when(journalRepository.hasItems(collectionName)).thenReturn(false);

        long delete = journalService.delete(accessToken, "123");
        assertThat(delete).isPositive();

        verify(mongoOperations).dropCollection("Tenancy_to_delete_entries");
        verify(journalRepository).drop(collectionName);
    }

    @DisplayName("Delete a journal does not exist")
    @Test
    void deleteNotExist() {
        when(journalRepository.getById(collectionName, "123456")).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> journalService.delete(accessToken, "123456"));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getStatusText()).isEqualTo("Journal not found");

        verify(mongoOperations, never()).dropCollection(anyString());
        verify(journalRepository, never()).delete(any(), any());
        verify(journalRepository, never()).hasItems(any());
        verify(journalRepository, never()).drop(any());
    }

    @DisplayName("Update balance")
    @Test
    void updateBalance() {
        Journal saved = buildJournal("123", "saved", 100);
        when(journalRepository.getById(collectionName, "123")).thenReturn(Optional.of(saved));

        Balance balance = Balance.builder()
                .accountBalance(BigDecimal.ZERO)
                .closedPositions(BigDecimal.ZERO)
                .deposits(BigDecimal.ZERO)
                .withdrawals(BigDecimal.ZERO)
                .taxes(BigDecimal.ZERO)
                .build();

        Journal toSave = Journal.builder()
                .id(saved.getId())
                .name(saved.getName())
                .startBalance(saved.getStartBalance())
                .currentBalance(balance)
                .lastBalance(LocalDateTime.now())
                .build();

        when(journalRepository.save(eq(collectionName),
                argThat(journal ->
                        journal.getId().equals(saved.getId()) &&
                                journal.getName().equals(saved.getName()) &&
                                journal.getStartBalance().equals(saved.getStartBalance()) &&
                                journal.getCurrentBalance().equals(balance)
                )
        )).thenReturn(toSave);

        journalService.updateBalance(accessToken, "123", balance);
    }

    @DisplayName("Update balance journal id not found return exception")
    @Test
    void updateBalanceJournalNotFound() {
        when(journalRepository.getById(collectionName, "123")).thenReturn(Optional.empty());

        Balance balance = Balance.builder()
                .accountBalance(BigDecimal.ZERO)
                .closedPositions(BigDecimal.ZERO)
                .deposits(BigDecimal.ZERO)
                .withdrawals(BigDecimal.ZERO)
                .taxes(BigDecimal.ZERO)
                .build();

        ApplicationException exception = assertThrows(ApplicationException.class, () -> journalService.updateBalance(accessToken, "123", balance));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getStatusText()).isEqualTo("Journal not found");

        verify(journalRepository, never()).save(any(), any());
    }

    private static Journal buildJournal(String id, String name, double balance) {
        return Journal.builder()
                .id(id)
                .name(name)
                .currency(Currency.DOLLAR)
                .startJournal(LocalDateTime.now())
                .startBalance(BigDecimal.valueOf(balance))
                .currentBalance(Balance.builder()
                        .accountBalance(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                        .taxes(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                        .withdrawals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                        .deposits(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                        .closedPositions(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                        .openedPositions(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                        .available(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                        .build())
                .build();
    }
}