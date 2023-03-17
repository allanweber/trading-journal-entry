package com.trading.journal.entry.entries;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Supplier;

import static com.trading.journal.entry.entries.EntryResult.LOSE;
import static com.trading.journal.entry.entries.EntryResult.WIN;
import static com.trading.journal.entry.entries.EntryStatus.CLOSED;
import static com.trading.journal.entry.entries.EntryStatus.OPEN;

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

    private static final String STRATEGIES = "strategies.$id";

    private AccessTokenInfo accessTokenInfo;

    private String journalId;

    private String symbol;

    private EntryType type;

    private EntryStatus status;

    private String from;

    private EntryDirection direction;

    private EntryResult result;

    private List<String> strategyIds;

    public String sortBy() {
        String sort = "date";
        if (CLOSED.equals(getStatus())) {
            sort = "exitDate";
        }
        return sort;
    }

    public Query buildQuery() {
        Query query = new Query();

        queryAppend(query, hasSymbol(), () -> Criteria.where(SYMBOL).is(symbol));

        queryAppend(query, hasType(), () -> Criteria.where(TYPE).is(type.name()));

        boolean addFromDate = hasFrom() && (hasNoStatus() || OPEN.equals(getStatus()));
        queryAppend(query, addFromDate, () -> Criteria.where(DATE).gte(LocalDateTime.parse(from, DATE_FORMATTER)));

        boolean addFromExitDate = hasFrom() && CLOSED.equals(getStatus());
        queryAppend(query, addFromExitDate, () -> Criteria.where(EXIT_DATE).gte(LocalDateTime.parse(from, DATE_FORMATTER)));

        queryAppend(query, hasDirection(), () -> Criteria.where(DIRECTION).is(direction.name()));

        queryAppend(query, OPEN.equals(getStatus()), () -> Criteria.where(NET_RESULT).exists(false));

        boolean closedWithNoResult = CLOSED.equals(getStatus()) && !hasResult();
        queryAppend(query, closedWithNoResult, () -> Criteria.where(NET_RESULT).exists(true));

        boolean closedWithWinResult = CLOSED.equals(getStatus()) && WIN.equals(getResult());
        queryAppend(query, closedWithWinResult, () -> Criteria.where(NET_RESULT).exists(true).gte(BigDecimal.ZERO));

        boolean closedWithLooseResult = CLOSED.equals(getStatus()) && LOSE.equals(getResult());
        queryAppend(query, closedWithLooseResult, () -> Criteria.where(NET_RESULT).exists(true).lt(BigDecimal.ZERO));

        boolean winWithNoStatus = WIN.equals(getResult()) && hasNoStatus();
        queryAppend(query, winWithNoStatus, () -> Criteria.where(NET_RESULT).gte(BigDecimal.ZERO));

        boolean looseWithNoStatus = LOSE.equals(getResult()) && hasNoStatus();
        queryAppend(query, looseWithNoStatus, () -> Criteria.where(NET_RESULT).lt(BigDecimal.ZERO));

        return query;
    }

    private void queryAppend(Query query, boolean predicate, Supplier<Criteria> criteria) {
        if (predicate) {
            query.addCriteria(criteria.get());
        }
    }

    private boolean hasSymbol() {
        return StringUtils.hasText(symbol);
    }

    private boolean hasType() {
        return type != null;
    }

    private boolean hasFrom() {
        return StringUtils.hasText(from);
    }

    private boolean hasNoStatus() {
        return status == null;
    }

    private boolean hasResult() {
        return result != null;
    }

    private boolean hasDirection() {
        return direction != null;
    }
}
