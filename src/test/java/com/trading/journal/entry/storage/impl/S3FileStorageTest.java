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
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
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
        metadata.setContentType("application/x-java-serialized-object");
        metadata.setContentLength(file.length);

        when(client.putObject(folder, fileName, new ByteArrayInputStream(file), metadata)).thenReturn(new PutObjectResult());

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

    @DisplayName("List all files in a folder")
    @Test
    void listFiles() {
        String rootFolder = "rootFolder";
        String targetFolder = "file.txt";

        ListObjectsV2Result listObjectsV2Result = mock(ListObjectsV2Result.class);

        S3ObjectSummary summary1 = new S3ObjectSummary();
        summary1.setKey("file1");
        S3ObjectSummary summary2 = new S3ObjectSummary();
        summary2.setKey("file2");
        when(listObjectsV2Result.getObjectSummaries()).thenReturn(asList(summary1, summary2));

        when(client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(listObjectsV2Result);
        when(listObjectsV2Result.isTruncated()).thenReturn(false);

        List<String> files = fileStorage.listFiles(rootFolder, targetFolder);
        assertThat(files).hasSize(2);
        assertThat(files).containsExactly("file1", "file2");
    }

    @DisplayName("List all files in a folder in multiple requests")
    @Test
    void listFilesInStages() {
        String rootFolder = "rootFolder";
        String targetFolder = "file.txt";

        ListObjectsV2Result result1 = mock(ListObjectsV2Result.class);
        ListObjectsV2Result result2 = mock(ListObjectsV2Result.class);

        S3ObjectSummary summary1 = new S3ObjectSummary();
        summary1.setKey("file1");
        S3ObjectSummary summary2 = new S3ObjectSummary();
        summary2.setKey("file2");
        S3ObjectSummary summary3 = new S3ObjectSummary();
        summary1.setKey("file3");
        S3ObjectSummary summary4 = new S3ObjectSummary();
        summary2.setKey("file4");
        when(result1.getObjectSummaries()).thenReturn(asList(summary1, summary2));
        when(result2.getObjectSummaries()).thenReturn(asList(summary3, summary4));

        when(client.listObjectsV2(any(ListObjectsV2Request.class)))
                .thenReturn(result1)
                .thenReturn(result2);

        when(result1.isTruncated()).thenReturn(true);
        when(result2.isTruncated()).thenReturn(false);
        when(result1.getNextContinuationToken()).thenReturn("next_token");

        fileStorage.listFiles(rootFolder, targetFolder);

        verify(client, times(2)).listObjectsV2(any(ListObjectsV2Request.class));
    }
}
