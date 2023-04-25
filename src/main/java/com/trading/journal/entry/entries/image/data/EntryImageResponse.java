package com.trading.journal.entry.entries.image.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EntryImageResponse {

    private String id;

    private String image;

    private String imageName;
}
