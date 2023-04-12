package com.trading.journal.entry.storage.data;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class FileResponse {

    private String fileName;

    private  byte[] file;
}
