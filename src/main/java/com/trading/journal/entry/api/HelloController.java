package com.trading.journal.entry.api;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.hello.Hello;
import com.trading.journal.entry.hello.HelloService;
import com.trading.journal.entry.pageable.PageResponse;
import com.trading.journal.entry.pageable.PageableRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;

@RequiredArgsConstructor
@RestController
public class HelloController implements HelloApi {

    private final HelloService helloService;

    @Override
    public ResponseEntity<PageResponse<Hello>> getAll(AccessTokenInfo accessTokenInfo, Integer page, Integer size, String[] sort) {
        PageableRequest pageableRequest = PageableRequest.builder()
                .page(page)
                .size(size)
                .sort(sort)
                .build();
        PageResponse<Hello> pageResponse = helloService.getAll(accessTokenInfo.tenancyId(), pageableRequest);
        return ok(pageResponse);
    }

    @Override
    public ResponseEntity<Hello> create(AccessTokenInfo accessTokenInfo, Hello data) {
        Hello saved = helloService.create(accessTokenInfo.tenancyId(), data);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(saved.getId()).toUri();
        return created(uri).body(saved);
    }

    @Override
    public ResponseEntity<Hello> getById(AccessTokenInfo accessTokenInfo, String id) {
        return ok(helloService.find(accessTokenInfo.tenancyId(), id));
    }
}
