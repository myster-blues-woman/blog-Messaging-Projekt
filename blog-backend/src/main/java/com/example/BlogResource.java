package com.example;

import com.example.model.Blog;
import com.example.service.BlogService;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/blog")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Blog API", description = "API for managing blogs")
@Transactional
public class BlogResource {

    @Inject
    BlogService blogService;

    @GET
    @Operation(summary = "Retrieve all blogs", description = "Returns a list of all stored blogs")
    public List<Blog> listAll() {
        return Blog.listAll();
    }

    @POST
    @Operation(summary = "Save a new blog", description = "Stores a blog entry in the database")
    public Blog create(Blog blog) {
        blogService.prepareAndValidateBlogPost(blog);
        return blog;
    }

    @GET
    @Path("validated")
    @Operation(summary = "Retrieve validated blogs", description = "Returns a list of all validated blogs")
    public List<Blog> listValidated() {
        return Blog.list("validated", true);
    }
}
