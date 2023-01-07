package com.trading.journal.entry.entries.trade;

import com.allanweber.jwttoken.helper.DateHelper;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.format.annotation.NumberFormat;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CloseTrade {

    @NumberFormat(pattern = "#0.00")
    @NotNull(message = "Exit price is required")
    private BigDecimal exitPrice;

    @JsonFormat(pattern = DateHelper.DATE_FORMAT)
    @NotNull(message = "Exit date is required")
    private LocalDateTime exitDate;
}
