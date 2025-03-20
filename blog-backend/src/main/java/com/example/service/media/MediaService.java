package com.example.service.media;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.example.model.media.Media;
import com.example.service.storage.MinioService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.SecurityContext;

@ApplicationScoped
public class MediaService {
    @Inject
    MinioService minioService2;

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
                minioService2.uploadImage(fileName, fileStream, fileSize, contentType);
            } catch (Exception e) {
                throw new IOException("Failed to upload file to storage service: " + e.getMessage(), e);
            }

            // Save metadata to database
            Media media = new Media();
            media.setFilename(fileName);
            media.setContentType(contentType);
            media.setCreatedAt(Instant.now());
            media.setCreatedBy(getCurrentUser());

            try {
                return mediaRepository.save(media);
            } catch (Exception e) {
                throw new RuntimeException("Failed to save media metadata to the database: " + e.getMessage(), e);
            }
        } catch (IllegalArgumentException e) {
            throw e; // Re-throw validation exceptions
        } catch (IOException e) {
            throw e; // Re-throw IO exceptions
        } catch (Exception e) {
            throw new RuntimeException("An unexpected error occurred during file upload: " + e.getMessage(), e);
        }
    }

    // public Optional<Media> getMediaById(Long id) {
    // return mediaRepository.findById(id);
    // }

    // public void deleteMedia(Long id) {
    // mediaRepository.findById(id).ifPresent(media -> {
    // // Delete from MinIO first
    // minioService2.deleteFile(media.getFilename());
    // // Then delete from database
    // mediaRepository.delete(media);
    // });
    // }

    private String getCurrentUser() {
        // Get the username of the current user
        if (securityContext.getUserPrincipal() != null) {
            return securityContext.getUserPrincipal().getName();
        }
        return "unknown"; // Fallback if user is not authenticated
    }

    private String determineContentType(String originalFilename) {
        try {
            // Extract file extension
            String fileExtension = getFileExtension(originalFilename);

            // Get content type based on file extension
            String contentType = Files.probeContentType(Paths.get(originalFilename));

            // If probeContentType is null, assign a default content type based on the
            // extension
            if (contentType == null) {
                contentType = switch (fileExtension) {
                    case "jpg", "jpeg" -> "image/jpeg";
                    case "png" -> "image/png";
                    case "gif" -> "image/gif";
                    case "pdf" -> "application/pdf";
                    case "txt" -> "text/plain";
                    default -> "application/octet-stream"; // Unknown file type
                };
            }

            return contentType;

        } catch (IOException e) {
            throw new RuntimeException("Error determining content type", e);
        }
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            return filename.substring(dotIndex + 1).toLowerCase();
        }
        return ""; // No extension
    }
}