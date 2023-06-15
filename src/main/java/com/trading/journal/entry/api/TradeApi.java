package com.trading.journal.entry.api;

import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.trade.CloseTrade;
import com.trading.journal.entry.entries.trade.OpenTrades;
import com.trading.journal.entry.entries.trade.Symbol;
import com.trading.journal.entry.entries.trade.Trade;
import com.trading.journal.entry.entries.trade.aggregate.AggregateType;
import com.trading.journal.entry.entries.trade.aggregate.PeriodAggregatedResult;
import com.trading.journal.entry.entries.trade.aggregate.TradesAggregated;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/journals/{journal-id}/entries/trade")
public interface TradeApi {

    @PostMapping()
    ResponseEntity<Entry> open(@PathVariable(name = "journal-id") String journalId, @RequestBody @Valid Trade trade);

    @PatchMapping("/{trade-id}")
    ResponseEntity<Entry> update(@PathVariable(name = "trade-id") String tradeId, @RequestBody @Valid Trade trade);

    @PatchMapping("/{trade-id}/close")
    ResponseEntity<Entry> close(@PathVariable(name = "trade-id") String tradeId, @RequestBody @Valid CloseTrade trade);

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/open")
    ResponseEntity<OpenTrades> countOpen(@PathVariable(name = "journal-id") String journalId);

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/symbols")
    ResponseEntity<List<Symbol>> symbols(@PathVariable(name = "journal-id") String journalId);

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/aggregate/time")
    ResponseEntity<PeriodAggregatedResult> time(@PathVariable(name = "journal-id") String journalId,
                                                @RequestParam("aggregation") AggregateType aggregation,
                                                @RequestParam(value = "page", defaultValue = "0", required = false) Long page,
                                                @RequestParam(value = "size", defaultValue = "10", required = false) Long size);

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/aggregate/trade")
    ResponseEntity<List<TradesAggregated>> trades(@PathVariable(name = "journal-id") String journalId,
                                                  @RequestParam("from") String from,
                                                  @RequestParam("until") String until);
}
