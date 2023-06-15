package com.trading.journal.entry.api;

import com.trading.journal.entry.entries.*;
import com.trading.journal.entry.entries.image.data.EntryImageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/journals/{journal-id}/entries")
public interface EntryApi {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<PageWrapper<Entry>> getAll(@PathVariable(name = "journal-id") String journalId,
                                              @SortDefault.SortDefaults({@SortDefault(sort = "date", direction = Sort.Direction.DESC)}) Pageable pageable,
                                              @RequestParam(value = "symbol", required = false) String symbol,
                                              @RequestParam(value = "type", required = false) EntryType type,
                                              @RequestParam(value = "status", required = false) EntryStatus status,
                                              @RequestParam(value = "from", required = false) String from,
                                              @RequestParam(value = "direction", required = false) EntryDirection direction,
                                              @RequestParam(value = "result", required = false) EntryResult result,
                                              @RequestParam(value = "strategies", required = false) List<String> strategies);

    @DeleteMapping("/{entry-id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Void> delete(@PathVariable(name = "entry-id") String entryId);

    @GetMapping("/{entry-id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Entry> get(@PathVariable(name = "entry-id") String entryId);

    @PostMapping("/{entry-id}/image")
    ResponseEntity<EntryImageResponse> uploadImage(@PathVariable(name = "entry-id") String entryId,
                                                   @RequestParam("file") MultipartFile file);

    @GetMapping("/{entry-id}/images")
    ResponseEntity<List<EntryImageResponse>> getImages(@PathVariable(name = "entry-id") String entryId);

    @DeleteMapping("/{entry-id}/image/{image-id}")
    ResponseEntity<Void> deleteImage(@PathVariable(name = "entry-id") String entryId,
                                     @PathVariable(name = "image-id") String imageId);
}
