package com.trading.journal.entry.api;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.entries.*;
import io.swagger.annotations.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Api(tags = "Entries Api")
@RequestMapping("/journals/{journal-id}/entries")
public interface EntryApi {

    @ApiOperation(notes = "Retrieve all entries from a journal", value = "Retrieve all entries from a journal sorted by date")
    @ApiResponses(@ApiResponse(code = 200, message = "Entries retrieved"))
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<List<Entry>> getAll(AccessTokenInfo accessTokenInfo, @PathVariable(name = "journal-id") String journalId,
                                       @ApiParam(name = "symbol", value = "MSFT, TSLA, etc") @RequestParam(value = "symbol", required = false) String symbol,
                                       @ApiParam(name = "type", value = "TRADE, TAXES, DEPOSIT, WITHDRAWAL") @RequestParam(value = "type", required = false) EntryType type,
                                       @ApiParam(name = "status", value = "OPEN or CLOSED") @RequestParam(value = "status", required = false) EntryStatus status,
                                       @ApiParam(name = "from", value = "2022-01-01 00:00:00") @RequestParam(value = "from", required = false) String from,
                                       @ApiParam(name = "direction", value = "LONG or SHORT") @RequestParam(value = "direction", required = false) EntryDirection direction,
                                       @ApiParam(name = "result", value = "WIN or LOSE") @RequestParam(value = "result", required = false) EntryResult result);

    @ApiOperation(notes = "Delete entry", value = "Delete entry")
    @ApiResponses(@ApiResponse(code = 200, message = "Entry deleted"))
    @DeleteMapping("/{entry-id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Void> delete(AccessTokenInfo accessTokenInfo, @PathVariable(name = "journal-id") String journalId, @PathVariable(name = "entry-id") String entryId);

    @ApiOperation(notes = "Get entry", value = "Get entry")
    @ApiResponses(@ApiResponse(code = 200, message = "Entry returned"))
    @GetMapping("/{entry-id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Entry> get(AccessTokenInfo accessTokenInfo, @PathVariable(name = "journal-id") String journalId, @PathVariable(name = "entry-id") String entryId);

    @ApiOperation(notes = "Upload Trade image", value = "Upload Trade image")
    @ApiResponses(@ApiResponse(code = 200, message = "Trade image uploaded"))
    @PostMapping("/{entry-id}/image")
    ResponseEntity<Void> uploadImage(AccessTokenInfo accessTokenInfo, @PathVariable(name = "journal-id") String journalId, @PathVariable(name = "entry-id") String entryId,
                                     @RequestParam("type") UploadType type,
                                     @RequestParam("file") MultipartFile file);

    @ApiOperation(notes = "Return Trade image", value = "Return Trade image")
    @ApiResponses(@ApiResponse(code = 200, message = "Trade image returned"))
    @GetMapping("/{entry-id}/image")
    ResponseEntity<EntryImageResponse> getImage(AccessTokenInfo accessTokenInfo, @PathVariable(name = "journal-id") String journalId, @PathVariable(name = "entry-id") String entryId,
                                                @RequestParam("type") UploadType type);
}
