package com.example.service.media;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.example.dto.MediaFileDTO;
import com.example.model.media.Media;
import com.example.service.storage.MinioService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.SecurityContext;

@ApplicationScoped
public class MediaService {
    @Inject
    MinioService minioService;

    @Inject
    MediaRepository mediaRepository;

    @Inject
    ImageProcessor imageProcessor;

    @Inject
    SecurityContext securityContext;

    @ConfigProperty(name = "app.storage.allowed-types")
    List<String> allowedTypes;

    @Transactional
    public Media uploadImage(String fileName, InputStream fileStream, long fileSize, String contentType)
            throws IOException {
        try {
            // Validate content type
            if (!allowedTypes.contains(contentType)) {
                throw new IllegalArgumentException("Unsupported file type: " + contentType);
            }

            // Upload to MinIO
            try {
                minioService.uploadImage(fileName, fileStream, fileSize, contentType);
            } catch (Exception e) {
                throw new IOException("Failed to upload file to storage service: " + e.getMessage(), e);
            }

            // Save metadata to database
            Media media = new Media();
            media.setOriginalFilename(fileName);
            media.setFileName(fileName);
            media.setContentType(contentType);
            media.setSize(fileSize);
            media.setCreatedAt(Instant.now());
            media.setCreatedBy(getCurrentUser());

            String storagePath = "blog-media" + fileName;
            media.setStoragePath(storagePath);

            try {
                return mediaRepository.save(media);
            } catch (Exception e) {
                throw new RuntimeException("Failed to save media metadata to the database: " + e.getMessage(), e);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("An unexpected error occurred during file upload: " + e.getMessage(), e);
        }
    }

    private String getCurrentUser() {
        if (securityContext.getUserPrincipal() != null) {
            return securityContext.getUserPrincipal().getName();
        }
        return "unknown";
    }

    public List<MediaFileDTO> listAllMedia() {
        return minioService.listAllFilesWithContent();
    }

    public InputStream downloadFile(String fileName)
            throws InvalidKeyException, NoSuchAlgorithmException, IllegalArgumentException, IOException {
        return minioService.downloadImage(fileName);
    }
}