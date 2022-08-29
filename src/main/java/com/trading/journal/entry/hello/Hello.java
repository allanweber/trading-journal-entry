package com.trading.journal.entry.hello;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;

@Document(collection = "hello")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Hello {

    @Id
    private String id;

    @NotBlank(message = "Name is required")
    private String name;
}
