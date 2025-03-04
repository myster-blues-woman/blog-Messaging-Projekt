package com.example.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record BlogValidationResponse(long id, boolean valid) {
}
