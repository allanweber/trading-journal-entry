package com.trading.journal.entry.api;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.deposit.Deposit;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@Api(tags = "Deposit Api")
@RequestMapping("/journals/{journal-id}/entries/deposit")
public interface DepositApi {

    @ApiOperation(notes = "Create Deposit", value = "Create Deposit")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Deposit created")
    })
    @PostMapping()
    ResponseEntity<Entry> create(AccessTokenInfo accessTokenInfo, @PathVariable(name = "journal-id") String journalId, @RequestBody @Valid Deposit deposit);
}
