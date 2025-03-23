### README: Media Management in Blog Messaging Project

#### Overview

The media management functionality in the Blog Messaging Project provides a robust system for handling file uploads, downloads, storage, and metadata management. It leverages  **MinIO**  as the storage backend and integrates with the application using Quarkus and Jakarta EE frameworks.

----------

### Key Components

#### 1.  **Media Entity**

The  Media  class represents the metadata of uploaded files stored in the database.

-   **File Attributes**:
    
    -   fileName: The unique name of the file in storage.
    -   originalFilename: The original name of the uploaded file.
    -   contentType: The MIME type of the file (e.g.,  `image/jpeg`,  `application/pdf`).
    -   size: The size of the file in bytes.
    -   storagePath: The path where the file is stored in MinIO.
    -   createdAt: The timestamp when the file was uploaded.
    -   createdBy: The user who uploaded the file.
-   **Location**:  
    Media.java
    

----------

#### 2.  **Media Service**

The  MediaService  class handles the business logic for media operations, including file uploads, downloads, and listing all media.

-   **Key Methods**:
    
    -   uploadImage: Validates and uploads a file to MinIO, then saves its metadata to the database.
    -   listAllMedia: Retrieves a list of all media files, including their metadata and content (via MinIO).
    -   downloadFile: Fetches a file from MinIO for download.
-   **Dependencies**:
    
    -   MinioService: Handles direct interactions with MinIO.
    -   MediaRepository: Manages database operations for the  Media  entity.
-   **Location**:  
    MediaService.java
    

----------

#### 3.  **MinIO Integration**

The  MinioService  class provides a direct interface to the MinIO storage backend.

-   **Key Features**:
    
    -   **File Upload**: Uploads files to a specified bucket in MinIO.
    -   **File Download**: Retrieves files from MinIO, with caching for improved performance.
    -   **File Deletion**: Deletes files from MinIO.
    -   **Bucket Initialization**: Ensures the required bucket exists in MinIO.
    -   **List Files**: Retrieves all files in the bucket, including their content encoded in Base64.
-   **Caching**:  
    Uses  **Caffeine Cache**  to store recently accessed files for 10 minutes to reduce storage access latency.
    
-   **Location**:  
    MinioService.java
    

----------

#### 4.  **Media Resource**

The  MediaResource  class exposes RESTful endpoints for interacting with media files.

-   **Endpoints**:
    
    -   POST /api/media/upload: Uploads a file.
    -   GET /api/media/: Lists all media files with metadata.
    -   GET /api/media/download/{fileName}: Downloads a specific file.
-   **Validation**:
    
    -   Only allows specific file types (e.g., images, PDFs, text files).
    -   Generates unique filenames to avoid collisions.
-   **Location**:  
    MediaResource.java
    

----------

#### 5.  **MediaFileDTO**

The  MediaFileDTO  class is a Data Transfer Object used to encapsulate file metadata and content for API responses.

-   **Attributes**:
    
    -   fileName: The name of the file.
    -   url: The URL to access the file.
    -   size: The size of the file in bytes.
    -   lastModified: The last modified timestamp of the file.
    -   contentBase64: The Base64-encoded content of the file.
-   **Location**:  
    MediaFileDTO.java
    

----------

### Workflow

1.  **File Upload**:
    
    -   A user uploads a file via the  POST /api/media/upload  endpoint.
    -   The file is validated, uploaded to MinIO, and its metadata is saved to the database.
2.  **File Listing**:
    
    -   The  GET /api/media/  endpoint retrieves all media files, including metadata and Base64-encoded content.
3.  **File Download**:
    
    -   The  GET /api/media/download/{fileName}  endpoint fetches a file from MinIO and streams it to the client.
4.  **File Deletion**  (Optional):
    
    -   Files can be deleted from MinIO using the  deleteFile  method in  MinioService.

----------

### Configuration

The following configuration properties are required for MinIO integration:

-   **MinIO Settings**:
    
    -   quarkus.minio.url: The MinIO server URL.
    -   quarkus.minio.access-key: The access key for MinIO.
    -   quarkus.minio.secret-key: The secret key for MinIO.
    -   `app.storage.bucket`: The bucket name for storing files.
-   **Allowed File Types**:
    
    -   Configured in  MediaResource  as a list of MIME types (e.g.,  `image/jpeg`,  `application/pdf`).

----------

### Error Handling

-   **Upload Errors**:
    
    -   Invalid file types or failed uploads return a  `400 Bad Request`  or  `500 Internal Server Error`.
-   **Download Errors**:
    
    -   Missing or inaccessible files return a  `500 Internal Server Error`.
-   **MinIO Errors**:
    
    -   Any issues with MinIO operations (e.g., bucket creation, file retrieval) are wrapped in runtime exceptions.

----------

### Future Enhancements

-   Add support for file versioning.
-   Implement soft deletion for files (mark as deleted instead of immediate removal).
-   Enhance security by restricting access to specific users or roles.

----------

This README provides an overview of the media management functionality in the Blog Messaging Project. For further details, refer to the source code and API documentation.