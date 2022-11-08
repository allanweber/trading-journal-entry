package com.trading.journal.entry.api;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.EntryImageResponse;
import com.trading.journal.entry.entries.UploadType;
import com.trading.journal.entry.queries.data.PageResponse;
import io.swagger.annotations.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "Entries Api")
@RequestMapping("/entries")
public interface EntryApi {

    String DESCRIPTION = "Available filters:eq (Equal), gt(Greater than), gte(Greater than or equal), lt(Less than), lte(Less than or equal). eg. 'FieldName.Operation', 'Value'";

    @ApiOperation(notes = "Query entries from a journal", value = "Query entries from a journal")
    @ApiResponses(@ApiResponse(code = 200, message = "Entries retrieved"))
    @GetMapping("/{journal-id}/query")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<PageResponse<Entry>> query(
            AccessTokenInfo accessTokenInfo,
            @PathVariable(name = "journal-id") String journalId,
            @RequestParam(value = "page", defaultValue = "0", required = false) Integer page,
            @RequestParam(value = "size", defaultValue = "100", required = false) Integer size,
            @ApiParam(name = "sort", value = "eg. \"id,asc\", \"name,desc\"") @RequestParam(value = "sort", required = false) String[] sort,
            @ApiParam(name = "filter", value = DESCRIPTION) @RequestParam(value = "filter", required = false) String[] filter);

    @ApiOperation(notes = "Retrieve all entries from a journal", value = "Retrieve all entries from a journal sorted by date")
    @ApiResponses(@ApiResponse(code = 200, message = "Entries retrieved"))
    @GetMapping("/{journal-id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<List<Entry>> getAll(AccessTokenInfo accessTokenInfo, @PathVariable(name = "journal-id") String journalId);

    @ApiOperation(notes = "Save entry", value = "Save entry, some fields are calculated")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Entry created"),
            @ApiResponse(code = 200, message = "Entry updated")
    })
    @PostMapping("/{journal-id}")
    ResponseEntity<Entry> save(AccessTokenInfo accessTokenInfo, @PathVariable(name = "journal-id") String journalId, @RequestBody @Valid Entry data);

    @ApiOperation(notes = "Delete entry", value = "Delete entry")
    @ApiResponses(@ApiResponse(code = 200, message = "Entry deleted"))
    @DeleteMapping("/{journal-id}/{entry-id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Void> delete(AccessTokenInfo accessTokenInfo, @PathVariable(name = "journal-id") String journalId,  @PathVariable(name = "entry-id") String entryId);

    @ApiOperation(notes = "Upload Trade image", value = "Upload Trade image")
    @ApiResponses(@ApiResponse(code = 200, message = "Trade image uploaded"))
    @PostMapping("/{journal-id}/{entry-id}/image")
    ResponseEntity<Void> uploadImage(AccessTokenInfo accessTokenInfo, @PathVariable(name = "journal-id") String journalId,  @PathVariable(name = "entry-id") String entryId,
                                                @RequestParam("type") UploadType type,
                                                @RequestParam("file") MultipartFile file);

    @ApiOperation(notes = "Return Trade image", value = "Return Trade image")
    @ApiResponses(@ApiResponse(code = 200, message = "Trade image returned"))
    @GetMapping("/{journal-id}/{entry-id}/image")
    ResponseEntity<EntryImageResponse> getImage(AccessTokenInfo accessTokenInfo, @PathVariable(name = "journal-id") String journalId, @PathVariable(name = "entry-id") String entryId,
                                                @RequestParam("type") UploadType type);
}
