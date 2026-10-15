package com.example.myschool;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TeacherMarksActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private EditText etExam;
    private EditText etSubject;
    private EditText etMaxMarks;
    private EditText etObtainedMarks;
    private TextView tvStudent;
    private TextView tvGrade;
    private LinearLayout marksListContainer;
    private MaterialButton btnSave;

    private String schoolId = "";
    private String studentId = "";
    private String staffId = "";
    private String className = "";
    private String division = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_marks);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews();
        readIntent();
        setupClicks();
        loadExistingMarks();
    }

    private void initViews() {
        tvStudent = findViewById(R.id.tvMarksStudent);
        etExam = findViewById(R.id.etMarksExam);
        etSubject = findViewById(R.id.etMarksSubject);
        etMaxMarks = findViewById(R.id.etMarksMax);
        etObtainedMarks = findViewById(R.id.etMarksObtained);
        tvGrade = findViewById(R.id.tvMarksGrade);
        marksListContainer = findViewById(R.id.marksListContainer);
        btnSave = findViewById(R.id.btnSaveMarks);
    }

    private void readIntent() {
        schoolId = safe(getIntent().getStringExtra("schoolId"));
        studentId = safe(getIntent().getStringExtra("studentId"));
        staffId = safe(getIntent().getStringExtra("staffId"));
        className = safe(getIntent().getStringExtra("className"));
        division = safe(getIntent().getStringExtra("division"));
        String studentName = safe(getIntent().getStringExtra("studentName"));
        tvStudent.setText(studentName.isEmpty() ? "Student" : studentName);
    }

    private void setupClicks() {
        findViewById(R.id.btnMarksBack).setOnClickListener(v -> finish());
        etObtainedMarks.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) updateGradePreview();
        });
        etMaxMarks.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) updateGradePreview();
        });
        btnSave.setOnClickListener(v -> saveMarks());
    }

    private void updateGradePreview() {
        Double max = number(etMaxMarks.getText().toString());
        Double obtained = number(etObtainedMarks.getText().toString());
        if (max == null || obtained == null || max <= 0) {
            tvGrade.setText("Grade: --");
            return;
        }
        if (obtained < 0 || obtained > max) {
            tvGrade.setText("Grade: Invalid marks");
            return;
        }
        tvGrade.setText("Grade: " + grade(obtained * 100.0 / max));
    }

    private void saveMarks() {
        String exam = safe(etExam.getText().toString());
        String subject = safe(etSubject.getText().toString());
        Double max = number(etMaxMarks.getText().toString());
        Double obtained = number(etObtainedMarks.getText().toString());

        if (schoolId.isEmpty() || studentId.isEmpty()) {
            toast("Student or school information is missing.");
            return;
        }
        if (exam.isEmpty()) {
            etExam.setError("Enter exam name");
            etExam.requestFocus();
            return;
        }
        if (subject.isEmpty()) {
            etSubject.setError("Enter subject");
            etSubject.requestFocus();
            return;
        }
        if (max == null || max <= 0) {
            etMaxMarks.setError("Enter valid total marks");
            etMaxMarks.requestFocus();
            return;
        }
        if (obtained == null || obtained < 0 || obtained > max) {
            etObtainedMarks.setError("Enter valid obtained marks");
            etObtainedMarks.requestFocus();
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Saving...");

        String normalizedExam = exam.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_");
        String normalizedSubject = subject.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_");
        String docId = schoolId + "_" + studentId + "_" + normalizedExam + "_" + normalizedSubject;

        Map<String, Object> data = new HashMap<>();
        data.put("schoolId", schoolId);
        data.put("studentId", studentId);
        data.put("studentName", safe(getIntent().getStringExtra("studentName")));
        data.put("className", className);
        data.put("division", division);
        data.put("examName", exam);
        data.put("subject", subject);
        data.put("maxMarks", max);
        data.put("obtainedMarks", obtained);
        data.put("percentage", obtained * 100.0 / max);
        data.put("grade", grade(obtained * 100.0 / max));
        data.put("teacherId", staffId);
        data.put("teacherUid", auth.getCurrentUser() == null ? "" : auth.getCurrentUser().getUid());
        data.put("updatedAt", FieldValue.serverTimestamp());

        db.collection("studentMarks").document(docId).set(data)
                .addOnSuccessListener(v -> {
                    btnSave.setEnabled(true);
                    btnSave.setText("Save Marks");
                    toast("Marks saved successfully.");
                    etSubject.setText("");
                    etObtainedMarks.setText("");
                    tvGrade.setText("Grade: --");
                    loadExistingMarks();
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    btnSave.setText("Save Marks");
                    toast("Failed to save marks: " + e.getMessage());
                });
    }

    private void loadExistingMarks() {
        marksListContainer.removeAllViews();
        if (schoolId.isEmpty() || studentId.isEmpty()) return;

        db.collection("studentMarks")
                .whereEqualTo("schoolId", schoolId)
                .whereEqualTo("studentId", studentId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        TextView empty = new TextView(this);
                        empty.setText("No marks added yet.");
                        empty.setTextColor(0xFF777B86);
                        empty.setGravity(Gravity.CENTER);
                        empty.setPadding(12, 24, 12, 24);
                        marksListContainer.addView(empty);
                        return;
                    }

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        addMarkCard(doc);
                    }
                })
                .addOnFailureListener(e -> toast("Unable to load marks."));
    }

    private void addMarkCard(DocumentSnapshot doc) {
        String exam = safe(doc.getString("examName"));
        String subject = safe(doc.getString("subject"));
        double max = numberOrZero(doc.getDouble("maxMarks"));
        double obtained = numberOrZero(doc.getDouble("obtainedMarks"));
        String grade = safe(doc.getString("grade"));

        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, 8);
        card.setLayoutParams(params);
        card.setRadius(16);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(14, 12, 14, 12);

        TextView title = new TextView(this);
        title.setText(exam + " • " + subject);
        title.setTextColor(0xFF25252B);
        title.setTextSize(15);
        title.setTypeface(null, Typeface.BOLD);
        root.addView(title);

        TextView detail = new TextView(this);
        detail.setText(String.format(Locale.getDefault(), "%.0f / %.0f  •  %.1f%%  •  Grade %s", obtained, max,
                max <= 0 ? 0 : obtained * 100 / max, grade));
        detail.setTextColor(0xFF6B6F7B);
        detail.setTextSize(12);
        root.addView(detail);

        MaterialButton delete = new MaterialButton(this);
        delete.setText("Delete");
        delete.setAllCaps(false);
        delete.setOnClickListener(v -> new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Marks")
                .setMessage("Remove " + subject + " marks from " + exam + "?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (d, w) -> db.collection("studentMarks").document(doc.getId()).delete()
                        .addOnSuccessListener(x -> {
                            toast("Marks deleted.");
                            loadExistingMarks();
                        })
                        .addOnFailureListener(e -> toast("Delete failed.")))
                .show());
        root.addView(delete);

        card.addView(root);
        marksListContainer.addView(card);
    }

    private Double number(String value) {
        try { return Double.parseDouble(value.trim()); } catch (Exception e) { return null; }
    }

    private double numberOrZero(Double value) { return value == null ? 0 : value; }

    private String grade(double percent) {
        if (percent >= 90) return "A+";
        if (percent >= 80) return "A";
        if (percent >= 70) return "B+";
        if (percent >= 60) return "B";
        if (percent >= 50) return "C";
        if (percent >= 35) return "D";
        return "F";
    }

    private String safe(String value) { return value == null ? "" : value.trim(); }
    private void toast(String message) { Toast.makeText(this, message, Toast.LENGTH_LONG).show(); }
}
