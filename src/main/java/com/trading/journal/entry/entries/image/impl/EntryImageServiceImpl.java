package com.trading.journal.entry.entries.image.impl;

import com.trading.journal.entry.entries.Entry;
import com.trading.journal.entry.entries.EntryImage;
import com.trading.journal.entry.entries.EntryService;
import com.trading.journal.entry.entries.image.EntryImageService;
import com.trading.journal.entry.entries.image.data.EntryImageResponse;
import com.trading.journal.entry.queries.TokenRequestScope;
import com.trading.journal.entry.storage.FileStorage;
import com.trading.journal.entry.storage.ImageCompression;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
@Service
public class EntryImageServiceImpl implements EntryImageService {

    private final FileStorage fileStorage;

    private final EntryService entryService;

    private final ImageCompression imageCompression;

    @SneakyThrows
    @Override
    public EntryImageResponse uploadImage(String entryId, MultipartFile file) {
        List<EntryImage> entryImages = getEntryImages(entryId);
        String imageName = "image-%s".formatted(entryImages.size() + 1);
        String imageId = UUID.randomUUID().toString();
        String storedName = "%s.jpg".formatted(imageId);
        entryImages.add(EntryImage.builder().imageId(imageId).name(imageName).storedName(storedName).build());

        String folder = getFolder();
        if (!fileStorage.folderExists(folder)) {
            fileStorage.createFolder(folder);
        }

        byte[] compressedImage = imageCompression.compressImage(file.getBytes());
        fileStorage.uploadFile(folder, entryId, storedName, compressedImage);

        entryService.updateImages(entryId, entryImages);
        return EntryImageResponse.builder().id(imageId).imageName(imageName).build();
    }

    @Override
    public List<EntryImageResponse> returnImages(String entryId) {
        String folder = getFolder();
        return ofNullable(entryService.getById(entryId).getImages()).orElse(emptyList())
                .stream()
                .map(image -> fileStorage.getFile(folder, entryId, image.getStoredName())
                        .map(file -> new EntryImageResponse(image.getImageId(), file, image.getName()))
                        .orElse(null)
                )
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteImage(String entryId, String imageId) {
        List<EntryImage> entryImages = getEntryImages(entryId);
        List<EntryImage> updatedImages = entryImages.stream().filter(image -> !imageId.equals(image.getImageId())).toList();
        fileStorage.deleteFile(getFolder(), entryId, "%s.jpg".formatted(imageId));
        entryService.updateImages(entryId, updatedImages);
    }

    private String getFolder() {
        return TokenRequestScope.get().tenancyName().toLowerCase(Locale.getDefault()).replaceAll("[^A-Za-z0-9]", "");
    }

    private List<EntryImage> getEntryImages(String entryId) {
        return ofNullable(entryService.getById(entryId))
                .map(Entry::getImages)
                .map(ArrayList::new)
                .orElse(new ArrayList<>());
    }
}
