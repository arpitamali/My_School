package com.example.myschool;

public class TimetableModel {

    private String startTime;
    private String endTime;
    private String subject;
    private String className;
    private String division;
    private boolean recess;

    public TimetableModel() {
    }

    public TimetableModel(
            String startTime,
            String endTime,
            String subject,
            String className,
            String division,
            boolean recess
    ) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.subject = subject;
        this.className = className;
        this.division = division;
        this.recess = recess;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public boolean isRecess() {
        return recess;
    }

    public void setRecess(boolean recess) {
        this.recess = recess;
    }
}