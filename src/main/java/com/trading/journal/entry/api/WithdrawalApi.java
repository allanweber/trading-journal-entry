package com.trading.journal.entry.api;

import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.withdrawal.Withdrawal;
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

@Api(tags = "Withdrawal Api")
@RequestMapping("/journals/{journal-id}/entries/withdrawal")
public interface WithdrawalApi {

    @ApiOperation(notes = "Create Withdrawal", value = "Create Withdrawal")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Withdrawal created")
    })
    @PostMapping()
    ResponseEntity<Entry> create(@PathVariable(name = "journal-id") String journalId, @RequestBody @Valid Withdrawal withdrawal);
}
