package com.trading.journal.entry.entries.impl;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.strategy.Strategy;
import com.trading.journal.entry.strategy.StrategyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class EntryStrategyServiceTest {

    private static final AccessTokenInfo ACCESS_TOKEN = new AccessTokenInfo("subject", 1L, "TENANCY", emptyList());

    @Mock
    StrategyService strategyService;

    @InjectMocks
    EntryStrategyService entryStrategyService;

    @DisplayName("Does nothing when entry.strategies is null")
    @Test
    void nullStrategies() {
        Entry entry = Entry.builder().build();

        List<Strategy> strategies = entryStrategyService.saveStrategy(ACCESS_TOKEN, entry);
        assertThat(strategies).isEmpty();

        verify(strategyService, never()).save(any(), any());
    }

    @DisplayName("Does nothing when entry.strategies is empty")
    @Test
    void emptyStrategies() {
        Entry entry = Entry.builder().strategies(emptyList()).build();

        List<Strategy> strategies = entryStrategyService.saveStrategy(ACCESS_TOKEN, entry);
        assertThat(strategies).isEmpty();

        verify(strategyService, never()).save(any(), any());
    }

    @DisplayName("When entry.strategies has only name create a new strategy")
    @Test
    void newStrategy() {
        Entry entry = Entry.builder()
                .strategies(asList(Strategy.builder().name("ST1").build(), Strategy.builder().name("ST2").build()))
                .build();

        when(strategyService.save(ACCESS_TOKEN, Strategy.builder().name("ST1").build()))
                .thenReturn(Strategy.builder().id("1").name("ST1").build());

        when(strategyService.save(ACCESS_TOKEN, Strategy.builder().name("ST2").build()))
                .thenReturn(Strategy.builder().id("2").name("ST2").build());

        List<Strategy> strategies = entryStrategyService.saveStrategy(ACCESS_TOKEN, entry);
        assertThat(strategies).hasSize(2);
    }

    @DisplayName("When entry.strategies has name and id, but id does not exist, create a new strategy")
    @Test
    void newStrategyWhenInvalidId() {
        Entry entry = Entry.builder()
                .strategies(asList(Strategy.builder().id("1").name("ST1").build(), Strategy.builder().id("2").name("ST2").build()))
                .build();

        when(strategyService.getById(ACCESS_TOKEN, "1")).thenReturn(Optional.empty());
        when(strategyService.getById(ACCESS_TOKEN, "2")).thenReturn(Optional.empty());

        when(strategyService.save(ACCESS_TOKEN, Strategy.builder().id("1").name("ST1").build()))
                .thenReturn(Strategy.builder().id("1").name("ST1").build());

        when(strategyService.save(ACCESS_TOKEN, Strategy.builder().id("2").name("ST2").build()))
                .thenReturn(Strategy.builder().id("2").name("ST2").build());

        List<Strategy> strategies = entryStrategyService.saveStrategy(ACCESS_TOKEN, entry);
        assertThat(strategies).hasSize(2);
    }

    @DisplayName("When entry.strategies has name and id, and id exist, return strategy")
    @Test
    void strategyById() {
        Entry entry = Entry.builder()
                .strategies(asList(Strategy.builder().id("1").name("ST1").build(), Strategy.builder().id("2").name("ST2").build()))
                .build();

        when(strategyService.getById(ACCESS_TOKEN, "1")).thenReturn(Optional.of(Strategy.builder().id("1").name("ST1").build()));
        when(strategyService.getById(ACCESS_TOKEN, "2")).thenReturn(Optional.of(Strategy.builder().id("2").name("ST2").build()));

        List<Strategy> strategies = entryStrategyService.saveStrategy(ACCESS_TOKEN, entry);
        assertThat(strategies).hasSize(2);

        verify(strategyService, never()).save(any(), any());
    }

    @DisplayName("When entry.strategies has name and id, but one id does not exist, execute save for only one strategy")
    @Test
    void strategyByIdOneExist() {
        Entry entry = Entry.builder()
                .strategies(asList(Strategy.builder().id("1").name("ST1").build(), Strategy.builder().id("2").name("ST2").build()))
                .build();

        when(strategyService.getById(ACCESS_TOKEN, "1")).thenReturn(Optional.of(Strategy.builder().id("1").name("ST1").build()));
        when(strategyService.getById(ACCESS_TOKEN, "2")).thenReturn(Optional.empty());

        when(strategyService.save(ACCESS_TOKEN, Strategy.builder().id("2").name("ST2").build()))
                .thenReturn(Strategy.builder().id("2").name("ST2").build());

        List<Strategy> strategies = entryStrategyService.saveStrategy(ACCESS_TOKEN, entry);
        assertThat(strategies).hasSize(2);

        verify(strategyService).save(any(), argThat(strategy -> strategy.getId().equals("2")));
    }
}