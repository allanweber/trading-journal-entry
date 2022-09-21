package com.trading.journal.entry.journal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Document(collection = "journals")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Journal {

    @Id
    private String id;

    @NotBlank(message = "Journal name is required")
    private String name;

    @NotNull(message = "Start balance is required")
    private BigDecimal startBalance;
}
