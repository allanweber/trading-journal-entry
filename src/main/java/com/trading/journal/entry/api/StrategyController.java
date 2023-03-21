package com.trading.journal.entry.api;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.ApplicationException;
import com.trading.journal.entry.queries.data.PageResponse;
import com.trading.journal.entry.strategy.Strategy;
import com.trading.journal.entry.strategy.StrategyService;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<PageResponse<Strategy>> getAll(AccessTokenInfo accessTokenInfo, int page, int size) {
        PageResponse<Strategy> strategies = strategyService.getAll(accessTokenInfo, page, size);
        return ok(strategies);
    }

    @Override
    public ResponseEntity<Strategy> save(AccessTokenInfo accessTokenInfo, Strategy strategy) {
        boolean isNew = Objects.isNull(strategy.getId());
        Strategy saved = strategyService.save(accessTokenInfo, strategy);
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
    public ResponseEntity<Strategy> getById(AccessTokenInfo accessTokenInfo, String strategyId) {
        Optional<Strategy> strategy = strategyService.getById(accessTokenInfo, strategyId);
        return strategy.map(ResponseEntity::ok)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Strategy not found"));
    }

    @Override
    public ResponseEntity<Void> delete(AccessTokenInfo accessTokenInfo, String strategyId) {
        strategyService.delete(accessTokenInfo, strategyId);
        return ok().build();
    }
}
