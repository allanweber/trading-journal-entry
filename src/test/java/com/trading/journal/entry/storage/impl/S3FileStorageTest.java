package com.trading.journal.entry.storage.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.trading.journal.entry.storage.data.FileResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayInputStream;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class S3FileStorageTest {

    @Mock
    AmazonS3 client;

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
        String folder = "folder";
        String fileName = "file.txt";
        byte[] file = "an file sample".getBytes();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/jpg");
        metadata.setContentLength(file.length);

        PutObjectRequest request = new PutObjectRequest(folder, fileName, new ByteArrayInputStream(file), metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead);

        when(client.putObject(request)).thenReturn(new PutObjectResult());

        fileStorage.uploadFile(folder, fileName, file);
    }

    @DisplayName("Download a file")
    @Test
    void getFile() {
        String folder = "folder";
        String fileName = "prefix/file.txt";

        when(client.doesObjectExist(folder, fileName)).thenReturn(true);

        S3Object s3Object = new S3Object();
        s3Object.setObjectContent(new ByteArrayInputStream("an file sample".getBytes()));
        when(client.getObject(new GetObjectRequest(folder, fileName))).thenReturn(s3Object);

        Optional<FileResponse> file = fileStorage.getFile(folder, fileName);

        assertThat(file).isPresent();
        assertThat(file.get().getFileName()).isEqualTo("file.txt");
        assertThat(file.get().getFile()).isEqualTo("an file sample".getBytes());
    }

    @DisplayName("Download a not existent file return empty")
    @Test
    void getFileNotFound() {
        String folder = "folder";
        String fileName = "file.txt";

        when(client.doesObjectExist(folder, fileName)).thenReturn(false);

        Optional<FileResponse> file = fileStorage.getFile(folder, fileName);

        assertThat(file).isNotPresent();
        verify(client, never()).getObject(any());
    }

    @DisplayName("Delete a file")
    @Test
    void deleteFile() {
        String folder = "folder";
        String fileName = "file.txt";

        doNothing().when(client).deleteObject(folder, fileName);

        fileStorage.deleteFile(folder, fileName);
    }
}
