package com.trading.journal.entry.entries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static com.trading.journal.entry.entries.EntryResult.LOSE;
import static com.trading.journal.entry.entries.EntryResult.WIN;
import static com.trading.journal.entry.entries.EntryStatus.CLOSED;
import static com.trading.journal.entry.entries.EntryStatus.OPEN;
import static java.util.Optional.ofNullable;

@AllArgsConstructor
@Getter
@Builder
public class EntriesQuery {
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static final String SYMBOL = "symbol";
    public static final String TYPE = "type";
    public static final String DATE = "date";
    public static final String EXIT_DATE = "exitDate";
    public static final String NET_RESULT = "netResult";
    public static final String DIRECTION = "direction";

    private static final String STRATEGIES = "strategyIds";
    public static final String JOURNAL_ID = "journalId";

    @NotEmpty(message = "Journal Id is required")
    private String journalId;

    private String symbol;

    private EntryType type;

    private EntryStatus status;

    private String from;

    private EntryDirection direction;

    private EntryResult result;

    private List<String> strategyIds;

    private Pageable pageable;

    public Query buildQuery() {
        Query query = new Query(Criteria.where(JOURNAL_ID).is(journalId));

        queryAppend(query, StringUtils.hasText(symbol), () -> Criteria.where(SYMBOL).is(symbol));

        queryAppend(query, Objects.nonNull(type), () -> Criteria.where(TYPE).is(type.name()));

        boolean addFromDate = StringUtils.hasText(from) && (Objects.isNull(status) || OPEN.equals(getStatus()));
        queryAppend(query, addFromDate, () -> Criteria.where(DATE).gte(LocalDateTime.parse(from, DATE_FORMATTER)));

        boolean addFromExitDate = StringUtils.hasText(from) && CLOSED.equals(getStatus());
        queryAppend(query, addFromExitDate, () -> Criteria.where(EXIT_DATE).gte(LocalDateTime.parse(from, DATE_FORMATTER)));

        queryAppend(query, Objects.nonNull(direction), () -> Criteria.where(DIRECTION).is(direction.name()));

        queryAppend(query, OPEN.equals(getStatus()), () -> Criteria.where(NET_RESULT).exists(false));

        boolean closedWithNoResult = CLOSED.equals(getStatus()) && Objects.isNull(result);
        queryAppend(query, closedWithNoResult, () -> Criteria.where(NET_RESULT).exists(true));

        boolean closedWithWinResult = CLOSED.equals(getStatus()) && WIN.equals(getResult());
        queryAppend(query, closedWithWinResult, () -> Criteria.where(NET_RESULT).exists(true).gte(BigDecimal.ZERO));

        boolean closedWithLooseResult = CLOSED.equals(getStatus()) && LOSE.equals(getResult());
        queryAppend(query, closedWithLooseResult, () -> Criteria.where(NET_RESULT).exists(true).lt(BigDecimal.ZERO));

        boolean winWithNoStatus = WIN.equals(getResult()) && Objects.isNull(status);
        queryAppend(query, winWithNoStatus, () -> Criteria.where(NET_RESULT).gte(BigDecimal.ZERO));

        boolean looseWithNoStatus = LOSE.equals(getResult()) && Objects.isNull(status);
        queryAppend(query, looseWithNoStatus, () -> Criteria.where(NET_RESULT).lt(BigDecimal.ZERO));

        Boolean hasStrategies = ofNullable(strategyIds).map(list -> !list.isEmpty()).orElse(false);
        queryAppend(query, hasStrategies, () -> Criteria.where(STRATEGIES).in(strategyIds));

        return query;
    }

    private void queryAppend(Query query, boolean predicate, Supplier<Criteria> criteria) {
        if (predicate) {
            query.addCriteria(criteria.get());
        }
    }
}
