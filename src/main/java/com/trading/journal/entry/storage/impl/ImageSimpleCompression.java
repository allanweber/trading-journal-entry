package com.trading.journal.entry.storage.impl;

import com.trading.journal.entry.storage.ImageCompression;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Iterator;

@Service
@NoArgsConstructor
public class ImageSimpleCompression implements ImageCompression {

    private static final float MAX_COMPRESSION_SIZE = 500; //500 kb

    @Override
    public byte[] compressImage(byte[] bytes) {
        return compressImage(bytes, 1);
    }

    @SneakyThrows
    private byte[] compressImage(byte[] bytes, float imageQuality) {
        float kbSize = bytes.length / 1024f;
        if (kbSize <= MAX_COMPRESSION_SIZE || imageQuality <= 0) {
            return bytes;
        } else {
            Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersByFormatName("jpg");

            if (!imageWriters.hasNext()) {
                throw new IllegalStateException("Writers Not Found!!");
            }
            ImageWriter imageWriter = imageWriters.next();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream);
            imageWriter.setOutput(imageOutputStream);

            ImageWriteParam imageWriteParam = imageWriter.getDefaultWriteParam();

            // Set the compress quality metrics
            imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            imageWriteParam.setCompressionQuality(imageQuality);

            // Create the buffered image
            InputStream inputStream = new ByteArrayInputStream(bytes);
            BufferedImage bufferedImage = ImageIO.read(inputStream);

            //Remove alpha channel to avoid (bogus input errors)
            bufferedImage = removeAlphaChannel(bufferedImage);

            // Compress and insert the image into the byte array.
            imageWriter.write(null, new IIOImage(bufferedImage, null, null), imageWriteParam);

            inputStream.close();
            outputStream.close();
            imageWriter.dispose();
            imageOutputStream.close();

            bytes = outputStream.toByteArray();

            return compressImage(bytes, imageQuality - .05f);
        }
    }

    private BufferedImage removeAlphaChannel(BufferedImage img) {
        if (!img.getColorModel().hasAlpha()) {
            return img;
        }
        BufferedImage target = createImage(img.getWidth(), img.getHeight());
        Graphics2D g = target.createGraphics();
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return target;
    }

    private BufferedImage createImage(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }


}