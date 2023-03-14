package com.trading.journal.entry.api;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.trade.*;
import com.trading.journal.entry.entries.trade.aggregate.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;

@RequiredArgsConstructor
@RestController
public class TradeController implements TradeApi {

    private final TradeService tradeService;

    private final AggregateService aggregateService;

    @Override
    public ResponseEntity<Entry> open(AccessTokenInfo accessTokenInfo, String journalId, Trade trade) {
        Entry created = tradeService.open(accessTokenInfo, journalId, trade);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.getId()).toUri();
        return created(uri).body(created);
    }

    @Override
    public ResponseEntity<Entry> update(AccessTokenInfo accessTokenInfo, String journalId, String tradeId, Trade trade) {
        Entry updated = tradeService.update(accessTokenInfo, journalId, tradeId, trade);
        return ok(updated);
    }

    @Override
    public ResponseEntity<Entry> close(AccessTokenInfo accessTokenInfo, String journalId, String tradeId, CloseTrade trade) {
        Entry closed = tradeService.close(accessTokenInfo, journalId, tradeId, trade);
        return ok(closed);
    }

    @Override
    public ResponseEntity<OpenTrades> countOpen(AccessTokenInfo accessTokenInfo, String journalId) {
        long open = tradeService.countOpen(accessTokenInfo, journalId);
        return ok(OpenTrades.builder().trades(open).build());
    }

    @Override
    public ResponseEntity<List<Symbol>> symbols(AccessTokenInfo accessTokenInfo, String journalId) {
        List<Symbol> symbols = tradeService.symbols(accessTokenInfo, journalId);
        return ok(symbols);
    }

    @Override
    public ResponseEntity<PeriodAggregatedResult> time(AccessTokenInfo accessTokenInfo, String journalId, AggregateType aggregation, Long page, Long size) {
        AggregateTrade aggregateTrade = new AggregateTrade(aggregation, page, size);
        PeriodAggregatedResult periods = aggregateService.aggregatePeriod(accessTokenInfo, journalId, aggregateTrade);
        return ok(periods);
    }

    @Override
    public ResponseEntity<List<TradesAggregated>> trades(AccessTokenInfo accessTokenInfo, String journalId, String from, String until) {
        AggregateTrade aggregateTrade = new AggregateTrade(from, until);
        List<TradesAggregated> trades = aggregateService.aggregateTrades(accessTokenInfo, journalId, aggregateTrade);
        return ok(trades);
    }
}
