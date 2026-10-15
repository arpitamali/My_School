package com.example.myschool;

public class EnquiryModel {

    private String id;
    private String name;
    private String phone;
    private String email;
    private String message;
    private String status;
    private String schoolId;
    private String date;

    public EnquiryModel() {
        // Required for Firestore
    }

    public EnquiryModel(
            String id,
            String name,
            String phone,
            String email,
            String message,
            String status,
            String schoolId,
            String date
    ) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.message = message;
        this.status = status;
        this.schoolId = schoolId;
        this.date = date;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
