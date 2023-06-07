package com.trading.journal.entry.storage.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.trading.journal.entry.configuration.properties.StorageProperties;
import com.trading.journal.entry.storage.FileStorage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;

@ConditionalOnProperty(prefix = "journal.entries.storage", name = "option", havingValue = "s3")
@RequiredArgsConstructor
@Service
@Slf4j
public class S3FileStorage implements FileStorage {

    private final AmazonS3 client;

    private final StorageProperties properties;

    @Override
    public boolean folderExists(String rootFolder) {
        return client.doesBucketExistV2(rootFolder);
    }

    @Override
    public void createFolder(String rootFolder) {
        client.createBucket(rootFolder);
    }

    @Override
    public void uploadFile(String rootFolder, String folder, String storedName, byte[] file) {
        InputStream input = new ByteArrayInputStream(file);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/jpg");
        metadata.setContentLength(file.length);
        String fileName = "%s/%s".formatted(folder, storedName);
        PutObjectRequest request = new PutObjectRequest(rootFolder, fileName, input, metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead);
        client.putObject(request);
    }

    @SneakyThrows
    @Override
    public Optional<String> getFile(String rootFolder, String folder, String storedName) {
        return Optional.of(UriComponentsBuilder.fromUriString(properties.getCdn())
                .pathSegment(rootFolder)
                .pathSegment(folder)
                .pathSegment(storedName)
                .build()
                .toString());
    }

    @Override
    public void deleteFile(String rootFolder, String folder, String storedName) {
        String fileName = "%s/%s".formatted(folder, storedName);
        client.deleteObject(rootFolder, fileName);
    }
}
