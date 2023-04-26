package com.trading.journal.entry.storage.impl;

import com.trading.journal.entry.storage.FileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ConditionalOnProperty(prefix = "journal.entries.storage", name = "option", havingValue = "local", matchIfMissing = true)
@RequiredArgsConstructor
@Service
@SuppressWarnings("PMD")
public class LocalFileStorage implements FileStorage {

    private static final Map<String, Map<String, byte[]>> files = new ConcurrentHashMap<>();

    @Override
    public boolean folderExists(String rootFolder) {
        return files.containsKey(rootFolder);
    }

    @Override
    public void createFolder(String rootFolder) {
        files.put(rootFolder, new ConcurrentHashMap<>());
    }

    @Override
    public void uploadFile(String rootFolder, String folder, String storedName, byte[] file) {
        if (files.containsKey(rootFolder)) {
            String fileName = "%s/%s".formatted(folder, storedName);
            files.get(rootFolder).put(fileName, file);
        }
    }

    @Override
    public Optional<String> getFile(String rootFolder, String folder, String storedName) {
        if (files.containsKey(rootFolder)) {
            String fileName = "%s/%s".formatted(folder, storedName);
            byte[] bytes = files.get(rootFolder).get(fileName);
            return Optional.of(Base64.getEncoder().encodeToString(bytes));
        }
        return Optional.empty();
    }

    @Override
    public void deleteFile(String rootFolder, String folder, String storedName) {
        if (files.containsKey(rootFolder)) {
            String fileName = "%s/%s".formatted(folder, storedName);
            files.get(rootFolder).remove(fileName);
        }
    }
}
