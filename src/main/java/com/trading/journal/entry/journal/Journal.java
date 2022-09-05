package com.trading.journal.entry.journal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "journals")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Journal {

    @Id
    private String id;

    private String name;
}
