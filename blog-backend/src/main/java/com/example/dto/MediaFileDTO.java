package com.example.dto;

public class MediaFileDTO {
    public String fileName;
    public String url;
    public long size;
    public String lastModified;
    public String contentBase64;

    public MediaFileDTO(String fileName, String url, long size, String lastModified, String contentBase64) {
        this.fileName = fileName;
        this.url = url;
        this.size = size;
        this.lastModified = lastModified;
        this.contentBase64 = contentBase64;
    }
}
