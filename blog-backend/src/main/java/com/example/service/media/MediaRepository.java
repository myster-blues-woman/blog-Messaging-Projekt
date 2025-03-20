package com.example.service.media;

import java.util.List;

import com.example.model.media.Media;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MediaRepository implements PanacheRepository<Media> {
    public List<Media> findByContentType(String contentType, int page, int size) {
        return find("contentType", contentType)
                .page(page, size)
                .list();
    }

    public List<Media> findByPostId(Long postId) {
        return find("SELECT m FROM Media m JOIN m.posts p WHERE p.id = ?1", postId).list();
    }

    public List<Media> findRecentlyUploaded(int limit) {
        return find("ORDER BY createdAt DESC")
                .page(0, limit)
                .list();
    }

    public Media save(Media media) {
        persist(media);
        return media;
    }
}