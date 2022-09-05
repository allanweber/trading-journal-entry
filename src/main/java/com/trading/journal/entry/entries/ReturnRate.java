package com.trading.journal.entry.entries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class ReturnRate {

    private Integer profit;

    private Integer loss;
}
