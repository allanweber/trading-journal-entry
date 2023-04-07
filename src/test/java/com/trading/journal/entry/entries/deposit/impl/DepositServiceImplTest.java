package com.trading.journal.entry.entries.deposit.impl;

import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.EntryService;
import com.trading.journal.entry.entries.EntryType;
import com.trading.journal.entry.entries.deposit.Deposit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class DepositServiceImplTest {

    private static final String JOURNAL_ID = UUID.randomUUID().toString();

    @Mock
    EntryService entryService;

    @InjectMocks
    DepositServiceImpl depositService;

    @DisplayName("Create a entry from a Deposit")
    @Test
    void create() {
        Deposit deposit = Deposit.builder()
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .price(BigDecimal.valueOf(200.21))
                .build();

        Entry entry = Entry.builder()
                .journalId(JOURNAL_ID)
                .type(EntryType.DEPOSIT)
                .date(LocalDateTime.of(2022, 9, 20, 15, 30, 50))
                .price(BigDecimal.valueOf(200.21))
                .build();

        when(entryService.save(entry)).thenReturn(entry);

        Entry entryCreated = depositService.create(JOURNAL_ID, deposit);

        assertThat(entryCreated).isNotNull();
    }
}