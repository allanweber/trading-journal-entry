package com.trading.journal.entry.strategy.impl;

import com.trading.journal.entry.ApplicationException;
import com.trading.journal.entry.entries.EntryService;
import com.trading.journal.entry.strategy.Strategy;
import com.trading.journal.entry.strategy.StrategyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class StrategyServiceImplTest {

    @Mock
    StrategyRepository strategyRepository;

    @Mock
    EntryService entryService;

    @InjectMocks
    StrategyServiceImpl strategyService;

    @DisplayName("Get strategies")
    @Test
    void all() {
        PageRequest pageable = PageRequest.of(0, Integer.MAX_VALUE);
        when(strategyRepository.findAll(pageable)).thenReturn(new PageImpl<>(asList(Strategy.builder().build(), Strategy.builder().build()), pageable, 2L));
        Page<Strategy> all = strategyService.getAll(pageable);
        assertThat(all.get()).isNotEmpty();
        assertThat(all.getTotalElements()).isEqualTo(2L);
    }

    @DisplayName("Save a new strategy")
    @Test
    void saveNew() {
        Strategy strategy = Strategy.builder().name("ST1").build();
        when(strategyRepository.save(strategy)).thenReturn(Strategy.builder().id("1").name("ST1").build());
        Strategy saved = strategyService.save(strategy);
        assertThat(saved.getId()).isEqualTo("1");
    }

    @DisplayName("Update strategy")
    @Test
    void updated() {
        Strategy strategy = Strategy.builder().id("1").name("ST1").build();
        when(strategyRepository.save(strategy)).thenReturn(Strategy.builder().id("1").name("ST1").build());
        Strategy saved = strategyService.save(strategy);
        assertThat(saved.getId()).isEqualTo("1");
    }

    @DisplayName("Get strategy by id")
    @Test
    void getById() {
        String id = "1";
        when(strategyRepository.getById(id)).thenReturn(Optional.of(Strategy.builder().id("1").name("ST1").build()));
        Optional<Strategy> byId = strategyService.getById(id);
        assertThat(byId).isPresent();
    }

    @DisplayName("Get strategy by id not found return empty")
    @Test
    void getByIdNotFound() {
        String id = "1";
        when(strategyRepository.getById(id)).thenReturn(Optional.empty());
        Optional<Strategy> byId = strategyService.getById(id);
        assertThat(byId).isNotPresent();
    }

    @DisplayName("Delete strategy by id")
    @Test
    void delete() {
        String id = "1";
        Strategy strategy = Strategy.builder().id("1").name("ST1").build();
        when(strategyRepository.getById(id)).thenReturn(Optional.of(strategy));
        when(strategyRepository.hasItems()).thenReturn(false);
        strategyService.delete(id);
        verify(strategyRepository).delete(strategy);
        verify(strategyRepository).drop();
    }

    @DisplayName("Delete strategy by id not found, throw an exception")
    @Test
    void deleteNotFound() {
        String id = "1";
        when(strategyRepository.getById(id)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> strategyService.delete(id));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getStatusText()).isEqualTo("Strategy not found");

        verify(strategyRepository, never()).delete(any(Strategy.class));
        verify(strategyRepository, never()).drop();
    }

    @DisplayName("Delete strategy by id has entries, throw an exception")
    @Test
    void deleteHasTrades(){
        String id = "1";

        when(strategyRepository.getById(id)).thenReturn(Optional.of(Strategy.builder().id("1").build()));
        when(entryService.countByStrategy(id)).thenReturn(1L);

        ApplicationException exception = assertThrows(ApplicationException.class, () -> strategyService.delete(id));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getStatusText()).isEqualTo("Strategy has entries");

        verify(strategyRepository, never()).delete(any(Strategy.class));
        verify(strategyRepository, never()).drop();
    }
}