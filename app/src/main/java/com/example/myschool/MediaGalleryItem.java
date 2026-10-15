package com.example.myschool;

public class MediaGalleryItem {

    private String documentId;
    private String imageUrl;
    private String storagePath;

    public MediaGalleryItem() {
    }

    public MediaGalleryItem(
            String documentId,
            String imageUrl,
            String storagePath
    ) {
        this.documentId = documentId;
        this.imageUrl = imageUrl;
        this.storagePath = storagePath;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getStoragePath() {
        return storagePath;
    }
}