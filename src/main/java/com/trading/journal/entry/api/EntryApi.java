package com.trading.journal.entry.api;

import com.trading.journal.entry.entries.*;
import com.trading.journal.entry.entries.image.data.EntryImageResponse;
import io.swagger.annotations.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
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
    ResponseEntity<PageWrapper<Entry>> getAll(@PathVariable(name = "journal-id") String journalId,
                                              @SortDefault.SortDefaults({@SortDefault(sort = "date", direction = Sort.Direction.DESC)}) Pageable pageable,
                                              @ApiParam(name = "symbol", value = "MSFT, TSLA, etc") @RequestParam(value = "symbol", required = false) String symbol,
                                              @ApiParam(name = "type", value = "TRADE, TAXES, DEPOSIT, WITHDRAWAL") @RequestParam(value = "type", required = false) EntryType type,
                                              @ApiParam(name = "status", value = "OPEN or CLOSED") @RequestParam(value = "status", required = false) EntryStatus status,
                                              @ApiParam(name = "from", value = "2022-01-01 00:00:00") @RequestParam(value = "from", required = false) String from,
                                              @ApiParam(name = "direction", value = "LONG or SHORT") @RequestParam(value = "direction", required = false) EntryDirection direction,
                                              @ApiParam(name = "result", value = "WIN or LOSE") @RequestParam(value = "result", required = false) EntryResult result,
                                              @ApiParam(name = "strategies", value = "List of Strategies Id") @RequestParam(value = "strategies", required = false) List<String> strategies);

    @ApiOperation(notes = "Delete entry", value = "Delete entry")
    @ApiResponses(@ApiResponse(code = 200, message = "Entry deleted"))
    @DeleteMapping("/{entry-id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Void> delete(@PathVariable(name = "entry-id") String entryId);

    @ApiOperation(notes = "Get entry", value = "Get entry")
    @ApiResponses(@ApiResponse(code = 200, message = "Entry returned"))
    @GetMapping("/{entry-id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Entry> get(@PathVariable(name = "entry-id") String entryId);

    @ApiOperation(notes = "Upload Trade image", value = "Upload Trade image")
    @ApiResponses(@ApiResponse(code = 200, message = "Trade image uploaded"))
    @PostMapping("/{entry-id}/image")
    ResponseEntity<EntryImageResponse> uploadImage(@PathVariable(name = "entry-id") String entryId,
                                     @RequestParam("file") MultipartFile file);

    @ApiOperation(notes = "Return Trade image", value = "Return Trade image")
    @ApiResponses(@ApiResponse(code = 200, message = "Trade image returned"))
    @GetMapping("/{entry-id}/images")
    ResponseEntity<List<EntryImageResponse>> getImages(@PathVariable(name = "entry-id") String entryId);

    @ApiOperation(notes = "Return Trade image", value = "Return Trade image")
    @ApiResponses(@ApiResponse(code = 200, message = "Trade image returned"))
    @DeleteMapping("/{entry-id}/image/{image-id}")
    ResponseEntity<Void> deleteImage(@PathVariable(name = "entry-id") String entryId,
                                     @PathVariable(name = "image-id") String imageId);
}
