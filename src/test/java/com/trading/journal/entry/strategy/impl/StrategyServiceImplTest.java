package com.trading.journal.entry.strategy.impl;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.ApplicationException;
import com.trading.journal.entry.queries.CollectionName;
import com.trading.journal.entry.strategy.Strategy;
import com.trading.journal.entry.strategy.StrategyRepository;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class StrategyServiceImplTest {

    private static final AccessTokenInfo ACCESS_TOKEN = new AccessTokenInfo("subject", 1L, "TENANCY", emptyList());

    private static CollectionName collectionName;

    @Mock
    StrategyRepository strategyRepository;

    @InjectMocks
    StrategyServiceImpl strategyService;

    @BeforeAll
    static void setUp() {
        collectionName = new CollectionName(ACCESS_TOKEN);
    }

    @DisplayName("Get strategies")
    @Test
    void all() {
        when(strategyRepository.getAll(collectionName)).thenReturn(asList(Strategy.builder().build(), Strategy.builder().build()));
        List<Strategy> all = strategyService.getAll(ACCESS_TOKEN);
        assertThat(all).isNotEmpty();
    }

    @DisplayName("Save a new strategy")
    @Test
    void saveNew() {
        Strategy strategy = Strategy.builder().name("ST1").build();
        when(strategyRepository.save(collectionName, strategy)).thenReturn(Strategy.builder().id("1").name("ST1").build());
        Strategy saved = strategyService.save(ACCESS_TOKEN, strategy);
        assertThat(saved.getId()).isEqualTo("1");
    }

    @DisplayName("Update strategy")
    @Test
    void updated() {
        Strategy strategy = Strategy.builder().id("1").name("ST1").build();
        when(strategyRepository.save(collectionName, strategy)).thenReturn(Strategy.builder().id("1").name("ST1").build());
        Strategy saved = strategyService.save(ACCESS_TOKEN, strategy);
        assertThat(saved.getId()).isEqualTo("1");
    }

    @DisplayName("Get strategy by id")
    @Test
    void getById() {
        String id = "1";
        when(strategyRepository.getById(collectionName, id)).thenReturn(Optional.of(Strategy.builder().id("1").name("ST1").build()));
        Optional<Strategy> byId = strategyService.getById(ACCESS_TOKEN, id);
        assertThat(byId).isPresent();
    }

    @DisplayName("Get strategy by id not found return empty")
    @Test
    void getByIdNotFound() {
        String id = "1";
        when(strategyRepository.getById(collectionName, id)).thenReturn(Optional.empty());
        Optional<Strategy> byId = strategyService.getById(ACCESS_TOKEN, id);
        assertThat(byId).isNotPresent();
    }

    @DisplayName("Delete strategy by id")
    @Test
    void delete() {
        String id = "1";
        Strategy strategy = Strategy.builder().id("1").name("ST1").build();
        when(strategyRepository.getById(collectionName, id)).thenReturn(Optional.of(strategy));
        when(strategyRepository.hasItems(collectionName)).thenReturn(false);
        strategyService.delete(ACCESS_TOKEN, id);
        verify(strategyRepository).delete(collectionName, strategy);
        verify(strategyRepository).drop(collectionName);
    }

    @DisplayName("Delete strategy by id not found, throw an exception")
    @Test
    void deleteNotFound() {
        String id = "1";
        when(strategyRepository.getById(collectionName, id)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> strategyService.delete(ACCESS_TOKEN, id));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getStatusText()).isEqualTo("Strategy not found");

        verify(strategyRepository, never()).delete(any(), any());
        verify(strategyRepository, never()).drop(any());
    }
}