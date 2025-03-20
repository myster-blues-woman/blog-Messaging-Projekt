package com.example.service.media;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ImageProcessor {
    public InputStream process(InputStream input, String contentType) {
        try {
            // Lade das Bild
            BufferedImage original = ImageIO.read(input);

            // Skaliere das Bild auf eine maximale Größe
            BufferedImage resized = resizeImage(original, 1200, 1200);

            // Qualitätseinstellungen für die Komprimierung
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            // Schreibe das komprimierte Bild
            if ("image/jpeg".equals(contentType)) {
                ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
                ImageWriteParam param = writer.getDefaultWriteParam();
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(0.85f);

                writer.setOutput(ImageIO.createImageOutputStream(output));
                writer.write(null, new IIOImage(resized, null, null), param);
                writer.dispose();
            } else if ("image/png".equals(contentType)) {
                ImageIO.write(resized, "png", output);
            } else {
                ImageIO.write(resized, contentType.substring(contentType.indexOf('/') + 1), output);
            }

            return new ByteArrayInputStream(output.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Fehler bei der Bildverarbeitung", e);
        }
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int maxWidth, int maxHeight) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // Berechne neue Dimensionen unter Beibehaltung des Seitenverhältnisses
        int newWidth = originalWidth;
        int newHeight = originalHeight;

        if (originalWidth > maxWidth) {
            newWidth = maxWidth;
            newHeight = (int) (originalHeight * ((double) maxWidth / originalWidth));
        }

        if (newHeight > maxHeight) {
            newHeight = maxHeight;
            newWidth = (int) (newWidth * ((double) maxHeight / newHeight));
        }

        // Erstelle das skalierte Bild
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, originalImage.getType());
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g.dispose();

        return resizedImage;
    }
}