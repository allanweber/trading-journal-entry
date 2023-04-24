package com.trading.journal.entry.entries.image.data;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class EntryImageResponse {

    private String id;

    private String image;

    private String imageName;
}
