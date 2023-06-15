package com.trading.journal.entry.api;

import com.trading.journal.entry.strategy.Strategy;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/strategies")
public interface StrategyApi {

    @GetMapping()
    ResponseEntity<PageWrapper<Strategy>> getAll(@SortDefault.SortDefaults({@SortDefault(sort = "name", direction = Sort.Direction.ASC)}) Pageable pageable
    );

    @PostMapping()
    ResponseEntity<Strategy> save(@RequestBody @Valid Strategy strategy);

    @GetMapping("/{strategy-id}")
    ResponseEntity<Strategy> getById(@PathVariable(name = "strategy-id") String strategyId);

    @DeleteMapping("/{strategy-id}")
    ResponseEntity<Void> delete(@PathVariable(name = "strategy-id") String strategyId);
}
