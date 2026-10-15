package com.example.myschool;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ParentStudentFeatureActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String feature = "", studentId = "", schoolId = "";
    private String className = "", division = "", studentName = "";

    private LinearLayout list;
    private TextView subtitle;
    private final List<ListenerRegistration> listeners = new ArrayList<>();
    private final java.util.HashSet<String> addedKeys = new java.util.HashSet<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();

        feature = safe(getIntent().getStringExtra("feature"));
        studentId = safe(getIntent().getStringExtra("studentId"));
        schoolId = safe(getIntent().getStringExtra("schoolId"));

        buildScreen();
        loadStudent();
    }

    private void buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(247, 247, 243));

        TextView back = text("‹  Back", 16, 0xFF5A4A35, true);
        back.setPadding(dp(18), dp(14), dp(18), dp(14));
        back.setBackgroundColor(Color.WHITE);
        back.setOnClickListener(v -> finish());

        // Back button top margin = 20dp
        LinearLayout.LayoutParams backParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        backParams.topMargin = dp(20);
        root.addView(back, backParams);

        String title;

        switch (feature) {
            case "homework":
                title = "Homework";
                break;

            case "exam":
                title = "Exam & Marks";
                break;

            case "fees":
                title = "Fees";
                break;

            case "timetable":
                title = "Class Timetable";
                break;

            default:
                title = "Student Details";
                break;
        }

        TextView heading = text(title, 22, 0xFF222222, true);
        heading.setPadding(dp(16), dp(18), dp(16), dp(5));
        root.addView(heading);

        subtitle = text("Loading student details…", 13, 0xFF777777, false);
        subtitle.setPadding(dp(16), 0, dp(16), dp(14));
        root.addView(subtitle);

        ScrollView scroll = new ScrollView(this);

        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(14), dp(4), dp(14), dp(24));

        scroll.addView(list);

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(-1, 0, 1)
        );

        setContentView(root);
    }

    private void loadStudent() {
        if (studentId.isEmpty()) {
            message("Student ID not available.");
            return;
        }

        db.collection("students")
                .document(studentId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        message("Student record not found.");
                        return;
                    }

                    studentName = join(
                            s(doc, "firstName"),
                            s(doc, "middleName"),
                            s(doc, "lastName")
                    );

                    if (studentName.isEmpty()) {
                        studentName = s(doc, "studentName");
                    }

                    className = s(doc, "className");
                    division = s(doc, "division");

                    if (schoolId.isEmpty()) {
                        schoolId = s(doc, "schoolId");
                    }

                    subtitle.setText(
                            studentName
                                    + "  •  Class "
                                    + className
                                    + (division.isEmpty()
                                    ? ""
                                    : " - " + division)
                    );

                    loadFeature();
                })
                .addOnFailureListener(
                        e -> message("Unable to load student information.")
                );
    }

    private void loadFeature() {
        list.removeAllViews();

        switch (feature) {
            case "homework":
                loadHomework();
                break;

            case "exam":
                loadExamsAndMarks();
                break;

            case "fees":
                loadFees();
                break;

            case "timetable":
                loadTimetable();
                break;

            default:
                message("Feature not available.");
                break;
        }
    }

    private void loadHomework() {
        if (schoolId.isEmpty()) {
            message("School information is missing.");
            return;
        }

        ListenerRegistration registration =
                db.collection("homework")
                        .whereEqualTo("schoolId", schoolId)
                        .addSnapshotListener((snap, error) -> {

                            if (error != null) {
                                message("Unable to load homework.");
                                return;
                            }

                            list.removeAllViews();
                            int count = 0;

                            if (snap != null) {
                                List<DocumentSnapshot> docs =
                                        new ArrayList<>(snap.getDocuments());

                                Collections.sort(
                                        docs,
                                        (a, b) -> s(b, "createdAt")
                                                .compareTo(s(a, "createdAt"))
                                );

                                for (DocumentSnapshot d : docs) {
                                    String targetClass =
                                            first(d, "className", "class");

                                    if (!sameClass(targetClass, className)) {
                                        continue;
                                    }

                                    String targetDivision =
                                            first(d, "division", "section");

                                    if (!targetDivision.isEmpty()
                                            && !sameDivision(
                                            targetDivision, division)) {
                                        continue;
                                    }

                                    String title =
                                            first(d, "title", "homeworkTitle");

                                    String subject = s(d, "subject");

                                    String description =
                                            first(d, "description", "details");

                                    String dueDate =
                                            first(
                                                    d,
                                                    "dueDate",
                                                    "submissionDate",
                                                    "lastDate"
                                            );

                                    String date =
                                            first(d, "date", "createdAt");

                                    String body =
                                            (subject.isEmpty()
                                                    ? ""
                                                    : "Subject: " + subject + "\n")
                                                    + (description.isEmpty()
                                                    ? ""
                                                    : description + "\n")
                                                    + (date.isEmpty()
                                                    ? ""
                                                    : "Assigned: " + date + "\n")
                                                    + (dueDate.isEmpty()
                                                    ? "Due date: Not specified"
                                                    : "Due date: " + dueDate);

                                    addCard(
                                            title.isEmpty()
                                                    ? "Homework"
                                                    : title,
                                            body
                                    );

                                    count++;
                                }
                            }

                            if (count == 0) {
                                message("No homework assigned to this class yet.");
                            }
                        });

        listeners.add(registration);
    }

    private void loadExamsAndMarks() {
        if (schoolId.isEmpty()) {
            message("School information is missing.");
            return;
        }

        ListenerRegistration examRegistration =
                db.collection("conductTests")
                        .whereEqualTo("schoolId", schoolId)
                        .addSnapshotListener((snap, error) -> {

                            if (error != null) {
                                message("Unable to load exam list.");
                                return;
                            }

                            if (snap == null) return;

                            for (DocumentSnapshot d : snap.getDocuments()) {
                                String targetClass =
                                        first(d, "className", "class");

                                if (!sameClass(targetClass, className)) {
                                    continue;
                                }

                                String targetDivision =
                                        first(d, "division", "section");

                                if (!targetDivision.isEmpty()
                                        && !sameDivision(
                                        targetDivision, division)) {
                                    continue;
                                }

                                String topic =
                                        first(d, "topic", "testName", "examName");

                                String subject = s(d, "subject");

                                String date =
                                        first(d, "testDate", "examDate", "date");

                                String time = s(d, "testTime");

                                String key = "test_" + d.getId();

                                if (!alreadyAdded(key)) {
                                    addCard(
                                            topic.isEmpty()
                                                    ? "Conducted Exam"
                                                    : topic,
                                            (subject.isEmpty()
                                                    ? ""
                                                    : "Subject: " + subject + "\n")
                                                    + "Exam date: "
                                                    + (date.isEmpty()
                                                    ? "Not specified"
                                                    : date)
                                                    + (time.isEmpty()
                                                    ? ""
                                                    : "\nTime: " + time)
                                                    + "\nMarks: See marks section below"
                                    );

                                    markAdded(key);
                                }
                            }
                        });

        listeners.add(examRegistration);

        ListenerRegistration marksRegistration =
                db.collection("studentMarks")
                        .whereEqualTo("schoolId", schoolId)
                        .whereEqualTo("studentId", studentId)
                        .addSnapshotListener((snap, error) -> {

                            if (error != null) {
                                message("Unable to load marks.");
                                return;
                            }

                            if (snap == null) return;

                            for (DocumentSnapshot d : snap.getDocuments()) {
                                String exam =
                                        first(d, "examName", "testName", "topic");

                                String subject = s(d, "subject");

                                String date =
                                        first(d, "examDate", "testDate", "date");

                                String obtained =
                                        first(d, "obtainedMarks", "marks");

                                String maximum =
                                        first(d, "maxMarks", "totalMarks", "outOf");

                                String percentage = s(d, "percentage");

                                String body =
                                        (subject.isEmpty()
                                                ? ""
                                                : "Subject: " + subject + "\n")
                                                + (date.isEmpty()
                                                ? ""
                                                : "Date: " + date + "\n")
                                                + "Marks: "
                                                + (obtained.isEmpty()
                                                ? "Not entered"
                                                : obtained)
                                                + (maximum.isEmpty()
                                                ? ""
                                                : " / " + maximum)
                                                + (percentage.isEmpty()
                                                ? ""
                                                : "\nPercentage: "
                                                + percentage + "%");

                                String key = "mark_" + d.getId();

                                if (!alreadyAdded(key)) {
                                    addCard(
                                            exam.isEmpty()
                                                    ? "Exam Marks"
                                                    : exam,
                                            body
                                    );

                                    markAdded(key);
                                }
                            }
                        });

        listeners.add(marksRegistration);

        message("Loading exams and marks…");
    }

    private void loadFees() {
        ListenerRegistration registration =
                db.collection("students")
                        .document(studentId)
                        .addSnapshotListener((doc, error) -> {

                            if (error != null || doc == null || !doc.exists()) {
                                message("Unable to load fee details.");
                                return;
                            }

                            list.removeAllViews();

                            double total = number(doc.get("totalFees"));
                            double paid = number(doc.get("paidFees"));

                            double remaining =
                                    doc.contains("remainingFees")
                                            ? number(doc.get("remainingFees"))
                                            : Math.max(0, total - paid);

                            addCard("Total Fees", "₹ " + money(total));
                            addCard("Paid Amount", "₹ " + money(paid));
                            addCard("Remaining Amount", "₹ " + money(remaining));

                            String paymentDate =
                                    first(doc, "lastPaymentDate", "paymentDate");

                            String paymentAmount =
                                    first(doc, "lastPaymentAmount", "paidAmount");

                            String dueDate =
                                    first(
                                            doc,
                                            "feeDueDate",
                                            "dueDate",
                                            "remainingDueDate"
                                    );

                            addCard(
                                    "Payment Details",
                                    "Last payment: "
                                            + (paymentDate.isEmpty()
                                            ? "Not recorded"
                                            : paymentDate)
                                            + "\nLast payment amount: ₹ "
                                            + (paymentAmount.isEmpty()
                                            ? "Not recorded"
                                            : paymentAmount)
                                            + "\nNext due date: "
                                            + (dueDate.isEmpty()
                                            ? "Not specified by school"
                                            : dueDate)
                            );
                        });

        listeners.add(registration);
    }

    // =========================================================
    // TIMETABLE - UPDATED
    // =========================================================

    private void loadTimetable() {
        if (schoolId.isEmpty()) {
            message("School information is missing.");
            return;
        }

        ListenerRegistration registration =
                db.collection("timetables")
                        .whereEqualTo("schoolId", schoolId)
                        .addSnapshotListener((snap, error) -> {

                            if (error != null) {
                                message("Unable to load timetable.");
                                return;
                            }

                            list.removeAllViews();

                            List<DocumentSnapshot> docs = new ArrayList<>();

                            if (snap != null) {
                                for (DocumentSnapshot d : snap.getDocuments()) {

                                    String timetableClass =
                                            first(d, "className", "class");

                                    // Match class formats such as Class 5, 5, 5th.
                                    if (!sameClass(timetableClass, className)) {
                                        continue;
                                    }

                                    String timetableDivision =
                                            first(d, "division", "section");

                                    if (!timetableDivision.isEmpty()
                                            && !sameDivision(
                                            timetableDivision, division)) {
                                        continue;
                                    }

                                    docs.add(d);
                                }
                            }

                            // Monday to Saturday order.
                            Collections.sort(docs, (a, b) -> {
                                int dayComparison = Integer.compare(
                                        dayOrder(first(a, "day", "dayName", "weekday")),
                                        dayOrder(first(b, "day", "dayName", "weekday"))
                                );

                                if (dayComparison != 0) {
                                    return dayComparison;
                                }

                                int periodComparison = Integer.compare(
                                        (int) number(a.get("periodNumber")),
                                        (int) number(b.get("periodNumber"))
                                );

                                if (periodComparison != 0) {
                                    return periodComparison;
                                }

                                return s(a, "startTime")
                                        .compareToIgnoreCase(s(b, "startTime"));
                            });

                            String lastDay = "";
                            int count = 0;

                            for (DocumentSnapshot d : docs) {
                                String day =
                                        first(d, "day", "dayName", "weekday");

                                if (!day.equalsIgnoreCase(lastDay)) {
                                    TextView dayHeader = text(
                                            day.isEmpty() ? "Day" : day,
                                            16,
                                            0xFF5A4A35,
                                            true
                                    );

                                    dayHeader.setPadding(
                                            dp(8), dp(14), dp(8), dp(5)
                                    );

                                    list.addView(dayHeader);
                                    lastDay = day;
                                }

                                String period =
                                        first(d, "periodNumber", "period");

                                String subject = s(d, "subject");

                                String startTime = s(d, "startTime");
                                String endTime = s(d, "endTime");

                                String time = startTime;

                                if (!endTime.isEmpty()) {
                                    time += (time.isEmpty() ? "" : " - ")
                                            + endTime;
                                }

                                String teacher = s(d, "teacherName");

                                String body =
                                        "Time: "
                                                + (time.isEmpty()
                                                ? "Not specified"
                                                : time)
                                                + (teacher.isEmpty()
                                                ? ""
                                                : "\nTeacher: " + teacher);

                                addCard(
                                        (period.isEmpty()
                                                ? ""
                                                : "Period " + period + " • ")
                                                + (subject.isEmpty()
                                                ? "Subject not set"
                                                : subject),
                                        body
                                );

                                count++;
                            }

                            if (count == 0) {
                                message(
                                        "No timetable found for this class/division."
                                );
                            }
                        });

        listeners.add(registration);
    }

    private boolean alreadyAdded(String key) {
        return addedKeys.contains(key);
    }

    private void markAdded(String key) {
        addedKeys.add(key);
    }

    private void addCard(String title, String body) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(15), dp(13), dp(15), dp(13));
        card.setBackgroundColor(Color.WHITE);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, -2);

        params.setMargins(0, 0, 0, dp(10));
        card.setLayoutParams(params);

        TextView titleView = text(title, 16, 0xFF222222, true);
        card.addView(titleView);

        TextView bodyView = text(
                body == null || body.trim().isEmpty()
                        ? "Details not provided."
                        : body,
                14,
                0xFF666666,
                false
        );

        bodyView.setPadding(0, dp(7), 0, 0);
        card.addView(bodyView);

        list.addView(card);
    }

    private void message(String value) {
        if (list == null) return;

        if (list.getChildCount() == 0) {
            TextView textView = text(value, 14, 0xFF777777, false);
            textView.setPadding(dp(8), dp(16), dp(8), dp(16));
            list.addView(textView);
        }
    }

    private TextView text(
            String value,
            int size,
            int color,
            boolean bold
    ) {
        TextView textView = new TextView(this);
        textView.setText(value);
        textView.setTextSize(size);
        textView.setTextColor(color);

        if (bold) {
            textView.setTypeface(null, Typeface.BOLD);
        }

        return textView;
    }

    private String s(DocumentSnapshot doc, String key) {
        Object value = doc.get(key);
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String first(DocumentSnapshot doc, String... keys) {
        for (String key : keys) {
            String value = s(doc, key);

            if (!value.isEmpty()) {
                return value;
            }
        }

        return "";
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String join(String... values) {
        StringBuilder builder = new StringBuilder();

        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                if (builder.length() > 0) {
                    builder.append(' ');
                }

                builder.append(value.trim());
            }
        }

        return builder.toString();
    }

    private boolean same(String a, String b) {
        return a != null
                && b != null
                && a.trim().equalsIgnoreCase(b.trim());
    }

    private boolean sameClass(String a, String b) {
        if (same(a, b)) return true;

        String normalizedA = normalizeClass(a);
        String normalizedB = normalizeClass(b);

        return !normalizedA.isEmpty()
                && normalizedA.equals(normalizedB);
    }

    private String normalizeClass(String value) {
        if (value == null) return "";

        String normalized = value.trim().toLowerCase(Locale.ROOT);

        normalized = normalized.replaceAll("^class\\s*", "");
        normalized = normalized.replaceAll("(st|nd|rd|th)$", "");
        normalized = normalized.replaceAll("\\s+", "");

        return normalized;
    }

    private boolean sameDivision(String a, String b) {
        if (same(a, b)) return true;
        if (a == null || b == null) return false;

        return a.trim().replaceAll("\\s+", "")
                .equalsIgnoreCase(b.trim().replaceAll("\\s+", ""));
    }

    private double number(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return 0;
        }
    }

    private String money(double value) {
        return String.format(Locale.getDefault(), "%.2f", value);
    }

    private int dayOrder(String day) {
        String value = day == null
                ? ""
                : day.trim().toLowerCase(Locale.ROOT);

        switch (value) {
            case "mon":
            case "monday":
                return 1;

            case "tue":
            case "tues":
            case "tuesday":
                return 2;

            case "wed":
            case "wednesday":
                return 3;

            case "thu":
            case "thur":
            case "thurs":
            case "thursday":
                return 4;

            case "fri":
            case "friday":
                return 5;

            case "sat":
            case "saturday":
                return 6;

            case "sun":
            case "sunday":
                return 7;

            default:
                return 8;
        }
    }

    private int dp(int value) {
        return (int) (
                value * getResources().getDisplayMetrics().density + 0.5f
        );
    }

    @Override
    protected void onDestroy() {
        for (ListenerRegistration registration : listeners) {
            if (registration != null) {
                registration.remove();
            }
        }

        super.onDestroy();
    }
}