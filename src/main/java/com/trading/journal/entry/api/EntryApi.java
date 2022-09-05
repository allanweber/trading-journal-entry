package com.trading.journal.entry.api;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.queries.data.PageResponse;
import io.swagger.annotations.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "Entries Api")
@RequestMapping("/entry")
public interface EntryApi {

    String DESCRIPTION = "Available filters:eq (Equal), gt(Greater than), gte(Greater than or equal), lt(Less than), lte(Less than or equal). eg. 'FieldName.Operation', 'Value'";

    @ApiOperation(notes = "Get all entries", value = "Get all entries")
    @ApiResponses(@ApiResponse(code = 200, message = "Entries retrieved"))
    @GetMapping("{journal-id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<PageResponse<Entry>> getAll(
            AccessTokenInfo accessTokenInfo,
            @PathVariable(name = "journal-id") String journalId,
            @RequestParam(value = "page", defaultValue = "0", required = false) Integer page,
            @RequestParam(value = "size", defaultValue = "100", required = false) Integer size,
            @ApiParam(name = "sort", value = "eg. \"id,asc\", \"name,desc\"") @RequestParam(value = "sort", required = false) String[] sort,
            @ApiParam(name = "filter", value = DESCRIPTION) @RequestParam(value = "filter", required = false) String[] filter);

    @ApiOperation(notes = "Create new entry", value = "Create new entry")
    @ApiResponses(@ApiResponse(code = 201, message = "Entry created"))
    @PostMapping("{journal-id}")
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<Entry> create(AccessTokenInfo accessTokenInfo, @PathVariable(name = "journal-id") String journalId, @RequestBody @Valid Entry data);
}
