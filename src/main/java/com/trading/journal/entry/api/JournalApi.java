package com.trading.journal.entry.api;

import com.trading.journal.entry.balance.Balance;
import com.trading.journal.entry.journal.Journal;
import com.trading.journal.entry.journal.JournalData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "Journal Api")
@RequestMapping("/journals")
public interface JournalApi {

    @ApiOperation(notes = "Get all journals", value = "Get all journals")
    @ApiResponses(@ApiResponse(code = 200, message = "Journals retrieved"))
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<List<JournalData>> getAll();

    @ApiOperation(notes = "Get a journal", value = "Get a journal")
    @ApiResponses(@ApiResponse(code = 200, message = "Journal retrieved"))
    @GetMapping("/{journal-id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<JournalData> get(@PathVariable(name = "journal-id") String journalId);

    @ApiOperation(notes = "Create new journal", value = "Create new journal")
    @ApiResponses(@ApiResponse(code = 201, message = "Journal created"))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<JournalData> save(@RequestBody @Valid Journal data);

    @ApiOperation(notes = "Delete a journal", value = "Delete a journal")
    @ApiResponses(@ApiResponse(code = 200, message = "Journal deleted"))
    @DeleteMapping("/{journal-id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Void> delete(@PathVariable(name = "journal-id") String journalId);

    @ApiOperation(notes = "Get a journal", value = "Get a journal")
    @ApiResponses(@ApiResponse(code = 200, message = "Journal retrieved"))
    @GetMapping("/{journal-id}/balance")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Balance> balance(@PathVariable(name = "journal-id") String journalId);
}
