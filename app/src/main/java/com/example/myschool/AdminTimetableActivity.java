package com.example.myschool;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AdminTimetableActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Spinner spinnerClass;
    private Spinner spinnerDivision;
    private Spinner spinnerDay;
    private LinearLayout timetableContainer;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String schoolId = "";

    private final List<String> classList = new ArrayList<>();
    private final List<String> divisionList = new ArrayList<>();
    private final List<String> dayList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_timetable);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        btnBack = findViewById(R.id.btnBack);
        spinnerClass = findViewById(R.id.spinnerAdminClass);
        spinnerDivision = findViewById(R.id.spinnerAdminDivision);
        spinnerDay = findViewById(R.id.spinnerAdminDay);
        timetableContainer = findViewById(R.id.adminTimetableContainer);

        btnBack.setOnClickListener(v -> finish());

        setupDays();
        setupSpinnerListeners();
        loadSchoolId();
    }

    private void loadSchoolId() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        schoolId = auth.getCurrentUser().getUid();
        loadClassesAndDivisions();
    }

    private void setupDays() {
        Collections.addAll(dayList,
                "All Days", "Monday", "Tuesday", "Wednesday",
                "Thursday", "Friday", "Saturday");

        spinnerDay.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                dayList
        ));
    }

    private void loadClassesAndDivisions() {
        db.collection("students")
                .whereEqualTo("schoolId", schoolId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    Set<String> classes = new HashSet<>();
                    Set<String> divisions = new HashSet<>();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String status = safe(doc.getString("status"));
                        if ("Deleted".equalsIgnoreCase(status)) continue;

                        String className = safe(doc.getString("className"));
                        String division = safe(doc.getString("division"));

                        if (!className.isEmpty()) classes.add(className);
                        if (!division.isEmpty()) divisions.add(division);
                    }

                    classList.clear();
                    classList.add("All Classes");
                    classList.addAll(classes);
                    sortAfterFirst(classList);

                    divisionList.clear();
                    divisionList.add("All Divisions");
                    divisionList.addAll(divisions);
                    sortAfterFirst(divisionList);

                    spinnerClass.setAdapter(new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_spinner_dropdown_item,
                            classList
                    ));

                    spinnerDivision.setAdapter(new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_spinner_dropdown_item,
                            divisionList
                    ));

                    loadTimetable();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Unable to load classes: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    private void setupSpinnerListeners() {
        spinnerClass.setOnItemSelectedListener(new SimpleItemListener() {
            @Override public void onItemSelected(int position) { loadTimetable(); }
        });
        spinnerDivision.setOnItemSelectedListener(new SimpleItemListener() {
            @Override public void onItemSelected(int position) { loadTimetable(); }
        });
        spinnerDay.setOnItemSelectedListener(new SimpleItemListener() {
            @Override public void onItemSelected(int position) { loadTimetable(); }
        });
    }

    private void loadTimetable() {
        if (TextUtils.isEmpty(schoolId) || timetableContainer == null) return;

        timetableContainer.removeAllViews();
        addMessage("Loading timetable...");

        db.collection("timetables")
                .whereEqualTo("schoolId", schoolId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    timetableContainer.removeAllViews();

                    String selectedClass = selected(spinnerClass);
                    String selectedDivision = selected(spinnerDivision);
                    String selectedDay = selected(spinnerDay);

                    List<DocumentSnapshot> rows = new ArrayList<>();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String className = safe(doc.getString("className"));
                        String division = safe(doc.getString("division"));
                        String day = safe(doc.getString("day"));

                        if (!"All Classes".equals(selectedClass)
                                && !same(className, selectedClass)) continue;
                        if (!"All Divisions".equals(selectedDivision)
                                && !same(division, selectedDivision)) continue;
                        if (!"All Days".equals(selectedDay)
                                && !same(day, selectedDay)) continue;

                        rows.add(doc);
                    }

                    Collections.sort(rows, new Comparator<DocumentSnapshot>() {
                        @Override
                        public int compare(DocumentSnapshot a, DocumentSnapshot b) {
                            int dayCompare = Integer.compare(
                                    dayOrder(safe(a.getString("day"))),
                                    dayOrder(safe(b.getString("day")))
                            );
                            if (dayCompare != 0) return dayCompare;

                            int periodA = number(a.get("periodNumber"));
                            int periodB = number(b.get("periodNumber"));
                            if (periodA != periodB) return Integer.compare(periodA, periodB);

                            return safe(a.getString("startTime"))
                                    .compareToIgnoreCase(safe(b.getString("startTime")));
                        }
                    });

                    if (rows.isEmpty()) {
                        addMessage("No timetable found for the selected filter.");
                        return;
                    }

                    String lastDay = "";
                    for (DocumentSnapshot doc : rows) {
                        String day = safe(doc.getString("day"));
                        if (!day.equalsIgnoreCase(lastDay)) {
                            addDayHeader(day);
                            lastDay = day;
                        }
                        addTimetableRow(doc);
                    }
                })
                .addOnFailureListener(e -> {
                    timetableContainer.removeAllViews();
                    addMessage("Unable to load timetable.");
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void addDayHeader(String day) {
        TextView tv = new TextView(this);
        tv.setText(day.isEmpty() ? "Day" : day);
        tv.setTextSize(17);
        tv.setTextColor(0xFF42179B);
        tv.setGravity(Gravity.CENTER_VERTICAL);
        tv.setPadding(14, 12, 14, 8);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        timetableContainer.addView(tv);
    }

    private void addTimetableRow(DocumentSnapshot doc) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(16, 12, 16, 12);
        card.setBackgroundResource(R.drawable.admin_timetable_row);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(8, 5, 8, 5);
        card.setLayoutParams(params);

        String className = safe(doc.getString("className"));
        String division = safe(doc.getString("division"));
        String subject = safe(doc.getString("subject"));
        String teacher = safe(doc.getString("teacherName"));
        String start = safe(doc.getString("startTime"));
        String end = safe(doc.getString("endTime"));
        String period = safeValue(doc.get("periodNumber"));

        TextView title = text(
                (period.isEmpty() ? "Period" : "Period " + period)
                        + "   •   " + className
                        + (division.isEmpty() ? "" : " - " + division),
                15, 0xFF24105F, true
        );
        card.addView(title);

        card.addView(text(
                subject.isEmpty() ? "Subject not set" : subject,
                14, 0xFF333333, true
        ));

        String time = start;
        if (!end.isEmpty()) time += " - " + end;
        if (!time.trim().isEmpty()) {
            card.addView(text("Time: " + time, 12, 0xFF777777, false));
        }

        if (!teacher.isEmpty()) {
            card.addView(text("Teacher: " + teacher, 12, 0xFF777777, false));
        }

        timetableContainer.addView(card);
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(value);
        tv.setTextSize(size);
        tv.setTextColor(color);
        if (bold) tv.setTypeface(null, android.graphics.Typeface.BOLD);
        return tv;
    }

    private void addMessage(String message) {
        TextView tv = text(message, 14, 0xFF777777, false);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(16, 40, 16, 40);
        timetableContainer.addView(tv);
    }

    private String selected(Spinner spinner) {
        Object item = spinner.getSelectedItem();
        return item == null ? "" : String.valueOf(item);
    }

    private boolean same(String a, String b) {
        return normalize(a).equals(normalize(b));
    }

    private String normalize(String value) {
        return safe(value).replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }

    private int dayOrder(String day) {
        String d = normalize(day);
        if (d.equals("monday")) return 1;
        if (d.equals("tuesday")) return 2;
        if (d.equals("wednesday")) return 3;
        if (d.equals("thursday")) return 4;
        if (d.equals("friday")) return 5;
        if (d.equals("saturday")) return 6;
        return 99;
    }

    private int number(Object value) {
        if (value instanceof Number) return ((Number) value).intValue();
        try { return Integer.parseInt(safeValue(value)); }
        catch (Exception ignored) { return 999; }
    }

    private String safe(String value) { return value == null ? "" : value.trim(); }

    private String safeValue(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private void sortAfterFirst(List<String> list) {
        if (list.size() <= 2) return;
        String first = list.remove(0);
        Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
        list.add(0, first);
    }

    private abstract static class SimpleItemListener
            implements android.widget.AdapterView.OnItemSelectedListener {
        @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        public abstract void onItemSelected(int position);
        @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
            onItemSelected(position);
        }
    }
}
