package com.trading.journal.entry.entries;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.util.StringUtils;

@AllArgsConstructor
@Getter
@Builder
public class GetAll {

    private AccessTokenInfo accessTokenInfo;

    private String journalId;

    private String symbol;

    private EntryType type;

    private EntryStatus status;

    private String from;

    private EntryDirection direction;

    private EntryResult result;

    public boolean hasSymbol() {
        return StringUtils.hasText(symbol);
    }

    public boolean hasType() {
        return type != null;
    }

    public boolean hasFrom() {
        return StringUtils.hasText(from);
    }

    public boolean hasStatus() {
        return status != null;
    }

    public boolean hasDirection() {
        return direction != null;
    }

    public boolean hasResult() {
        return result != null;
    }
}
