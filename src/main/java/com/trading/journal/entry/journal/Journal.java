package com.trading.journal.entry.journal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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

    @NotNull(message = "Balance is required")
    private Double balance;
}
