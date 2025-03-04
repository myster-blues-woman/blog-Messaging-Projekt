package com.example.service;

import com.example.dto.BlogValidationRequest;
import com.example.dto.BlogValidationResponse;
import com.example.model.Blog;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;
import java.util.Optional;

@ApplicationScoped
public class BlogService {

    private static final Logger LOG = Logger.getLogger(BlogService.class);

    @Inject
    @Channel("validation-requests")
    Emitter<BlogValidationRequest> blogValidationRequestEmitter;

    /**
     * Saves the blog entry in the database.
     *
     * @param blog The blog entry to be persisted.
     * @return The persisted blog entry.
     */
    @Transactional
    public Blog publishBlogEntry(Blog blog) {
        blog.persist();
        return blog;
    }

    /**
     * Prepares and validates the blog post by persisting it and initiating content
     * validation.
     *
     * @param blog The blog entry to be prepared and validated.
     */
    public void prepareAndValidateBlogPost(Blog blog) {
        Blog publishedBlog = publishBlogEntry(blog);
        initiateContentValidation(publishedBlog);
    }

    /**
     * Initiates the validation process for the blog content.
     *
     * @param blog The blog entry that needs to be validated.
     */
    private void initiateContentValidation(Blog blog) {
        LOG.infof("Initiating blog content validation for blog ID: %s", blog.id);
        BlogValidationRequest validationRequest = new BlogValidationRequest(blog.id, blog.name, blog.description);
        blogValidationRequestEmitter.send(validationRequest)
                .toCompletableFuture().join();

        LOG.info("Blog validation request sent successfully: " + validationRequest);
    }

    /**
     * Processes incoming validation responses and updates the validation status of
     * the corresponding blog entry.
     *
     * @param response The validation response containing the blog ID and validation
     *                 status.
     */
    @Incoming("validation-responses")
    @Transactional
    public void updateBlogValidationStatus(BlogValidationResponse response) {
        LOG.infof("Processing blog validation response: ID=%s, Valid=%s", response.id(), response.valid());

        Optional<Blog> blogOptional = Blog.findByIdOptional(response.id());
        if (blogOptional.isEmpty()) {
            LOG.warn("Blog entry not found for validation update");
            return;
        }

        Blog blog = blogOptional.get();
        blog.validated = response.valid();
        blog.persist();

        LOG.infof("Updated blog validation status: %s -> %s", blog.id, response.valid());
    }
}
