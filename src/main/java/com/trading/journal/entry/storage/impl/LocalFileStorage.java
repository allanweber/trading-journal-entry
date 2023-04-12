package com.trading.journal.entry.storage.impl;

import com.trading.journal.entry.storage.FileStorage;
import com.trading.journal.entry.storage.data.FileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Profile("local | test")
@RequiredArgsConstructor
@Service
@SuppressWarnings("PMD")
public class LocalFileStorage implements FileStorage {

    private static final Map<String, Map<String, byte[]>> files = new ConcurrentHashMap<>();

    @Override
    public boolean folderExists(String folderName) {
        return files.containsKey(folderName);
    }

    @Override
    public void createFolder(String folderName) {
        files.put(folderName, new ConcurrentHashMap<>());
    }

    @Override
    public void uploadFile(String folder, String fileName, byte[] file) {
        if (files.containsKey(folder)) {
            files.get(folder).put(fileName, file);
        }
    }

    @Override
    public Optional<FileResponse> getFile(String folder, String fileName) {
        if (files.containsKey(folder)) {
            byte[] bytes = files.get(folder).get(fileName);
            String name = fileName.substring(fileName.indexOf('/') + 1);
            return Optional.of(new FileResponse(name, bytes));
        }
        return Optional.empty();
    }

    @Override
    public void deleteFile(String folder, String fileName) {
        if (files.containsKey(folder)) {
            files.get(folder).remove(fileName);
        }
    }

    @Override
    public List<String> listFiles(String rootFolder, String targetFolder) {
        if (files.containsKey(rootFolder)) {
            return files.get(rootFolder).keySet().stream().filter(key -> key.startsWith(targetFolder)).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
