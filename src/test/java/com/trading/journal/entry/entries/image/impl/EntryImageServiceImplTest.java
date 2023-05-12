package com.trading.journal.entry.entries.image.impl;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.EntryImage;
import com.trading.journal.entry.entries.EntryService;
import com.trading.journal.entry.entries.image.data.EntryImageResponse;
import com.trading.journal.entry.queries.TokenRequestScope;
import com.trading.journal.entry.storage.FileStorage;
import com.trading.journal.entry.storage.ImageCompression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class EntryImageServiceImplTest {

    @Mock
    FileStorage fileStorage;

    @Mock
    EntryService entryService;

    @Mock
    ImageCompression imageCompression;

    @InjectMocks
    EntryImageServiceImpl entryImage;

    @BeforeEach
    void setUp() {
        TokenRequestScope.set(new AccessTokenInfo("user", 1L, "Test-Tenancy", singletonList("ROLE_USER")));
    }

    @DisplayName("Upload one image when tenancy name has special chars, replace it")
    @Test
    void uploadImageSpecial() throws IOException {

        TokenRequestScope.set(new AccessTokenInfo("user", 1L, "Testt@#$%enan%^&()cY123?{}|", singletonList("ROLE_USER")));
        String entryId = UUID.randomUUID().toString();

        when(entryService.getById(entryId)).thenReturn(Entry.builder().id(entryId).build());

        when(fileStorage.folderExists("testtenancy123")).thenReturn(true);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenReturn(new byte[]{0});

        when(imageCompression.compressImage(any())).thenReturn(new byte[]{0});
        doNothing().when(fileStorage).uploadFile(eq("testtenancy123"), eq(entryId), anyString(), any());

        EntryImageResponse response = entryImage.uploadImage(entryId, file);
        assertThat(response.getImageName()).isEqualTo("image-1");

        verify(entryService).updateImages(eq(entryId), argThat(images -> images.size() == 1));
        verify(fileStorage, never()).createFolder(anyString());
    }

    @DisplayName("Upload one image")
    @Test
    void uploadImage() throws IOException {
        String rootFolder = "testtenancy";
        String entryId = UUID.randomUUID().toString();

        when(entryService.getById(entryId)).thenReturn(Entry.builder().id(entryId).build());

        when(fileStorage.folderExists(rootFolder)).thenReturn(true);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenReturn(new byte[]{0});

        when(imageCompression.compressImage(any())).thenReturn(new byte[]{0});
        doNothing().when(fileStorage).uploadFile(eq(rootFolder), eq(entryId), anyString(), any());

        EntryImageResponse response = entryImage.uploadImage(entryId, file);
        assertThat(response.getImageName()).isEqualTo("image-1");

        verify(entryService).updateImages(eq(entryId), argThat(images -> images.size() == 1));
        verify(fileStorage, never()).createFolder(anyString());
    }

    @DisplayName("Upload one image but need to create a nre folder first")
    @Test
    void uploadImageCreateFolder() throws IOException {
        String rootFolder = "Test-Tenancy";
        String entryId = UUID.randomUUID().toString();

        when(entryService.getById(entryId)).thenReturn(Entry.builder().id(entryId).build());

        when(fileStorage.folderExists(rootFolder)).thenReturn(false);
        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenReturn(new byte[]{0});

        when(imageCompression.compressImage(any())).thenReturn(new byte[]{0});
        doNothing().when(fileStorage).uploadFile(eq(rootFolder), eq(entryId), anyString(), any());

        EntryImageResponse response = entryImage.uploadImage(entryId, file);
        assertThat(response.getImageName()).isEqualTo("image-1");

        verify(entryService).updateImages(eq(entryId), argThat(images -> images.size() == 1));
        verify(fileStorage).createFolder(anyString());
    }

    @DisplayName("Upload one image when there is already one image uploaded")
    @Test
    void uploadSecondImage() throws IOException {
        String rootFolder = "test-tenancy";
        String entryId = UUID.randomUUID().toString();

        when(entryService.getById(entryId))
                .thenReturn(Entry.builder().id(entryId).images(singletonList(new EntryImage("1", "image-1", "1.jpg"))).build());

        when(fileStorage.folderExists(rootFolder)).thenReturn(true);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenReturn(new byte[]{0});

        when(imageCompression.compressImage(any())).thenReturn(new byte[]{0});
        doNothing().when(fileStorage).uploadFile(eq(rootFolder), eq(entryId), anyString(), any());

        EntryImageResponse response = entryImage.uploadImage(entryId, file);
        assertThat(response.getImageName()).isEqualTo("image-2");

        verify(entryService).updateImages(eq(entryId), argThat(images -> images.size() == 2));
    }

    @DisplayName("Return one image")
    @Test
    void returnImages() {
        String rootFolder = "testtenancy";
        String entryId = UUID.randomUUID().toString();

        when(entryService.getById(entryId))
                .thenReturn(Entry.builder().id(entryId).images(singletonList(new EntryImage("1", "image-1", "1.jpg"))).build());

        when(fileStorage.getFile(rootFolder, entryId, "1.jpg")).thenReturn(of("https://cdn.trading-jounal.com/%s/%s/1.jpg".formatted(rootFolder, entryId)));

        List<EntryImageResponse> images = entryImage.returnImages(entryId);
        assertThat(images).hasSize(1);
        assertThat(images).extracting(EntryImageResponse::getImageName).containsExactly("image-1");
    }

    @DisplayName("Return two images")
    @Test
    void returnMultipleImages() {
        String rootFolder = "testtenancy";
        String entryId = UUID.randomUUID().toString();

        when(entryService.getById(entryId))
                .thenReturn(Entry.builder().id(entryId).images(asList(new EntryImage("1", "image-1", "1.jpg"), new EntryImage("2", "image-2", "2.jpg"))).build());

        when(fileStorage.getFile(rootFolder, entryId, "1.jpg")).thenReturn(of("https://cdn.trading-jounal.com/%s/%s/1.jpg".formatted(rootFolder, entryId)));
        when(fileStorage.getFile(rootFolder, entryId, "2.jpg")).thenReturn(of("https://cdn.trading-jounal.com/%s/%s/2.jpg".formatted(rootFolder, entryId)));

        List<EntryImageResponse> images = entryImage.returnImages(entryId);
        assertThat(images).hasSize(2);
        assertThat(images).extracting(EntryImageResponse::getImageName).containsExactly("image-1", "image-2");
    }

    @DisplayName("Return one image but it is not found")
    @Test
    void returnImagesEmpty() {
        String entryId = UUID.randomUUID().toString();

        when(entryService.getById(entryId)).thenReturn(Entry.builder().build());

        List<EntryImageResponse> images = entryImage.returnImages(entryId);
        assertThat(images).isEmpty();
        verify(fileStorage, never()).getFile(anyString(), anyString(), anyString());
    }

    @DisplayName("Delete a image")
    @Test
    void deleteImage() {
        String rootFolder = "Test-Tenancy";
        String entryId = UUID.randomUUID().toString();

        when(entryService.getById(entryId)).thenReturn(Entry.builder().id(entryId).images(singletonList(new EntryImage("1", "image-1", "1.jpg"))).build());
        doNothing().when(fileStorage).deleteFile(rootFolder, entryId, "1.jpg");

        entryImage.deleteImage(entryId, "1");

        verify(entryService).updateImages(eq(entryId), argThat(images -> images.size() == 0));
    }

    @DisplayName("Delete a image but there are more images for this entry")
    @Test
    void deleteOneOfTheImages() {
        String rootFolder = "Test-Tenancy";
        String entryId = UUID.randomUUID().toString();

        when(entryService.getById(entryId))
                .thenReturn(Entry.builder().id(entryId).images(asList(new EntryImage("1", "image-1", "1.jpg"), new EntryImage("2", "image-2", "2.jpg"))).build());

        doNothing().when(fileStorage).deleteFile(rootFolder, entryId, "2.jpg");

        entryImage.deleteImage(entryId, "2");

        verify(entryService).updateImages(eq(entryId), argThat(images -> images.size() == 1));
    }
}