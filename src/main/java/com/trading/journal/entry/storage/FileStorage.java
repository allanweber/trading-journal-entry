package com.trading.journal.entry.storage;

import com.trading.journal.entry.storage.data.FileResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface FileStorage {

    void createFolder(String folderName);

    void uploadFile(String folder, String fileName, byte[] file);

    Optional<FileResponse> getFile(String folder, String fileName) throws IOException;

    void deleteFile(String folder, String fileName);

    List<String> listFiles(String rootFolder, String targetFolder);
}
