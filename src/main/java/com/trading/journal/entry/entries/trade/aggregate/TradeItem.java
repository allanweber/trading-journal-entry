package com.trading.journal.entry.entries.trade.aggregate;

import com.allanweber.jwttoken.helper.DateHelper;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.NumberFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TradeItem {

    private String tradeId;

    @JsonFormat(pattern = DateHelper.DATE_FORMAT)
    private LocalDateTime date;

    private String symbol;

    @JsonFormat(pattern = DateHelper.DATE_FORMAT)
    private LocalDateTime exitDate;

    @NumberFormat(pattern = "#0.00")
    private BigDecimal netResult;
}
