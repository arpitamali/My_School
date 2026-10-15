package com.example.myschool;

public class AttendanceRecord {

    private final String date;
    private final String status;


    public AttendanceRecord(
            String date,
            String status
    ) {

        this.date = date;
        this.status = status;
    }


    public String getDate() {

        return date;
    }


    public String getStatus() {

        return status;
    }
}