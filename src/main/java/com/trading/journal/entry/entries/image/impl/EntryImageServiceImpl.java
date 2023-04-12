package com.trading.journal.entry.entries.image.impl;

import com.trading.journal.entry.ApplicationException;
import com.trading.journal.entry.entries.EntryRepository;
import com.trading.journal.entry.entries.image.EntryImageService;
import com.trading.journal.entry.entries.image.data.EntryImageResponse;
import com.trading.journal.entry.queries.TokenRequestScope;
import com.trading.journal.entry.storage.FileStorage;
import com.trading.journal.entry.storage.ImageCompression;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
@Service
public class EntryImageServiceImpl implements EntryImageService {

    private final FileStorage fileStorage;

    private final EntryRepository entryRepository;

    private final ImageCompression imageCompression;

    @SneakyThrows
    @Override
    public void uploadImage(String entryId, MultipartFile file) {
        List<String> entryImages = getEntryImages(entryId);
        String imageFileName = "image-%s.jpg".formatted(entryImages.size() + 1);
        entryImages.add(imageFileName);

        String folder = getFolder();
        if (!fileStorage.folderExists(folder)) {
            fileStorage.createFolder(folder);
        }
        String fileName = "%s/%s".formatted(entryId, imageFileName);
        byte[] compressedImage = imageCompression.compressImage(file.getBytes());
        fileStorage.uploadFile(folder, fileName, compressedImage);

        Query query = new Query(Criteria.where("_id").is(entryId));
        Update update = new Update().set("images", entryImages);
        entryRepository.update(query, update);
    }

    @Override
    public List<EntryImageResponse> returnImages(String entryId) {
        String folder = getFolder();
        return fileStorage.listFiles(folder, entryId)
                .stream()
                .map(fileName -> fileStorage.getFile(folder, fileName)
                        .map(file -> new EntryImageResponse(Base64.getEncoder().encodeToString(file.getFile()), file.getFileName()))
                        .orElse(null)
                )
                .collect(Collectors.toList());
    }

    @Override
    public void deleteImage(String entryId, String imageName) {
        List<String> entryImages = getEntryImages(entryId);
        entryImages.remove(imageName);
        String fileName = "%s/%s".formatted(entryId, imageName);
        fileStorage.deleteFile(getFolder(), fileName);

        Update update = new Update().set("images", entryImages);
        if (entryImages.isEmpty()) {
            update = new Update().unset("images");
        }
        Query query = new Query(Criteria.where("_id").is(entryId));
        entryRepository.update(query, update);
    }

    private String getFolder() {
        return TokenRequestScope.get().tenancyName();
    }

    private List<String> getEntryImages(String entryId) {
        return entryRepository.getById(entryId).map(entry -> ofNullable(entry.getImages()).map(ArrayList::new).orElse(new ArrayList<>())).orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Entry not found"));
    }
}
