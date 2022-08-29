package com.trading.journal.entry.hello;

import com.trading.journal.entry.pageable.PageResponse;
import com.trading.journal.entry.pageable.PageableRequest;

public interface HelloService {

    PageResponse<Hello> getAll(Long tenancyId, PageableRequest pageableRequest);

    Hello create(Long tenancyId, Hello hello);

    Hello find(Long tenancyId, String id);
}
