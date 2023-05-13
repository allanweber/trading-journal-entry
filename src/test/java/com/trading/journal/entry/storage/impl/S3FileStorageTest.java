package com.trading.journal.entry.storage.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.trading.journal.entry.configuration.properties.StorageProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayInputStream;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class S3FileStorageTest {

    @Mock
    AmazonS3 client;

    @Mock
    StorageProperties properties;

    @InjectMocks
    S3FileStorage fileStorage;

    @DisplayName("Bucket exists")
    @Test
    void bucketExists() {
        String folder = "folder";
        when(client.doesBucketExistV2(folder)).thenReturn(true);

        boolean exists = fileStorage.folderExists(folder);
        assertThat(exists).isTrue();
    }

    @DisplayName("Bucket does not exists")
    @Test
    void bucketNotExists() {
        String folder = "folder";
        when(client.doesBucketExistV2(folder)).thenReturn(false);

        boolean exists = fileStorage.folderExists(folder);
        assertThat(exists).isFalse();
    }

    @DisplayName("Create a new bucket")
    @Test
    void createFolder() {
        String folder = "folder";
        when(client.createBucket(folder)).thenReturn(new Bucket());

        fileStorage.createFolder(folder);
    }

    @DisplayName("Upload a file")
    @Test
    void uploadFile() {
        String rootFolder = "rootFolder";
        String folder = UUID.randomUUID().toString();
        String storedName = "%s.jpg".formatted(UUID.randomUUID().toString());
        byte[] file = "an file sample".getBytes();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/jpg");
        metadata.setContentLength(file.length);

        String fileName = "%s/%s".formatted(folder, storedName);
        PutObjectRequest request = new PutObjectRequest(rootFolder, fileName, new ByteArrayInputStream(file), metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead);

        when(client.putObject(request)).thenReturn(new PutObjectResult());

        fileStorage.uploadFile(rootFolder, folder, storedName, file);
    }

    @DisplayName("Download a file")
    @Test
    void getFile() {
        String rootFolder = "rootFolder";
        String folder = UUID.randomUUID().toString();
        String storedName = "%s.jpg".formatted(UUID.randomUUID().toString());

        when(properties.getCdn()).thenReturn("https://cdn.trading-jounal.com");

        Optional<String> file = fileStorage.getFile(rootFolder, folder, storedName);

        assertThat(file).isPresent();
        assertThat(file.get()).isEqualTo("https://cdn.trading-jounal.com/%s/%s/%s".formatted(rootFolder, folder, storedName));
    }

    @DisplayName("Delete a file")
    @Test
    void deleteFile() {
        String rootFolder = "rootFolder";
        String folder = UUID.randomUUID().toString();
        String storedName = "%s.jpg".formatted(UUID.randomUUID().toString());

        String fileName = "%s/%s".formatted(folder, storedName);
        doNothing().when(client).deleteObject(rootFolder, fileName);

        fileStorage.deleteFile(rootFolder, folder, storedName);
    }
}
