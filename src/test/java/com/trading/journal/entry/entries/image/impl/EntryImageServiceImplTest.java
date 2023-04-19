package com.trading.journal.entry.entries.image.impl;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.EntryRepository;
import com.trading.journal.entry.entries.image.data.EntryImageResponse;
import com.trading.journal.entry.queries.TokenRequestScope;
import com.trading.journal.entry.storage.FileStorage;
import com.trading.journal.entry.storage.ImageCompression;
import com.trading.journal.entry.storage.data.FileResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class EntryImageServiceImplTest {

    @Mock
    FileStorage fileStorage;

    @Mock
    EntryRepository entryRepository;

    @Mock
    ImageCompression imageCompression;

    @InjectMocks
    EntryImageServiceImpl entryImage;

    @BeforeEach
    void setUp() {
        TokenRequestScope.set(new AccessTokenInfo("user", 1L, "Test-Tenancy", singletonList("ROLE_USER")));
    }

    @DisplayName("Upload one image")
    @Test
    void uploadImage() throws IOException {
        String entryId = UUID.randomUUID().toString();
        String folder = "Test-Tenancy";

        when(entryRepository.getById(entryId)).thenReturn(of(Entry.builder().id(entryId).build()));

        when(fileStorage.folderExists(folder)).thenReturn(true);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenReturn(new byte[]{0});

        when(imageCompression.compressImage(any())).thenReturn(new byte[]{0});
        doNothing().when(fileStorage).uploadFile(folder, "%s/image-1.jpg".formatted(entryId), new byte[]{0});

        entryImage.uploadImage(entryId, file);

        verify(entryRepository).update(new Query(Criteria.where("_id").is(entryId)), new Update().set("images", singletonList("image-1.jpg")));
        verify(fileStorage, never()).createFolder(anyString());
    }

    @DisplayName("Upload one image but need to create a nre folder first")
    @Test
    void uploadImageCreateFolder() throws IOException {
        String entryId = UUID.randomUUID().toString();
        String folder = "Test-Tenancy";

        when(entryRepository.getById(entryId)).thenReturn(of(Entry.builder().id(entryId).build()));

        when(fileStorage.folderExists(folder)).thenReturn(false);
        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenReturn(new byte[]{0});

        when(imageCompression.compressImage(any())).thenReturn(new byte[]{0});
        doNothing().when(fileStorage).uploadFile(folder, "%s/image-1.jpg".formatted(entryId), new byte[]{0});

        entryImage.uploadImage(entryId, file);

        verify(entryRepository).update(new Query(Criteria.where("_id").is(entryId)), new Update().set("images", singletonList("image-1.jpg")));
        verify(fileStorage).createFolder(anyString());
    }

    @DisplayName("Upload one image when there is already one image uploaded")
    @Test
    void uploadSecondImage() throws IOException {
        String entryId = UUID.randomUUID().toString();
        String folder = "Test-Tenancy";

        when(entryRepository.getById(entryId)).thenReturn(of(Entry.builder().id(entryId).images(singletonList("image-1.jpg")).build()));

        when(fileStorage.folderExists(folder)).thenReturn(true);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenReturn(new byte[]{0});

        when(imageCompression.compressImage(any())).thenReturn(new byte[]{0});
        doNothing().when(fileStorage).uploadFile(folder, "%s/image-2.jpg".formatted(entryId), new byte[]{0});

        entryImage.uploadImage(entryId, file);

        verify(entryRepository).update(new Query(Criteria.where("_id").is(entryId)), new Update().set("images", asList("image-1.jpg", "image-2.jpg")));
    }

    @DisplayName("Return one image")
    @Test
    void returnImages() {
        String entryId = UUID.randomUUID().toString();
        byte[] file = "some-file".getBytes();
        when(entryRepository.getById(entryId)).thenReturn(of(Entry.builder().id(entryId).images(singletonList("image-1.jpg")).build()));
        when(fileStorage.getFile("Test-Tenancy", "%s/image-1.jpg".formatted(entryId))).thenReturn(of(new FileResponse("image-1.jpg", file)));

        List<EntryImageResponse> images = entryImage.returnImages(entryId);
        assertThat(images).hasSize(1);
        assertThat(images).extracting(EntryImageResponse::getImageName).containsExactly("image-1.jpg");
        assertThat(images).extracting(EntryImageResponse::getImage).containsExactly(Base64.getEncoder().encodeToString(file));
    }

    @DisplayName("Return two images")
    @Test
    void returnMultipleImages() {
        String entryId = UUID.randomUUID().toString();
        byte[] file = "some-file".getBytes();
        byte[] file2 = "some-file-2".getBytes();
        when(entryRepository.getById(entryId)).thenReturn(of(Entry.builder().id(entryId).images(asList("image-1.jpg", "image-2")).build()));
        when(fileStorage.getFile("Test-Tenancy", "%s/image-1.jpg".formatted(entryId))).thenReturn(of(new FileResponse("image-1.jpg", file)));
        when(fileStorage.getFile("Test-Tenancy", "%s/image-2".formatted(entryId))).thenReturn(of(new FileResponse("image-2", file2)));

        List<EntryImageResponse> images = entryImage.returnImages(entryId);
        assertThat(images).hasSize(2);
        assertThat(images).extracting(EntryImageResponse::getImageName).containsExactly("image-1.jpg", "image-2");
        assertThat(images).extracting(EntryImageResponse::getImage).containsExactly(Base64.getEncoder().encodeToString(file), Base64.getEncoder().encodeToString(file2));
    }

    @DisplayName("Return one image but it is not found")
    @Test
    void returnImagesEmpty() {
        String entryId = UUID.randomUUID().toString();

        when(entryRepository.getById(entryId)).thenReturn(empty());

        List<EntryImageResponse> images = entryImage.returnImages(entryId);
        assertThat(images).isEmpty();
        verify(fileStorage, never()).getFile(anyString(), anyString());
    }

    @DisplayName("Delete a image")
    @Test
    void deleteImage() {
        String entryId = UUID.randomUUID().toString();

        when(entryRepository.getById(entryId)).thenReturn(of(Entry.builder().id(entryId).images(singletonList("image-1.jpg")).build()));
        doNothing().when(fileStorage).deleteFile("Test-Tenancy", "%s/image-1.jpg".formatted(entryId));

        entryImage.deleteImage(entryId, "image-1.jpg");

        verify(entryRepository).update(new Query(Criteria.where("_id").is(entryId)), new Update().unset("images"));
    }

    @DisplayName("Delete a image but there are more images for this entry")
    @Test
    void deleteOneOfTheImages() {
        String entryId = UUID.randomUUID().toString();

        when(entryRepository.getById(entryId)).thenReturn(of(Entry.builder().id(entryId).images(asList("image-1.jpg", "image-2")).build()));
        doNothing().when(fileStorage).deleteFile("Test-Tenancy", "%s/image-2".formatted(entryId));

        entryImage.deleteImage(entryId, "image-2");

        verify(entryRepository).update(new Query(Criteria.where("_id").is(entryId)), new Update().set("images", singletonList("image-1.jpg")));
    }
}