package com.example.service.storage;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

@Path("/s3")
public class S3Resource {

    @Inject
    S3Client s3Client;

    @GET
    @Path("/buckets")
    public ListBucketsResponse listBuckets() {
        return s3Client.listBuckets();
    }
}