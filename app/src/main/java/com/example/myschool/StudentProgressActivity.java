package com.example.myschool;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class StudentProgressActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String schoolId = "";
    private String studentId = "";
    private String staffId = "";
    private String studentName = "Student";
    private String className = "";
    private String division = "";

    private TextView tvStudent;
    private TextView tvAttendance;
    private TextView tvOverall;
    private TextView tvPerformance;
    private TextView tvDeclaration;
    private TextView tvStrength;
    private TextView tvImprovement;
    private LinearLayout examsContainer;
    private LinearLayout subjectsContainer;
    private LinearLayout recordsContainer;

    private final List<MarkRow> allMarks = new ArrayList<>();
    private ListenerRegistration marksListener;
    private ListenerRegistration attendanceListener;
    private ListenerRegistration recordsListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_progress);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews();
        readIntent();
        setupClicks();
        loadProgress();
    }

    private void initViews() {
        tvStudent = findViewById(R.id.tvProgressStudent);
        tvAttendance = findViewById(R.id.tvProgressAttendance);
        tvOverall = findViewById(R.id.tvProgressOverall);
        tvPerformance = findViewById(R.id.tvProgressPerformance);
        tvDeclaration = findViewById(R.id.tvProgressDeclaration);
        tvStrength = findViewById(R.id.tvProgressStrength);
        tvImprovement = findViewById(R.id.tvProgressImprovement);
        examsContainer = findViewById(R.id.progressExamsContainer);
        subjectsContainer = findViewById(R.id.progressSubjectsContainer);
        recordsContainer = findViewById(R.id.progressRecordsContainer);
    }

    private void readIntent() {
        schoolId = safe(getIntent().getStringExtra("schoolId"));
        studentId = safe(getIntent().getStringExtra("studentId"));
        staffId = safe(getIntent().getStringExtra("staffId"));
        studentName = safe(getIntent().getStringExtra("studentName"));
        className = safe(getIntent().getStringExtra("className"));
        division = safe(getIntent().getStringExtra("division"));

        if (studentName.isEmpty()) studentName = "Student";
        tvStudent.setText(studentName + (className.isEmpty() ? "" : " • " + className + " " + division));
    }

    private void setupClicks() {
        findViewById(R.id.btnProgressBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddRemark).setOnClickListener(v -> showRecordDialog("Remark"));
        findViewById(R.id.btnAddAchievement).setOnClickListener(v -> showRecordDialog("Achievement"));
        findViewById(R.id.btnRefreshProgress).setOnClickListener(v -> loadProgress());
    }

    private void loadProgress() {
        loadMarksAndAttendance();
        loadRecords();
    }

    private void loadMarksAndAttendance() {
        allMarks.clear();
        examsContainer.removeAllViews();
        subjectsContainer.removeAllViews();

        if (schoolId.isEmpty() || studentId.isEmpty()) return;

        if (marksListener != null) marksListener.remove();
        marksListener = db.collection("studentMarks")
                .whereEqualTo("schoolId", schoolId)
                .whereEqualTo("studentId", studentId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) {
                        tvOverall.setText("Overall --");
                        tvPerformance.setText("Unable to load marks");
                        tvDeclaration.setText("Progress declaration: --");
                        return;
                    }
                    allMarks.clear();
                    examsContainer.removeAllViews();
                    subjectsContainer.removeAllViews();
                    double totalObtained = 0;
                    double totalMax = 0;

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Double max = doc.getDouble("maxMarks");
                        Double obtained = doc.getDouble("obtainedMarks");
                        if (max == null || obtained == null || max <= 0) continue;

                        String exam = safe(doc.getString("examName"));
                        String subject = safe(doc.getString("subject"));
                        double percent = obtained * 100.0 / max;
                        String grade = safe(doc.getString("grade"));
                        if (grade.isEmpty()) grade = grade(percent);

                        allMarks.add(new MarkRow(exam, subject, max, obtained, percent, grade));
                        totalObtained += obtained;
                        totalMax += max;
                    }

                    allMarks.sort(Comparator
                            .comparing((MarkRow row) -> examOrder(row.exam))
                            .thenComparing(row -> row.exam, String.CASE_INSENSITIVE_ORDER)
                            .thenComparing(row -> row.subject, String.CASE_INSENSITIVE_ORDER));

                    renderAllExamMarks();
                    renderSubjectSummary();

                    double overall = totalMax <= 0 ? 0 : totalObtained * 100.0 / totalMax;
                    tvOverall.setText(String.format(Locale.getDefault(), "Overall %.1f%%", overall));
                    tvPerformance.setText(performanceText(overall));
                    updateAcademicDeclaration(overall);
                });

        if (attendanceListener != null) attendanceListener.remove();
        attendanceListener = db.collection("studentAttendance")
                .whereEqualTo("schoolId", schoolId)
                .whereEqualTo("studentId", studentId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) {
                        tvAttendance.setText("Attendance --");
                        return;
                    }
                    int present = 0;
                    int absent = 0;
                    int holiday = 0;

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String status = safe(doc.getString("status"));
                        if (status.equalsIgnoreCase("Present")) present++;
                        else if (status.equalsIgnoreCase("Absent")) absent++;
                        else if (status.equalsIgnoreCase("Holiday")) holiday++;
                        // Legacy Half Day is intentionally ignored/cancelled.
                    }

                    int applicable = present + absent;
                    int percent = applicable == 0 ? 0 : Math.round((present * 100f) / applicable);
                    tvAttendance.setText("Attendance " + percent + "%  •  Present " + present
                            + "  •  Absent " + absent + "  •  Holiday " + holiday);
                });
    }

    private void renderAllExamMarks() {
        if (allMarks.isEmpty()) {
            addEmptyText(examsContainer, "No test/exam marks available yet.");
            return;
        }

        LinkedHashMap<String, List<MarkRow>> grouped = new LinkedHashMap<>();
        for (MarkRow row : allMarks) {
            String label = displayExam(row.exam);
            grouped.computeIfAbsent(label, k -> new ArrayList<>()).add(row);
        }

        for (Map.Entry<String, List<MarkRow>> entry : grouped.entrySet()) {
            MaterialCardView card = new MaterialCardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2);
            cardParams.setMargins(0, 0, 0, 10);
            card.setLayoutParams(cardParams);
            card.setRadius(16);

            LinearLayout root = new LinearLayout(this);
            root.setOrientation(LinearLayout.VERTICAL);
            root.setPadding(14, 12, 14, 12);

            TextView title = new TextView(this);
            title.setText(entry.getKey());
            title.setTextColor(0xFF25252B);
            title.setTextSize(15);
            title.setTypeface(null, 1);
            root.addView(title);

            for (MarkRow row : entry.getValue()) {
                TextView line = new TextView(this);
                line.setText(String.format(Locale.getDefault(), "%s    %.0f/%.0f    %.1f%%    Grade %s",
                        row.subject, row.obtained, row.max, row.percent, row.grade));
                line.setTextColor(0xFF5D616D);
                line.setTextSize(12.5f);
                line.setPadding(0, 8, 0, 0);
                root.addView(line);
            }

            card.addView(root);
            examsContainer.addView(card);
        }
    }

    private void renderSubjectSummary() {
        if (allMarks.isEmpty()) {
            addEmptyText(subjectsContainer, "No subject-wise progress available.");
            tvStrength.setText("Strengths: --");
            tvImprovement.setText("Areas for improvement: --");
            return;
        }

        Map<String, List<Double>> subjectValues = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (MarkRow row : allMarks) {
            subjectValues.computeIfAbsent(row.subject.isEmpty() ? "Other" : row.subject, k -> new ArrayList<>())
                    .add(row.percent);
        }

        String bestSubject = "";
        String weakSubject = "";
        double best = -1;
        double weak = 101;

        for (Map.Entry<String, List<Double>> entry : subjectValues.entrySet()) {
            double avg = 0;
            for (double value : entry.getValue()) avg += value;
            avg /= entry.getValue().size();
            addSubjectCard(entry.getKey(), avg);

            if (avg > best) {
                best = avg;
                bestSubject = entry.getKey();
            }
            if (avg < weak) {
                weak = avg;
                weakSubject = entry.getKey();
            }
        }

        tvStrength.setText("Strengths: " + bestSubject + (best >= 0 ? " (" + format1(best) + "%)" : ""));
        tvImprovement.setText("Areas for improvement: " + weakSubject + (weak <= 100 ? " (" + format1(weak) + "%)" : ""));
    }

    private void addSubjectCard(String subject, double average) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, 8);
        card.setLayoutParams(params);
        card.setRadius(16);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setGravity(Gravity.CENTER_VERTICAL);
        root.setPadding(14, 12, 14, 12);

        TextView name = new TextView(this);
        name.setText(subject);
        name.setTextColor(0xFF25252B);
        name.setTextSize(14);
        name.setTypeface(null, 1);
        root.addView(name, new LinearLayout.LayoutParams(0, -2, 1f));

        TextView avg = new TextView(this);
        avg.setText(String.format(Locale.getDefault(), "%.1f%%  •  Grade %s", average, grade(average)));
        avg.setTextColor(0xFF5427D8);
        avg.setTextSize(14);
        avg.setTypeface(null, 1);
        root.addView(avg);

        card.addView(root);
        subjectsContainer.addView(card);
    }

    private void updateAcademicDeclaration(double overall) {
        if (overall <= 0) {
            tvDeclaration.setText("Progress declaration: No academic data available yet.");
        } else if (overall >= 85) {
            tvDeclaration.setText("Progress declaration: The student is demonstrating strong and consistent academic progress.");
        } else if (overall >= 70) {
            tvDeclaration.setText("Progress declaration: The student is making good academic progress and should continue regular practice.");
        } else if (overall >= 50) {
            tvDeclaration.setText("Progress declaration: The student is progressing, with scope for improvement through regular revision and practice.");
        } else {
            tvDeclaration.setText("Progress declaration: The student needs focused academic support, regular practice and close follow-up.");
        }
    }

    private void loadRecords() {
        recordsContainer.removeAllViews();
        if (schoolId.isEmpty() || studentId.isEmpty()) return;

        if (recordsListener != null) recordsListener.remove();
        recordsListener = db.collection("studentRecords")
                .whereEqualTo("schoolId", schoolId)
                .whereEqualTo("studentId", studentId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) {
                        toast("Unable to load remarks.");
                        return;
                    }
                    recordsContainer.removeAllViews();
                    if (snapshot.isEmpty()) {
                        addEmptyText(recordsContainer, "No remarks or achievements added yet.");
                        return;
                    }

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String type = safe(doc.getString("type"));
                        if ("ReportEvaluation".equalsIgnoreCase(type)) continue;
                        String title = safe(doc.getString("title"));
                        String description = safe(doc.getString("description"));
                        addRecordCard(doc.getId(), type, title, description);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        if (marksListener != null) marksListener.remove();
        if (attendanceListener != null) attendanceListener.remove();
        if (recordsListener != null) recordsListener.remove();
        marksListener = null;
        attendanceListener = null;
        recordsListener = null;
        super.onDestroy();
    }

    private void addRecordCard(String docId, String type, String title, String description) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, 8);
        card.setLayoutParams(params);
        card.setRadius(16);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(14, 12, 14, 12);

        TextView typeView = new TextView(this);
        typeView.setText(type.isEmpty() ? "Record" : type);
        typeView.setTextColor(0xFF5427D8);
        typeView.setTextSize(11);
        typeView.setTypeface(null, 1);
        root.addView(typeView);

        TextView titleView = new TextView(this);
        titleView.setText(title.isEmpty() ? "Untitled" : title);
        titleView.setTextColor(0xFF25252B);
        titleView.setTextSize(14);
        titleView.setTypeface(null, 1);
        titleView.setPadding(0, 3, 0, 2);
        root.addView(titleView);

        if (!description.isEmpty()) {
            TextView desc = new TextView(this);
            desc.setText(description);
            desc.setTextColor(0xFF6B6F7B);
            desc.setTextSize(12);
            root.addView(desc);
        }

        MaterialButton delete = new MaterialButton(this);
        delete.setText("Delete");
        delete.setAllCaps(false);
        delete.setOnClickListener(v -> db.collection("studentRecords").document(docId).delete()
                .addOnSuccessListener(x -> loadRecords())
                .addOnFailureListener(e -> toast("Delete failed.")));
        root.addView(delete);

        card.addView(root);
        recordsContainer.addView(card);
    }

    private void showRecordDialog(String type) {
        LinearLayout box = new LinearLayout(this);
        box.setPadding(30, 10, 30, 0);
        box.setOrientation(LinearLayout.VERTICAL);

        EditText title = new EditText(this);
        title.setHint(type.equals("Achievement") ? "Achievement title" : "Remark title");
        box.addView(title);

        EditText description = new EditText(this);
        description.setHint(type.equals("Achievement") ? "Achievement details" : "Teacher remark");
        description.setMinLines(3);
        box.addView(description);

        new AlertDialog.Builder(this)
                .setTitle("Add " + type)
                .setView(box)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {
                    String titleValue = safe(title.getText().toString());
                    String descriptionValue = safe(description.getText().toString());
                    if (titleValue.isEmpty()) {
                        toast("Title is required.");
                        return;
                    }

                    Map<String, Object> data = new HashMap<>();
                    data.put("schoolId", schoolId);
                    data.put("studentId", studentId);
                    data.put("studentName", studentName);
                    data.put("className", className);
                    data.put("division", division);
                    data.put("type", type);
                    data.put("title", titleValue);
                    data.put("description", descriptionValue);
                    data.put("teacherId", staffId);
                    data.put("teacherUid", auth.getCurrentUser() == null ? "" : auth.getCurrentUser().getUid());
                    data.put("createdAt", FieldValue.serverTimestamp());

                    db.collection("studentRecords").add(data)
                            .addOnSuccessListener(v -> {
                                toast(type + " added.");
                                loadRecords();
                            })
                            .addOnFailureListener(e -> toast("Unable to save " + type.toLowerCase(Locale.ROOT) + "."));
                })
                .show();
    }

    private void addEmptyText(LinearLayout container, String text) {
        TextView empty = new TextView(this);
        empty.setText(text);
        empty.setTextColor(0xFF777B86);
        empty.setPadding(12, 18, 12, 18);
        container.addView(empty);
    }

    private int examOrder(String exam) {
        String normalized = normalizeExam(exam);
        if ("UT 1".equals(normalized)) return 1;
        if ("Terminal Exam".equals(normalized)) return 2;
        if ("UT 2".equals(normalized)) return 3;
        if ("Annual Exam".equals(normalized)) return 4;
        return 10;
    }

    private String displayExam(String exam) {
        String normalized = normalizeExam(exam);
        return normalized.isEmpty() ? (exam.isEmpty() ? "Test / Exam" : exam) : normalized;
    }

    private String normalizeExam(String exam) {
        String value = safe(exam).toLowerCase(Locale.ROOT).replaceAll("\\s+", "").replace("_", "").replace("-", "");
        if (value.equals("ut1") || value.equals("unittest1")) return "UT 1";
        if (value.contains("terminal")) return "Terminal Exam";
        if (value.equals("ut2") || value.equals("unittest2")) return "UT 2";
        if (value.contains("annual") || value.contains("finalexam")) return "Annual Exam";
        return safe(exam);
    }

    private String performanceText(double percent) {
        if (percent >= 85) return "Excellent progress";
        if (percent >= 70) return "Good progress";
        if (percent >= 50) return "Needs regular practice";
        if (percent > 0) return "Needs focused academic support";
        return "No academic data yet";
    }

    private String grade(double p) {
        if (p >= 90) return "A+";
        if (p >= 80) return "A";
        if (p >= 70) return "B+";
        if (p >= 60) return "B";
        if (p >= 50) return "C";
        if (p >= 35) return "D";
        return "F";
    }

    private String format1(double value) { return String.format(Locale.getDefault(), "%.1f", value); }
    private String safe(String value) { return value == null ? "" : value.trim(); }
    private void toast(String message) { Toast.makeText(this, message, Toast.LENGTH_LONG).show(); }

    private static class MarkRow {
        final String exam;
        final String subject;
        final double max;
        final double obtained;
        final double percent;
        final String grade;

        MarkRow(String exam, String subject, double max, double obtained, double percent, String grade) {
            this.exam = exam;
            this.subject = subject;
            this.max = max;
            this.obtained = obtained;
            this.percent = percent;
            this.grade = grade;
        }
    }
}
