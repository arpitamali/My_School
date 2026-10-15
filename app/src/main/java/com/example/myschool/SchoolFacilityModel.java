package com.example.myschool;

public class SchoolFacilityModel {

    private String id;
    private String name;
    private String description;
    private String schoolId;

    public SchoolFacilityModel() {
        // Required empty constructor for Firestore
    }

    public SchoolFacilityModel(
            String id,
            String name,
            String description,
            String schoolId
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.schoolId = schoolId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }
}

