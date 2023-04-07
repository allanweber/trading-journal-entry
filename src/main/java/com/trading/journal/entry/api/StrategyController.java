package com.trading.journal.entry.api;

import com.trading.journal.entry.ApplicationException;
import com.trading.journal.entry.strategy.Strategy;
import com.trading.journal.entry.strategy.StrategyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class StrategyController implements StrategyApi {

    private final StrategyService strategyService;

    @Override
    public ResponseEntity<PageWrapper<Strategy>> getAll(Pageable pageable) {
        Page<Strategy> strategies = strategyService.getAll(pageable);
        return ok(new PageWrapper<>(strategies));
    }

    @Override
    public ResponseEntity<Strategy> save(Strategy strategy) {
        boolean isNew = Objects.isNull(strategy.getId());
        Strategy saved = strategyService.save(strategy);
        ResponseEntity<Strategy> response;
        if (isNew) {
            URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(saved.getId()).toUri();
            response = created(uri).body(saved);
        } else {
            response = ok(saved);
        }
        return response;
    }

    @Override
    public ResponseEntity<Strategy> getById(String strategyId) {
        Optional<Strategy> strategy = strategyService.getById(strategyId);
        return strategy.map(ResponseEntity::ok)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Strategy not found"));
    }

    @Override
    public ResponseEntity<Void> delete(String strategyId) {
        strategyService.delete(strategyId);
        return ok().build();
    }
}
