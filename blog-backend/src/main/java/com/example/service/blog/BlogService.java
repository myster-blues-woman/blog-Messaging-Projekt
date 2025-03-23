package com.example.service.blog;

import com.example.model.blog.Blog;
import com.example.model.media.Media;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class BlogService {

    @Transactional
    public Blog prepareAndValidateBlogPost(Blog blog) {
        if (blog.title == null || blog.title.trim().isEmpty()) {
            throw new WebApplicationException("Blog title cannot be empty", Response.Status.BAD_REQUEST);
        }

        if (Blog.count("title = ?1 and id != ?2", blog.title, blog.id == null ? -1 : blog.id) > 0) {
            throw new WebApplicationException("Blog with this title already exists", Response.Status.CONFLICT);
        }

        if (blog.id == null) {
            blog.createdAt = LocalDateTime.now();
        }

        blog.persist();
        return blog;
    }

    @Transactional
    public Blog addMediaToPost(Long blogId, Long mediaId) {
        Blog blog = Blog.findById(blogId);
        if (blog == null) {
            throw new WebApplicationException("Blog not found", Response.Status.NOT_FOUND);
        }

        Media media = Media.findById(mediaId);
        if (media == null) {
            throw new WebApplicationException("Media not found", Response.Status.NOT_FOUND);
        }

        blog.addMedia(media);
        return blog;
    }

    public List<Blog> getAllBlogs() {
        return Blog.listAll();
    }

    public Blog getBlogById(Long id) {
        return Blog.findById(id);
    }

    public List<Blog> getValidatedBlogs() {
        return Blog.list("validated", true);
    }
}