package com.trading.journal.entry.entries;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@EqualsAndHashCode
@ToString
public class EntryImage {

    private String imageId;

    private String name;

    private String storedName;
}
