package com.example.service.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.example.dto.MediaFileDTO;
import com.example.service.media.MediaRepository;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class MinioService {

    @Inject
    MediaRepository mediaRepository;

    private MinioClient minioClient;
    private Cache<String, byte[]> mediaCache;

    @ConfigProperty(name = "quarkus.minio.url")
    String minioUrl;

    @ConfigProperty(name = "quarkus.minio.access-key")
    String accessKey;

    @ConfigProperty(name = "quarkus.minio.secret-key")
    String secretKey;

    @ConfigProperty(name = "app.storage.bucket")
    String bucketName;

    @PostConstruct
    public void init() {
        mediaCache = Caffeine.newBuilder()
                .maximumSize(100) // Max 100 items in cache
                .expireAfterWrite(10, TimeUnit.MINUTES) // Cache expires after 10 minutes
                .build();

        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
        this.minioClient = MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(accessKey, secretKey)
                .region("us-east-1")
                .build();

        initializeBucket();
    }

    @Transactional
    public void uploadImage(String fileName, InputStream fileStream, long fileSize, String contentType) {
        try {
            // Read all bytes from input stream
            byte[] fileBytes = fileStream.readAllBytes();

            // Upload to MinIO using the actual file content
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(new ByteArrayInputStream(fileBytes), fileBytes.length, -1)
                            .contentType(contentType)
                            .build());

        } catch (MinioException e) {
            throw new RuntimeException("Error uploading file to MinIO: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error processing the uploaded file: " + e.getMessage(), e);
        }
    }

    public InputStream downloadImage(String fileName)
            throws IOException, InvalidKeyException, NoSuchAlgorithmException, IllegalArgumentException {
        try {
            // Check cache first
            byte[] cachedContent = mediaCache.getIfPresent(fileName);
            if (cachedContent != null) {
                return new ByteArrayInputStream(cachedContent);
            }

            // Fetch from MinIO if not in cache
            InputStream contentStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build());
            byte[] contentBytes = contentStream.readAllBytes();

            // Store in cache
            mediaCache.put(fileName, contentBytes);

            return new ByteArrayInputStream(contentBytes);
        } catch (MinioException e) {
            throw new RuntimeException("Error retrieving file from MinIO: " + e.getMessage(), e);
        }
    }

    public void deleteFile(String fileName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Error deleting file from MinIO: " + e.getMessage(), e);
        }
    }

    public void initializeBucket() {
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error initializing MinIO bucket: " + e.getMessage(), e);
        }
    }

    public List<MediaFileDTO> listAllFilesWithContent() {
        List<MediaFileDTO> files = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(bucketName).recursive(true).build());

            for (Result<Item> result : results) {
                Item item = result.get();

                InputStream contentStream = minioClient.getObject(
                        GetObjectArgs.builder().bucket(bucketName).object(item.objectName()).build());
                byte[] contentBytes = contentStream.readAllBytes();
                String base64Content = Base64.getEncoder().encodeToString(contentBytes);

                files.add(new MediaFileDTO(
                        item.objectName(),
                        minioUrl + "/" + bucketName + "/" + item.objectName(),
                        item.size(),
                        item.lastModified() != null ? item.lastModified().toString() : null,
                        base64Content));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error listing files from MinIO: " + e.getMessage(), e);
        }
        return files;
    }

}