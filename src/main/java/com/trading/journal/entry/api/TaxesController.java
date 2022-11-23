package com.trading.journal.entry.api;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.taxes.Taxes;
import com.trading.journal.entry.entries.taxes.TaxesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

import static org.springframework.http.ResponseEntity.created;

@RequiredArgsConstructor
@RestController
public class TaxesController implements TaxesApi {

    private final TaxesService taxesService;

    @Override
    public ResponseEntity<Entry> create(AccessTokenInfo accessTokenInfo, String journalId, Taxes taxes) {
        Entry created = taxesService.create(accessTokenInfo, journalId, taxes);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.getId()).toUri();
        return created(uri).body(created);
    }
}
