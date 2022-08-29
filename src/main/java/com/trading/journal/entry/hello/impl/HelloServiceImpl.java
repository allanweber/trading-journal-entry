package com.trading.journal.entry.hello.impl;

import com.trading.journal.entry.ApplicationException;
import com.trading.journal.entry.hello.Hello;
import com.trading.journal.entry.hello.HelloRepository;
import com.trading.journal.entry.hello.HelloService;
import com.trading.journal.entry.pageable.PageResponse;
import com.trading.journal.entry.pageable.PageableRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class HelloServiceImpl implements HelloService {

    private final HelloRepository repository;

    @Override
    public PageResponse<Hello> getAll(Long tenancyId, PageableRequest pageRequest) {
        Page<Hello> page = repository.findAll(pageRequest.pageable());
        return new PageResponse<>(page.getTotalElements(), page.getTotalPages(), page.getNumber(), page.toList());
    }

    @Override
    public Hello create(Long tenancyId, Hello hello) {
        return repository.save(hello);
    }

    @Override
    public Hello find(Long tenancyId, String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "not found"));
    }
}
