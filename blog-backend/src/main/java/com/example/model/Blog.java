package com.example.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class Blog extends PanacheEntity {

    @Schema(hidden = true)
    public Long id;

    public String name;
    public String description;

    @Schema(hidden = true)
    public boolean validated = false;
}
