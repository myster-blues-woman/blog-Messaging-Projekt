package com.example.service.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.example.service.media.MediaRepository;

import io.minio.*;
import io.minio.errors.MinioException;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class MinioService {

    @Inject
    MediaRepository mediaRepository;

    private MinioClient minioClient;

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
            throws InvalidKeyException, NoSuchAlgorithmException, IllegalArgumentException, IOException {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build());
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
}