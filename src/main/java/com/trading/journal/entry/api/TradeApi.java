package com.trading.journal.entry.api;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.trade.OpenTrades;
import com.trading.journal.entry.entries.trade.Symbol;
import com.trading.journal.entry.entries.trade.Trade;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "Trade Api")
@RequestMapping("/journals/{journal-id}/entries/trade")
public interface TradeApi {

    @ApiOperation(notes = "Create Trade", value = "Create Trade")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Trade created")
    })
    @PostMapping()
    ResponseEntity<Entry> create(AccessTokenInfo accessTokenInfo, @PathVariable(name = "journal-id") String journalId, @RequestBody @Valid Trade trade);

    @ApiOperation(notes = "Update Trade", value = "Update Trade")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trade updated")
    })
    @PatchMapping("/{trade-id}")
    ResponseEntity<Entry> update(AccessTokenInfo accessTokenInfo, @PathVariable(name = "journal-id") String journalId, @PathVariable(name = "trade-id") String tradeId, @RequestBody @Valid Trade trade);

    @ApiOperation(notes = "Count open trades", value = "Retrieve number of open trades")
    @ApiResponses(@ApiResponse(code = 200, message = "Open trades"))
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/open")
    ResponseEntity<OpenTrades> countOpen(AccessTokenInfo accessTokenInfo, @PathVariable(name = "journal-id") String journalId);

    @ApiOperation(notes = "Retrieve all symbols", value = "Retrieve all unique symbols ever trades")
    @ApiResponses(@ApiResponse(code = 200, message = "Symbols retrieve"))
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/symbols")
    ResponseEntity<List<Symbol>> symbols(AccessTokenInfo accessTokenInfo, @PathVariable(name = "journal-id") String journalId);
}
