package com.trading.journal.entry.entries.image;

import com.trading.journal.entry.entries.image.data.EntryImageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EntryImageService {

    EntryImageResponse uploadImage(String entryId, MultipartFile file);

    List<EntryImageResponse> returnImages(String entryId);

    void deleteImage(String entryId, String imageId);
}
