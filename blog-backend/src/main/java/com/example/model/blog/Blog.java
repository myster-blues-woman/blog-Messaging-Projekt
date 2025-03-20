package com.example.model.blog;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.example.model.media.Media;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "blog")
public class Blog extends PanacheEntity {

    @Schema(hidden = true)
    public Long id;

    public String name;
    public String description;

    @Schema(hidden = true)
    public boolean validated = false;

    @ManyToOne
    @JoinColumn(name = "media_id")
    private Media media;

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }
}