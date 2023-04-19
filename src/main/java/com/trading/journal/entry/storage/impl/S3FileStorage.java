package com.trading.journal.entry.storage.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.trading.journal.entry.storage.FileStorage;
import com.trading.journal.entry.storage.data.FileResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

@Profile("!(local | test)")
@RequiredArgsConstructor
@Service
public class S3FileStorage implements FileStorage {

    private final AmazonS3 client;

    @Override
    public boolean folderExists(String folderName) {
        return client.doesBucketExistV2(folderName);
    }

    @Override
    public void createFolder(String folderName) {
        client.createBucket(folderName);
    }

    @Override
    public void uploadFile(String folder, String fileName, byte[] file) {
        InputStream input = new ByteArrayInputStream(file);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/jpg");
        metadata.setContentLength(file.length);
        PutObjectRequest request = new PutObjectRequest(folder, fileName, input, metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead);
        client.putObject(request);
    }

    @SneakyThrows
    @Override
    public Optional<FileResponse> getFile(String folder, String fileName) {
        Optional<FileResponse> file = empty();
        if (client.doesObjectExist(folder, fileName)) {
            try (S3Object item = client.getObject(new GetObjectRequest(folder, fileName))) {
                String name = fileName.substring(fileName.indexOf('/') + 1);
                file = of(new FileResponse(name, IOUtils.toByteArray(item.getObjectContent())));
            }
        }
        return file;
    }

    @Override
    public void deleteFile(String folder, String fileName) {
        client.deleteObject(folder, fileName);
    }
}
