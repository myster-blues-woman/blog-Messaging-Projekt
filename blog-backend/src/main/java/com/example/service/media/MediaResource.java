package com.example.service.media;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import com.example.dto.MediaFileDTO;
import com.example.model.media.Media;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/api/media")
public class MediaResource {

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "application/pdf", "text/plain");

    @Inject
    MediaService mediaService;

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Datei hochladen", description = "Lädt eine Datei (Bild, PDF oder Text) hoch")
    @RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @Schema(type = SchemaType.OBJECT, requiredProperties = {
            "file" })))
    @APIResponse(responseCode = "201", description = "Datei erfolgreich hochgeladen", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Media.class)))
    public Response uploadFile(@RestForm("file") FileUpload file) {
        try {
            // Validate file exists
            if (file == null) {
                return Response.status(Status.BAD_REQUEST)
                        .entity("{\"error\": \"No file uploaded\"}").build();
            }

            String contentType = file.contentType();

            // Validate content type
            if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
                return Response.status(Status.BAD_REQUEST)
                        .entity("{\"error\": \"Unsupported file type: " + contentType +
                                ". Allowed types: images (JPEG, PNG, GIF), PDF, and text files\"}")
                        .build();
            }

            // Generate unique filename to avoid collisions
            String originalFilename = file.fileName();
            String extension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID().toString() + "." + extension;

            Media savedMedia = mediaService.uploadImage(
                    uniqueFilename,
                    Files.newInputStream(file.uploadedFile()),
                    file.size(),
                    contentType);

            return Response.status(Status.CREATED)
                    .entity(savedMedia)
                    .build();

        } catch (IOException e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to upload file: " + e.getMessage() + "\"}")
                    .build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to process file: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            return filename.substring(dotIndex + 1).toLowerCase();
        }
        return ""; // No extension
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Liste aller Medien abrufen", description = "Gibt eine Liste aller gespeicherten Mediendateien zurück")
    @APIResponse(responseCode = "200", description = "Liste der Mediendateien", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Media.class)))
    public Response listMedia() {
        try {
            List<MediaFileDTO> mediaList = mediaService.listAllMedia();
            return Response.ok(mediaList).build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to fetch media: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/download/{fileName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(summary = "Download einer Mediendatei", description = "Lädt eine gespeicherte Datei direkt herunter")
    @APIResponse(responseCode = "200", description = "Datei wird heruntergeladen")
    public Response downloadFile(@jakarta.ws.rs.PathParam("fileName") String fileName) {
        try {
            InputStream fileStream = mediaService.downloadFile(fileName);
            return Response.ok(fileStream)
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to download file: " + e.getMessage() + "\"}")
                    .build();
        }
    }

}