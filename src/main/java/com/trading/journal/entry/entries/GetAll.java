package com.trading.journal.entry.entries;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.queries.data.Filter;
import com.trading.journal.entry.queries.data.FilterOperation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.trading.journal.entry.entries.EntryResult.LOSE;
import static com.trading.journal.entry.entries.EntryResult.WIN;
import static com.trading.journal.entry.entries.EntryStatus.CLOSED;
import static com.trading.journal.entry.entries.EntryStatus.OPEN;

@AllArgsConstructor
@Getter
@Builder
public class GetAll {

    public static final String SYMBOL = "symbol";
    public static final String TYPE = "type";
    public static final String DATE = "date";
    public static final String EXIT_DATE = "exitDate";
    public static final String NET_RESULT = "netResult";
    public static final String DIRECTION = "direction";

    private AccessTokenInfo accessTokenInfo;

    private String journalId;

    private String symbol;

    private EntryType type;

    private EntryStatus status;

    private String from;

    private EntryDirection direction;

    private EntryResult result;

    private boolean hasSymbol() {
        return StringUtils.hasText(symbol);
    }

    private boolean hasType() {
        return type != null;
    }

    private boolean hasFrom() {
        return StringUtils.hasText(from);
    }

    private boolean hasStatus() {
        return status != null;
    }

    private boolean hasDirection() {
        return direction != null;
    }

    public List<Filter> filterAll() {
        List<Filter> filters = new ArrayList<>();
        filter(filters, hasSymbol(), SYMBOL, FilterOperation.EQUAL, this::getSymbol);
        filter(filters, hasType(), TYPE, FilterOperation.EQUAL, () -> getType().name());

        filter(filters, hasFrom() && (!hasStatus() || OPEN.equals(getStatus())), DATE, FilterOperation.GREATER_THAN_OR_EQUAL_TO, this::getFrom);
        filter(filters, hasFrom() && CLOSED.equals(getStatus()), EXIT_DATE, FilterOperation.GREATER_THAN_OR_EQUAL_TO, this::getFrom);

        filter(filters, OPEN.equals(getStatus()), NET_RESULT, FilterOperation.EXISTS, () -> "false");
        filter(filters, CLOSED.equals(getStatus()), NET_RESULT, FilterOperation.EXISTS, () -> "true");

        filter(filters, hasDirection(), DIRECTION, FilterOperation.EQUAL, () -> getDirection().name());

        filter(filters, WIN.equals(getResult()), NET_RESULT, FilterOperation.GREATER_THAN_OR_EQUAL_TO, () -> "0");
        filter(filters, LOSE.equals(getResult()), NET_RESULT, FilterOperation.LESS_THAN, () -> "0");

        return filters;
    }

    private void filter(List<Filter> filters, boolean predicate, String field, FilterOperation operation, Supplier<String> value) {
        if (predicate) {
            filters.add(Filter.builder().field(field).operation(operation).value(value.get()).build());
        }
    }

    public String sortBy() {
        String sort = "date";
        if (CLOSED.equals(getStatus())) {
            sort = "exitDate";
        }
        return sort;
    }
}
