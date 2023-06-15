package com.trading.journal.entry.configuration;

import jakarta.annotation.PostConstruct;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

@Configuration
@NoArgsConstructor
public class ApplicationConfiguration {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

}
