package com.example.myschool;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DemoDataStore extends SQLiteOpenHelper {

    private static final String DB_NAME = "myschool_demo_only.db";
    private static final int DB_VERSION = 1;

    public DemoDataStore(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE demo_records (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "category TEXT NOT NULL," +
                "title TEXT NOT NULL," +
                "details TEXT," +
                "created_at TEXT NOT NULL)");

        seed(db, "Classes", "Class 5 - A", "Sample demo class");
        seed(db, "Students", "Aarav Patil", "Class: 5 • Division: A • Demo student");
        seed(db, "Students", "Anaya Shah", "Class: 5 • Division: A • Demo student");
        seed(db, "Teachers", "Demo Teacher", "Subject: Mathematics");
        seed(db, "Attendance", "Aarav Patil — Present", "Date: Today • Class 5-A");
        seed(db, "Attendance", "Anaya Shah — Present", "Date: Today • Class 5-A");
        seed(db, "Homework", "Mathematics Homework", "Complete textbook exercise 3.1 • Class 5-A");
        seed(db, "Exams", "Unit Test 1", "Subject: Mathematics • Date: Friday • Class 5-A");
        seed(db, "Marks", "Aarav Patil — Mathematics", "Marks: 18 / 20 • Unit Test 1");
        seed(db, "Fees", "Aarav Patil — Fees", "Total: ₹10,000 • Paid: ₹6,000 • Remaining: ₹4,000");
        seed(db, "Timetable", "Monday • Period 1", "Mathematics • 09:00 AM - 09:40 AM • Demo Teacher");
        seed(db, "Timetable", "Tuesday • Period 1", "English • 09:00 AM - 09:40 AM • Demo Teacher");
        seed(db, "Timetable", "Wednesday • Period 1", "Science • 09:00 AM - 09:40 AM • Demo Teacher");
        seed(db, "Timetable", "Thursday • Period 1", "History • 09:00 AM - 09:40 AM • Demo Teacher");
        seed(db, "Timetable", "Friday • Period 1", "Mathematics • 09:00 AM - 09:40 AM • Demo Teacher");
        seed(db, "Timetable", "Saturday • Period 1", "Sports • 09:00 AM - 09:40 AM • Demo Teacher");
        seed(db, "Notices", "Welcome to MySCHOOL Demo", "This is sample notice data. It is not a real school notice.");
    }

    private void seed(SQLiteDatabase db, String category, String title, String details) {
        ContentValues values = new ContentValues();
        values.put("category", category);
        values.put("title", title);
        values.put("details", details);
        values.put("created_at", now());
        db.insert("demo_records", null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Keep existing local demo records when the app is updated.
    }

    public long addRecord(String category, String title, String details) {
        ContentValues values = new ContentValues();
        values.put("category", category);
        values.put("title", title);
        values.put("details", details);
        values.put("created_at", now());
        return getWritableDatabase().insert("demo_records", null, values);
    }

    public ArrayList<DemoRecord> getRecords(String category) {
        ArrayList<DemoRecord> records = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(
                "demo_records",
                new String[]{"id", "category", "title", "details", "created_at"},
                "category = ?",
                new String[]{category},
                null, null, "id DESC"
        );

        try {
            while (cursor.moveToNext()) {
                records.add(new DemoRecord(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4)
                ));
            }
        } finally {
            cursor.close();
        }
        return records;
    }

    private String now() {
        return new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                .format(new Date());
    }

    public static class DemoRecord {
        public final long id;
        public final String category;
        public final String title;
        public final String details;
        public final String createdAt;

        public DemoRecord(long id, String category, String title, String details, String createdAt) {
            this.id = id;
            this.category = category;
            this.title = title;
            this.details = details == null ? "" : details;
            this.createdAt = createdAt == null ? "" : createdAt;
        }
    }
}
