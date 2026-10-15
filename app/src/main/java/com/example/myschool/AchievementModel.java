package com.example.myschool;

public class AchievementModel {

    private String documentId;
    private String title;
    private String description;
    private String date;

    public AchievementModel() {
    }

    public AchievementModel(
            String documentId,
            String title,
            String description,
            String date
    ) {
        this.documentId = documentId;
        this.title = title;
        this.description = description;
        this.date = date;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }
}