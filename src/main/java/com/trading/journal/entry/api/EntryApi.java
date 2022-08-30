package com.trading.journal.entry.api;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.pageable.PageResponse;
import io.swagger.annotations.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "Entries Api")
@RequestMapping("/entry")
public interface EntryApi {

    @ApiOperation(notes = "Get all entries", value = "Get all entries")
    @ApiResponses(@ApiResponse(code = 200, message = "Entries retrieved"))
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<PageResponse<Entry>> getAll(
            AccessTokenInfo accessTokenInfo,
            @RequestParam(value = "page", defaultValue = "0", required = false) Integer page,
            @RequestParam(value = "size", defaultValue = "100", required = false) Integer size,
            @ApiParam(name = "sort", value = "eg. \"id,asc\", \"name,desc\"") @RequestParam(value = "sort", required = false) String[] sort,
            @ApiParam(name = "filter", value = "eg. \"id,1\", \"name,abc\"") @RequestParam(value = "filter", required = false) String[] filter);

    @ApiOperation(notes = "Create new entry", value = "Create new entry")
    @ApiResponses(@ApiResponse(code = 201, message = "Entry created"))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<Entry> create(AccessTokenInfo accessTokenInfo, @RequestBody @Valid Entry data);
}
