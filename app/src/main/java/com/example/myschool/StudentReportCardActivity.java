package com.example.myschool;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.CheckBox;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.Timestamp;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StudentReportCardActivity extends AppCompatActivity {

    private static final int CREATE_PDF_REQUEST = 7201;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String studentId = "";
    private String schoolId = "";
    private String staffId = "";
    private String studentName = "Student";
    private String className = "";
    private String division = "";
    private String admissionNumber = "";
    private String rollNumber = "";

    private TextView tvName;
    private TextView tvClass;
    private TextView tvStudentInfo;
    private TextView tvAttendance;
    private TextView tvOverall;
    private TextView tvStrengths;
    private TextView tvImprovement;
    private TextView tvTeacherRemark;
    private TextView tvPromotion;
    private LinearLayout marksContainer;
    private MaterialButton btnEditEvaluation;

    private final List<MarkRow> reportMarks = new ArrayList<>();
    private String reportTitle = "Student Report Card";
    private Evaluation evaluation = new Evaluation();
    private ListenerRegistration profileListener;
    private ListenerRegistration marksListener;
    private ListenerRegistration attendanceListener;
    private ListenerRegistration evaluationListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_report_card);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews();
        readIntent();
        setupClicks();
        loadReport();
    }

    private void initViews() {
        tvName = findViewById(R.id.tvReportStudentName);
        tvClass = findViewById(R.id.tvReportStudentClass);
        tvStudentInfo = findViewById(R.id.tvReportStudentInfo);
        tvAttendance = findViewById(R.id.tvReportAttendance);
        tvOverall = findViewById(R.id.tvReportOverall);
        tvStrengths = findViewById(R.id.tvReportStrengths);
        tvImprovement = findViewById(R.id.tvReportImprovement);
        tvTeacherRemark = findViewById(R.id.tvReportTeacherRemark);
        tvPromotion = findViewById(R.id.tvReportPromotion);
        marksContainer = findViewById(R.id.reportMarksContainer);
        btnEditEvaluation = findViewById(R.id.btnEditEvaluation);
    }

    private void readIntent() {
        studentId = safe(getIntent().getStringExtra("studentId"));
        schoolId = safe(getIntent().getStringExtra("schoolId"));
        staffId = safe(getIntent().getStringExtra("staffId"));
        studentName = safe(getIntent().getStringExtra("studentName"));
        className = safe(getIntent().getStringExtra("className"));
        division = safe(getIntent().getStringExtra("division"));

        if (studentName.isEmpty()) studentName = "Student";
        tvName.setText(studentName);
        tvClass.setText(className + (division.isEmpty() ? "" : " - " + division));
    }

    private void setupClicks() {
        findViewById(R.id.btnReportBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnReportRefresh).setOnClickListener(v -> loadReport());
        btnEditEvaluation.setOnClickListener(v -> showEvaluationDialog());
        findViewById(R.id.btnSaveReportPdf).setOnClickListener(v -> createPdfFile());
    }

    private void loadReport() {
        loadStudentProfile();
        loadMarks();
        loadAttendance();
        loadEvaluation();
    }

    private void loadStudentProfile() {
        if (schoolId.isEmpty() || studentId.isEmpty()) return;

        if (profileListener != null) profileListener.remove();
        profileListener = db.collection("students").document(studentId)
                .addSnapshotListener((doc, error) -> {
                    if (error != null || doc == null) return;
                    if (!doc.exists()) return;
                    String first = safe(doc.getString("firstName"));
                    String middle = safe(doc.getString("middleName"));
                    String last = safe(doc.getString("lastName"));
                    String fetchedName = (first + " " + middle + " " + last).trim().replaceAll("\\s+", " ");
                    if (!fetchedName.isEmpty()) {
                        studentName = fetchedName;
                        tvName.setText(studentName);
                    }

                    String fetchedClass = safe(doc.getString("className"));
                    String fetchedDivision = safe(doc.getString("division"));
                    if (!fetchedClass.isEmpty()) className = fetchedClass;
                    if (!fetchedDivision.isEmpty()) division = fetchedDivision;

                    admissionNumber = safe(doc.getString("admissionNumber"));
                    rollNumber = safe(doc.getString("rollNumber"));

                    tvClass.setText(className + (division.isEmpty() ? "" : " - " + division));
                    tvStudentInfo.setText("Roll No: " + (rollNumber.isEmpty() ? "-" : rollNumber)
                            + "  •  Admission No: " + (admissionNumber.isEmpty() ? "-" : admissionNumber));
                });
    }

    private void loadMarks() {
        reportMarks.clear();
        marksContainer.removeAllViews();

        if (schoolId.isEmpty() || studentId.isEmpty()) return;

        if (marksListener != null) marksListener.remove();
        marksListener = db.collection("studentMarks")
                .whereEqualTo("schoolId", schoolId)
                .whereEqualTo("studentId", studentId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) {
                        toast("Unable to load marks: " + (error == null ? "" : error.getMessage()));
                        return;
                    }
                    reportMarks.clear();
                    marksContainer.removeAllViews();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String exam = normalizeExam(safe(doc.getString("examName")));
                        if (exam.isEmpty() || !isReportExam(exam)) continue;

                        Double max = doc.getDouble("maxMarks");
                        Double obtained = doc.getDouble("obtainedMarks");
                        if (max == null || obtained == null || max <= 0) continue;

                        double percent = obtained * 100.0 / max;
                        String grade = safe(doc.getString("grade"));
                        if (grade.isEmpty()) grade = grade(percent);

                        reportMarks.add(new MarkRow(exam, safe(doc.getString("subject")), max, obtained, percent, grade));
                    }

                    reportMarks.sort(Comparator
                            .comparing((MarkRow row) -> examOrder(row.exam))
                            .thenComparing(row -> row.subject, String.CASE_INSENSITIVE_ORDER));

                    renderReportMarks();
                });
    }

    private void renderReportMarks() {
        if (reportMarks.isEmpty()) {
            addEmptyText(marksContainer, "No marks entered for UT 1, Terminal Exam, UT 2 or Annual Exam.");
            tvOverall.setText("Overall: --");
            return;
        }

        LinkedHashMap<String, List<MarkRow>> grouped = new LinkedHashMap<>();
        for (String exam : reportExamOrder()) {
            grouped.put(exam, new ArrayList<>());
        }
        for (MarkRow row : reportMarks) {
            grouped.get(row.exam).add(row);
        }

        double totalMax = 0;
        double totalObtained = 0;

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
            title.setTextSize(16);
            title.setTypeface(null, Typeface.BOLD);
            root.addView(title);

            if (entry.getValue().isEmpty()) {
                TextView none = new TextView(this);
                none.setText("No marks entered");
                none.setTextColor(0xFF777B86);
                none.setTextSize(12);
                none.setPadding(0, 8, 0, 0);
                root.addView(none);
            }

            for (MarkRow row : entry.getValue()) {
                TextView line = new TextView(this);
                line.setText(String.format(Locale.getDefault(), "%s    %.0f/%.0f    Grade %s",
                        row.subject.isEmpty() ? "Subject" : row.subject,
                        row.obtained,
                        row.max,
                        row.grade));
                line.setTextColor(0xFF5D616D);
                line.setTextSize(12.5f);
                line.setPadding(0, 9, 0, 0);
                root.addView(line);

                totalMax += row.max;
                totalObtained += row.obtained;
            }

            card.addView(root);
            marksContainer.addView(card);
        }

        double overall = totalMax <= 0 ? 0 : totalObtained * 100.0 / totalMax;
        tvOverall.setText(String.format(Locale.getDefault(), "Overall: %.1f%%  •  %.0f/%.0f  •  Grade %s",
                overall, totalObtained, totalMax, grade(overall)));
    }

    private void loadAttendance() {
        if (schoolId.isEmpty() || studentId.isEmpty()) return;

        if (attendanceListener != null) attendanceListener.remove();
        attendanceListener = db.collection("studentAttendance")
                .whereEqualTo("schoolId", schoolId)
                .whereEqualTo("studentId", studentId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) {
                        tvAttendance.setText("Attendance Summary: --");
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
                        // Legacy Half Day records are intentionally not counted.
                    }

                    int workingDays = present + absent;
                    int percentage = workingDays == 0 ? 0 : Math.round((present * 100f) / workingDays);

                    tvAttendance.setText("Attendance Summary\n"
                            + "Present: " + present
                            + "   •   Absent: " + absent
                            + "   •   Holidays: " + holiday
                            + "   •   Attendance: " + percentage + "%");
                });
    }

    private void loadEvaluation() {
        if (schoolId.isEmpty() || studentId.isEmpty()) return;

        if (evaluationListener != null) evaluationListener.remove();
        evaluationListener = db.collection("studentRecords")
                .whereEqualTo("schoolId", schoolId)
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("type", "ReportEvaluation")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) {
                        renderEvaluation();
                        return;
                    }
                    Evaluation latest = null;
                    Timestamp latestTime = null;

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Evaluation item = new Evaluation();
                        item.strengths = safe(doc.getString("strengths"));
                        item.improvement = safe(doc.getString("improvement"));
                        item.teacherRemark = safe(doc.getString("teacherRemark"));
                        item.nextClass = safe(doc.getString("nextClass"));
                        item.promotionStatus = safe(doc.getString("promotionStatus"));

                        Timestamp ts = doc.getTimestamp("updatedAt");
                        if (latest == null || (ts != null && (latestTime == null || ts.compareTo(latestTime) > 0))) {
                            latest = item;
                            latestTime = ts;
                        }
                    }

                    evaluation = latest == null ? new Evaluation() : latest;
                    renderEvaluation();
                });
    }

    @Override
    protected void onDestroy() {
        if (profileListener != null) profileListener.remove();
        if (marksListener != null) marksListener.remove();
        if (attendanceListener != null) attendanceListener.remove();
        if (evaluationListener != null) evaluationListener.remove();
        profileListener = null;
        marksListener = null;
        attendanceListener = null;
        evaluationListener = null;
        super.onDestroy();
    }

    private void renderEvaluation() {
        tvStrengths.setText("Strengths\n" +
                (evaluation.strengths.isEmpty() ? "Not added yet." : evaluation.strengths));

        tvImprovement.setText("Areas for Improvement\n" +
                (evaluation.improvement.isEmpty() ? "Not added yet." : evaluation.improvement));

        tvTeacherRemark.setText("Teacher's Overall Remark\n" +
                (evaluation.teacherRemark.isEmpty() ? "Not added yet." : evaluation.teacherRemark));

        String promotion = evaluation.promotionStatus.isEmpty() ? "Not declared yet" : evaluation.promotionStatus;
        if (!evaluation.nextClass.isEmpty()) promotion += " • Next Class: " + evaluation.nextClass;
        tvPromotion.setText("Declaration: " + promotion);
    }

    private void showEvaluationDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(8);
        box.setPadding(dp(24), pad, dp(24), 0);

        EditText strengths = createMultiLine("What the student does well / strengths", evaluation.strengths);
        EditText improvement = createMultiLine("What needs improvement / mistakes / areas to work on", evaluation.improvement);
        EditText remark = createMultiLine("Teacher's overall remark", evaluation.teacherRemark);
        EditText nextClass = createSingleLine("Next Class (e.g. Class 9)", evaluation.nextClass);

        CheckBox promote = new CheckBox(this);
        promote.setText("Declare student promoted to next class");
        promote.setChecked(!evaluation.promotionStatus.equalsIgnoreCase("Not Promoted"));
        if (evaluation.promotionStatus.isEmpty()) promote.setChecked(true);

        box.addView(strengths);
        box.addView(improvement);
        box.addView(remark);
        box.addView(nextClass);
        box.addView(promote);

        new AlertDialog.Builder(this)
                .setTitle("Teacher Evaluation")
                .setView(box)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {
                    evaluation.strengths = safe(strengths.getText().toString());
                    evaluation.improvement = safe(improvement.getText().toString());
                    evaluation.teacherRemark = safe(remark.getText().toString());
                    evaluation.nextClass = safe(nextClass.getText().toString());
                    evaluation.promotionStatus = promote.isChecked() ? "Promoted to Next Class" : "Not Promoted";
                    saveEvaluation();
                })
                .show();
    }

    private EditText createMultiLine(String hint, String value) {
        EditText field = new EditText(this);
        field.setHint(hint);
        field.setText(value);
        field.setMinLines(3);
        field.setGravity(Gravity.TOP | Gravity.START);
        field.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        field.setPadding(dp(12), dp(10), dp(12), dp(10));
        return field;
    }

    private EditText createSingleLine(String hint, String value) {
        EditText field = new EditText(this);
        field.setHint(hint);
        field.setText(value);
        field.setSingleLine(true);
        field.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        return field;
    }

    private void saveEvaluation() {
        if (schoolId.isEmpty() || studentId.isEmpty()) {
            toast("Student or school information is missing.");
            return;
        }

        Map<String, Object> data = new java.util.HashMap<>();
        data.put("schoolId", schoolId);
        data.put("studentId", studentId);
        data.put("studentName", studentName);
        data.put("className", className);
        data.put("division", division);
        data.put("type", "ReportEvaluation");
        data.put("strengths", evaluation.strengths);
        data.put("improvement", evaluation.improvement);
        data.put("teacherRemark", evaluation.teacherRemark);
        data.put("nextClass", evaluation.nextClass);
        data.put("promotionStatus", evaluation.promotionStatus);
        data.put("teacherId", staffId);
        data.put("teacherUid", auth.getCurrentUser() == null ? "" : auth.getCurrentUser().getUid());
        data.put("updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        String documentId = "reportEvaluation_" + schoolId + "_" + studentId;
        db.collection("studentRecords").document(documentId).set(data)
                .addOnSuccessListener(v -> {
                    toast("Teacher evaluation saved.");
                    renderEvaluation();
                })
                .addOnFailureListener(e -> toast("Unable to save evaluation: " + e.getMessage()));
    }

    private void createPdfFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, (studentName + "_Report_Card").replaceAll("[^a-zA-Z0-9._ -]", "_") + ".pdf");
        startActivityForResult(intent, CREATE_PDF_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != CREATE_PDF_REQUEST || resultCode != RESULT_OK || data == null || data.getData() == null) return;

        try {
            PdfDocument pdf = buildPdf();
            Uri uri = data.getData();
            try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                if (out == null) throw new IllegalStateException("Output stream unavailable");
                pdf.writeTo(out);
            }
            pdf.close();
            toast("Report card PDF saved successfully.");
        } catch (Exception e) {
            toast("Unable to create PDF: " + e.getMessage());
        }
    }

    private PdfDocument buildPdf() {
        PdfDocument pdf = new PdfDocument();
        List<String> lines = new ArrayList<>();

        lines.add("MY SCHOOL");
        lines.add("STUDENT REPORT CARD");
        lines.add("");
        lines.add("Student: " + studentName);
        lines.add("Class: " + className + (division.isEmpty() ? "" : " - " + division));
        lines.add("Roll No: " + (rollNumber.isEmpty() ? "-" : rollNumber));
        lines.add("Admission No: " + (admissionNumber.isEmpty() ? "-" : admissionNumber));
        lines.add("");
        lines.add("ACADEMIC PERFORMANCE");

        Map<String, List<MarkRow>> grouped = new LinkedHashMap<>();
        for (String exam : reportExamOrder()) grouped.put(exam, new ArrayList<>());
        for (MarkRow row : reportMarks) grouped.get(row.exam).add(row);

        for (Map.Entry<String, List<MarkRow>> entry : grouped.entrySet()) {
            lines.add(entry.getKey());
            if (entry.getValue().isEmpty()) {
                lines.add("  No marks entered");
            } else {
                for (MarkRow row : entry.getValue()) {
                    lines.add(String.format(Locale.getDefault(), "  %s: %.0f/%.0f | Grade %s",
                            row.subject.isEmpty() ? "Subject" : row.subject, row.obtained, row.max, row.grade));
                }
            }
        }

        lines.add("");
        lines.add(tvOverall.getText().toString());
        lines.add("");
        lines.add(tvAttendance.getText().toString().replace("\n", " | "));
        lines.add("");
        lines.add("TEACHER EVALUATION");
        lines.add("Strengths:");
        addWrappedLines(lines, evaluation.strengths.isEmpty() ? "Not added yet." : evaluation.strengths, "  ");
        lines.add("Areas for Improvement:");
        addWrappedLines(lines, evaluation.improvement.isEmpty() ? "Not added yet." : evaluation.improvement, "  ");
        lines.add("Teacher's Overall Remark:");
        addWrappedLines(lines, evaluation.teacherRemark.isEmpty() ? "Not added yet." : evaluation.teacherRemark, "  ");
        lines.add("Declaration: " + (evaluation.promotionStatus.isEmpty() ? "Not declared yet" : evaluation.promotionStatus));
        if (!evaluation.nextClass.isEmpty()) lines.add("Next Class: " + evaluation.nextClass);
        lines.add("");
        lines.add("SIGNATURES");
        lines.add("");
        lines.add("Parent / Guardian: ______________________________");
        lines.add("");
        lines.add("Class Teacher: ___________________________________");
        lines.add("");
        lines.add("Principal: ________________________________________");

        int pageNumber = 1;
        int lineIndex = 0;
        while (lineIndex < lines.size()) {
            PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
            PdfDocument.Page page = pdf.startPage(info);
            android.graphics.Canvas canvas = page.getCanvas();
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(android.graphics.Color.BLACK);

            int y = 44;
            while (lineIndex < lines.size() && y < 800) {
                String line = lines.get(lineIndex);
                if (lineIndex <= 1 || line.equals("ACADEMIC PERFORMANCE") || line.equals("TEACHER EVALUATION") || line.equals("SIGNATURES")) {
                    paint.setTypeface(Typeface.DEFAULT_BOLD);
                } else {
                    paint.setTypeface(Typeface.DEFAULT);
                }
                paint.setTextSize(lineIndex == 0 ? 20 : (lineIndex == 1 ? 16 : 11));
                canvas.drawText(line, 40, y, paint);
                y += 18;
                lineIndex++;
            }
            pdf.finishPage(page);
            pageNumber++;
        }

        return pdf;
    }

    private void addWrappedLines(List<String> lines, String text, String prefix) {
        String value = text == null ? "" : text.trim();
        if (value.isEmpty()) {
            lines.add(prefix);
            return;
        }
        String[] words = value.split("\\s+");
        StringBuilder current = new StringBuilder(prefix);
        for (String word : words) {
            if (current.length() + word.length() + 1 > 78) {
                lines.add(current.toString());
                current = new StringBuilder(prefix);
            }
            if (current.length() > prefix.length()) current.append(' ');
            current.append(word);
        }
        if (current.length() > 0) lines.add(current.toString());
    }

    private List<String> reportExamOrder() {
        List<String> list = new ArrayList<>();
        list.add("UT 1");
        list.add("Terminal Exam");
        list.add("UT 2");
        list.add("Annual Exam");
        return list;
    }

    private boolean isReportExam(String exam) {
        return "UT 1".equals(exam) || "Terminal Exam".equals(exam)
                || "UT 2".equals(exam) || "Annual Exam".equals(exam);
    }

    private int examOrder(String exam) {
        if ("UT 1".equals(exam)) return 1;
        if ("Terminal Exam".equals(exam)) return 2;
        if ("UT 2".equals(exam)) return 3;
        if ("Annual Exam".equals(exam)) return 4;
        return 10;
    }

    private String normalizeExam(String exam) {
        String value = safe(exam).toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", "")
                .replace("_", "")
                .replace("-", "");

        if (value.equals("ut1") || value.equals("unittest1")) return "UT 1";
        if (value.contains("terminal")) return "Terminal Exam";
        if (value.equals("ut2") || value.equals("unittest2")) return "UT 2";
        if (value.contains("annual") || value.contains("finalexam")) return "Annual Exam";
        return "";
    }

    private String grade(double percent) {
        if (percent >= 90) return "A+";
        if (percent >= 80) return "A";
        if (percent >= 70) return "B+";
        if (percent >= 60) return "B";
        if (percent >= 50) return "C";
        if (percent >= 35) return "D";
        return "F";
    }

    private void addEmptyText(LinearLayout parent, String text) {
        TextView empty = new TextView(this);
        empty.setText(text);
        empty.setTextColor(0xFF777B86);
        empty.setPadding(12, 18, 12, 18);
        parent.addView(empty);
    }

    private String safe(String value) { return value == null ? "" : value.trim(); }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
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

    private static class Evaluation {
        String strengths = "";
        String improvement = "";
        String teacherRemark = "";
        String nextClass = "";
        String promotionStatus = "";
    }
}
