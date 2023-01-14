package com.trading.journal.entry.entries.trade.aggregate;

import com.allanweber.jwttoken.helper.DateHelper;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.NumberFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class AggregatedItems {

    private String tradeId;

    private String symbol;

    private String order;

    @JsonFormat(pattern = DateHelper.DATE_FORMAT)
    private LocalDateTime date;

    @JsonFormat(pattern = DateHelper.DATE_FORMAT)
    private LocalDateTime exitDate;

    @NumberFormat(pattern = "#0.00")
    private BigDecimal result;
}
