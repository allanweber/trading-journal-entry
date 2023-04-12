package com.trading.journal.entry.storage;

import com.trading.journal.entry.storage.data.FileResponse;

import java.util.List;
import java.util.Optional;

public interface FileStorage {

    boolean folderExists(String folderName);

    void createFolder(String folderName);

    void uploadFile(String folder, String fileName, byte[] file);

    Optional<FileResponse> getFile(String folder, String fileName);

    void deleteFile(String folder, String fileName);

    List<String> listFiles(String rootFolder, String targetFolder);
}
