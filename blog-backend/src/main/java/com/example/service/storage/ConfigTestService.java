package com.example.service.storage;

import java.net.URI;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@ApplicationScoped
public class ConfigTestService {

    private static final Logger LOG = Logger.getLogger(ConfigTestService.class);

    @ConfigProperty(name = "quarkus.s3.endpoint")
    String s3Endpoint;

    @ConfigProperty(name = "quarkus.s3.aws.credentials.static-provider.access-key-id")
    String accessKeyId;

    @ConfigProperty(name = "quarkus.s3.aws.credentials.static-provider.secret-access-key")
    String secretAccessKey;

    @ConfigProperty(name = "app.storage.bucket")
    String bucketName;

    public S3Client buildS3Client() {
        LOG.info("quarkus.s3.endpoint: " + s3Endpoint);
        LOG.info("quarkus.s3.aws.credentials.static-provider.access-key-id: " + accessKeyId);
        LOG.info("quarkus.s3.aws.credentials.static-provider.secret-access-key: " + secretAccessKey);
        LOG.info("app.storage.bucket: " + bucketName);

        S3Client s3Client = S3Client.builder()
                .endpointOverride(URI.create(s3Endpoint))
                .region(Region.of("eu-central-1"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                .build();
        return s3Client;

    }
}