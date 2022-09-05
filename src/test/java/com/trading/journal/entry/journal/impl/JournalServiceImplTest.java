package com.trading.journal.entry.journal.impl;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.ApplicationException;
import com.trading.journal.entry.journal.Journal;
import com.trading.journal.entry.journal.JournalRepository;
import com.trading.journal.entry.queries.CollectionName;
import com.trading.journal.entry.queries.data.Filter;
import com.trading.journal.entry.queries.data.FilterOperation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
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
        when(journalRepository.getAll(collectionName)).thenReturn(singletonList(Journal.builder().build()));
        List<Journal> journals = journalService.getAll(accessToken);
        assertThat(journals).isNotEmpty();
    }

    @DisplayName("Get a journal by id")
    @Test
    void geById() {
        when(journalRepository.getById(collectionName, "123456")).thenReturn(Optional.of(Journal.builder().build()));
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
        Journal to_save = Journal.builder().name("to save").build();
        when(journalRepository.save(collectionName, to_save)).thenReturn(Journal.builder().build());
        Journal journal = journalService.save(accessToken, to_save);
        assertThat(journal).isNotNull();
    }

    @DisplayName("Save a journal with same name throw an exception")
    @Test
    void saveSameName() {
        Journal to_save = Journal.builder().name("to save").build();

        List<Filter> filters = asList(
                Filter.builder().field("name").operation(FilterOperation.EQUAL).value("to save").build(),
                Filter.builder().field("id").operation(FilterOperation.NOT_EQUAL).value(null).build()
        );
        when(journalRepository.query(collectionName, filters)).thenReturn(singletonList(Journal.builder().build()));

        ApplicationException exception = assertThrows(ApplicationException.class, () -> journalService.save(accessToken, to_save));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getStatusText()).isEqualTo("There is already another journal with the same name");

        verify(journalRepository, never()).save(any(), any());
    }

    @DisplayName("Delete a journal")
    @Test
    void delete() {
        Journal to_delete = Journal.builder().id("123").name("to save").build();
        when(journalRepository.getById(collectionName, "123")).thenReturn(Optional.of(to_delete));
        when(journalRepository.delete(collectionName, to_delete)).thenReturn(1L);
        long delete = journalService.delete(accessToken, "123");
        assertThat(delete).isPositive();
    }

    @DisplayName("Delete a journal does not exist")
    @Test
    void deleteNotExist() {
        when(journalRepository.getById(collectionName, "123456")).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> journalService.delete(accessToken, "123456"));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getStatusText()).isEqualTo("Journal not found");
    }
}