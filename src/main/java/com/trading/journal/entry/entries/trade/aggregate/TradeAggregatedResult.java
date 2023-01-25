package com.trading.journal.entry.entries.trade.aggregate;

import com.allanweber.jwttoken.helper.DateHelper;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TradeAggregatedResult {

    @JsonFormat(pattern = DateHelper.DATE_FORMAT)
    private String from;

    @JsonFormat(pattern = DateHelper.DATE_FORMAT)
    private String until;

    private Long total;
}
