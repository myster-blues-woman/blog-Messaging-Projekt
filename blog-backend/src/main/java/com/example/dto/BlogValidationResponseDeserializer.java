package com.example.dto;

import io.quarkus.kafka.client.serialization.JsonbDeserializer;

public class BlogValidationResponseDeserializer extends JsonbDeserializer<BlogValidationResponse> {
    public BlogValidationResponseDeserializer() {
        super(BlogValidationResponse.class);
    }
}
