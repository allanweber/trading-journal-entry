package com.trading.journal.entry.api;

import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.trade.CloseTrade;
import com.trading.journal.entry.entries.trade.OpenTrades;
import com.trading.journal.entry.entries.trade.Symbol;
import com.trading.journal.entry.entries.trade.Trade;
import com.trading.journal.entry.entries.trade.aggregate.AggregateType;
import com.trading.journal.entry.entries.trade.aggregate.PeriodAggregatedResult;
import com.trading.journal.entry.entries.trade.aggregate.TradesAggregated;
import io.swagger.annotations.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "Trade Api")
@RequestMapping("/journals/{journal-id}/entries/trade")
public interface TradeApi {

    @ApiOperation(notes = "Open Trade", value = "Open Trade")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Trade opened")
    })
    @PostMapping()
    ResponseEntity<Entry> open(@PathVariable(name = "journal-id") String journalId, @RequestBody @Valid Trade trade);

    @ApiOperation(notes = "Update Trade", value = "Update Trade")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trade updated")
    })
    @PatchMapping("/{trade-id}")
    ResponseEntity<Entry> update(@PathVariable(name = "journal-id") String journalId, @PathVariable(name = "trade-id") String tradeId, @RequestBody @Valid Trade trade);

    @ApiOperation(notes = "Close Trade", value = "Close Trade")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trade closed")
    })
    @PatchMapping("/{trade-id}/close")
    ResponseEntity<Entry> close(@PathVariable(name = "journal-id") String journalId, @PathVariable(name = "trade-id") String tradeId, @RequestBody @Valid CloseTrade trade);

    @ApiOperation(notes = "Count open trades", value = "Retrieve number of open trades")
    @ApiResponses(@ApiResponse(code = 200, message = "Open trades"))
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/open")
    ResponseEntity<OpenTrades> countOpen(@PathVariable(name = "journal-id") String journalId);

    @ApiOperation(notes = "Retrieve all symbols", value = "Retrieve all unique symbols ever trades")
    @ApiResponses(@ApiResponse(code = 200, message = "Symbols retrieve"))
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/symbols")
    ResponseEntity<List<Symbol>> symbols(@PathVariable(name = "journal-id") String journalId);

    @ApiOperation(notes = "Aggregate time periods where there is a trade", value = "Aggregate time periods where there is a trade")
    @ApiResponses(@ApiResponse(code = 200, message = "Period aggregated"))
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/aggregate/time")
    ResponseEntity<PeriodAggregatedResult> time(@PathVariable(name = "journal-id") String journalId,
                                                @ApiParam(name = "aggregation", value = "DAY, WEEK or MONTH") @RequestParam("aggregation") AggregateType aggregation,
                                                @RequestParam(value = "page", defaultValue = "0", required = false) Long page,
                                                @RequestParam(value = "size", defaultValue = "10", required = false) Long size);

    @ApiOperation(notes = "Aggregate trades by period of time", value = "Aggregate trades by period of time")
    @ApiResponses(@ApiResponse(code = 200, message = "Trades aggregated"))
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/aggregate/trade")
    ResponseEntity<List<TradesAggregated>> trades(@PathVariable(name = "journal-id") String journalId,
                                                  @ApiParam(name = "from", value = "Start date for aggregation") @RequestParam("from") String from,
                                                  @ApiParam(name = "until", value = "End date for aggregation") @RequestParam("until") String until);
}
