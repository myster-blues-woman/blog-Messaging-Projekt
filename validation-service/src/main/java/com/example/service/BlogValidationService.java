package com.example.service;

import com.example.dto.BlogValidationRequest;
import com.example.dto.BlogValidationResponse;

import java.util.List;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import io.smallrye.common.annotation.Blocking;
import org.jboss.logging.Logger;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BlogValidationService {

    private static final Logger LOG = Logger.getLogger(BlogValidationService.class);

    private static final List<String> FORBIDDEN_BLOG_NAMES = List.of(
            "admin", "moderator", "root", "null", "undefined", "test",
            "hacker", "spam", "fake", "banned", "offensive",
            "illegal", "violence", "hate", "nsfw", "scam",
            "bot", "dummy", "badword", "curse", "xxx", "forbidden");

    @Incoming("validation-requests")
    @Outgoing("validation-responses-out")
    @Blocking
    public BlogValidationResponse processBlogValidation(BlogValidationRequest message) {
        LOG.info("Processing blog validation request: " + message);

        boolean isBlogNameValid = isSuitableBlogName(message.name());
        boolean isBlogDescriptionValid = isAppropriateDescription(message.description());

        boolean isValidBlogPost = isBlogNameValid && isBlogDescriptionValid;

        LOG.infof("Blog validation result for '%s': %s", message.name(), isValidBlogPost);

        return new BlogValidationResponse(message.id(), isValidBlogPost);
    }

    private boolean isSuitableBlogName(String blogName) {
        if (blogName == null || blogName.trim().isEmpty()) {
            LOG.warn("Blog name validation failed: Name is empty.");
            return false;
        }

        for (String forbiddenWord : FORBIDDEN_BLOG_NAMES) {
            if (blogName.equalsIgnoreCase(forbiddenWord)) {
                LOG.warnf("Blog name validation failed: Forbidden name '%s' found.", forbiddenWord);
                return false;
            }
        }
        return true;
    }

    private boolean isAppropriateDescription(String blogDescription) {
        if (blogDescription == null || blogDescription.trim().isEmpty()) {
            LOG.warn("Blog description validation failed: Description is empty.");
            return false;
        }

        return true;
    }
}