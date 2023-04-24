package com.trading.journal.entry.storage;

import java.util.Optional;

public interface FileStorage {

    boolean folderExists(String rootFolder);

    void createFolder(String rootFolder);

    void uploadFile(String rootFolder, String folder, String storedName, byte[] file);

    Optional<String> getFile(String rootFolder, String folder, String storedName);

    void deleteFile(String rootFolder, String folder, String storedName);
}
