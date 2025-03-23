package com.example.model.blog;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.example.model.media.Media;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "blog")
public class Blog extends PanacheEntity {

    @Column(nullable = false, unique = true)
    public String title;

    @Column(columnDefinition = "TEXT")
    public String content;

    public String headerImageUrl;

    public LocalDateTime createdAt;

    public boolean validated;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "blog_media", joinColumns = @JoinColumn(name = "blog_id"), inverseJoinColumns = @JoinColumn(name = "media_id"))
    public Set<Media> mediaFiles = new HashSet<>();

    public void addMedia(Media media) {
        if (this.mediaFiles == null) {
            this.mediaFiles = new HashSet<>();
        }
        this.mediaFiles.add(media);
    }
}