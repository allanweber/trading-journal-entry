package com.trading.journal.entry.entries.impl;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.EntryRepository;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class EntryServiceImplTest {

    public static String journalId = "123456";
    private static AccessTokenInfo accessToken;

    private static CollectionName collectionName;

    @Mock
    EntryRepository repository;

    @Mock
    JournalService journalService;

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

    @DisplayName("Create a entry from a journal")
    @Test
    void create() {
        when(journalService.get(accessToken, journalId)).thenReturn(Journal.builder().name("my-journal").build());

        Entry toSave = Entry.builder().symbol("MSFT").build();
        when(repository.save(collectionName, toSave)).thenReturn(Entry.builder().id("123").symbol("MSFT").build());

        Entry entry = entryService.create(accessToken, journalId, toSave);
        assertThat(entry).isNotNull();
    }
}