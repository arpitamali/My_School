package com.example.myschool;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ParentSchoolEnquiryActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String studentId = "";
    private String schoolId = "";
    private String studentName = "";
    private String className = "";
    private String division = "";
    private String parentName = "Parent";
    private String parentPhone = "";
    private String parentEmail = "";

    private EditText etMessage;
    private Button btnSubmit;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_school_enquiry);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        studentId = safe(getIntent().getStringExtra("studentId"));
        if (studentId.isEmpty()) {
            studentId = getSharedPreferences("MySchoolPrefs", MODE_PRIVATE)
                    .getString("studentId", "");
        }

        findViewById(R.id.btnParentEnquiryBack).setOnClickListener(v -> finish());
        etMessage = findViewById(R.id.etParentEnquiryMessage);
        btnSubmit = findViewById(R.id.btnSubmitParentEnquiry);
        progressBar = findViewById(R.id.parentEnquiryProgressBar);

        btnSubmit.setOnClickListener(v -> submitEnquiry());
        loadStudent();
    }

    private void loadStudent() {
        if (studentId.isEmpty()) return;

        progressBar.setVisibility(View.VISIBLE);
        db.collection("students").document(studentId).get()
                .addOnSuccessListener(student -> {
                    progressBar.setVisibility(View.GONE);
                    if (!student.exists()) return;

                    schoolId = safe(student.getString("schoolId"));
                    studentName = buildName(student);
                    className = safe(student.getString("className"));
                    division = safe(student.getString("division"));
                    parentEmail = auth.getCurrentUser() == null ? "" : safe(auth.getCurrentUser().getEmail());

                    String fatherEmail = safe(student.getString("fatherEmail"));
                    String motherEmail = safe(student.getString("motherEmail"));

                    if (!parentEmail.isEmpty() && parentEmail.equalsIgnoreCase(fatherEmail)) {
                        parentName = safe(student.getString("fatherName"));
                        parentPhone = safe(student.getString("fatherMobile"));
                    } else if (!parentEmail.isEmpty() && parentEmail.equalsIgnoreCase(motherEmail)) {
                        parentName = safe(student.getString("motherName"));
                        parentPhone = safe(student.getString("motherMobile"));
                    } else {
                        parentName = safe(student.getString("guardianName"));
                        parentPhone = safe(student.getString("guardianMobile"));
                    }

                    if (parentName.isEmpty()) parentName = "Parent";

                    TextView tvInfo = findViewById(R.id.tvParentEnquiryStudentInfo);
                    tvInfo.setText("Student: " + studentName + " • Class " + className +
                            (division.isEmpty() ? "" : " - " + division));
                })
                .addOnFailureListener(e -> progressBar.setVisibility(View.GONE));
    }

    private void submitEnquiry() {
        String message = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            etMessage.setError("Enter your enquiry");
            etMessage.requestFocus();
            return;
        }

        if (schoolId.isEmpty()) {
            Toast.makeText(this, "School information not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String id = db.collection("enquiries").document().getId();
        Map<String, Object> data = new HashMap<>();
        data.put("enquiryId", id);
        data.put("schoolId", schoolId);
        data.put("parentId", auth.getCurrentUser() == null ? "" : auth.getCurrentUser().getUid());
        data.put("name", parentName);
        data.put("phone", parentPhone);
        data.put("email", parentEmail);
        data.put("studentName", studentName);
        data.put("studentId", studentId);
        data.put("className", className);
        data.put("division", division);
        data.put("message", message);
        data.put("status", "New");
        data.put("date", new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));
        data.put("createdAt", FieldValue.serverTimestamp());

        btnSubmit.setEnabled(false);
        db.collection("enquiries").document(id).set(data)
                .addOnSuccessListener(unused -> {
                    btnSubmit.setEnabled(true);
                    etMessage.setText("");
                    Toast.makeText(this, "Enquiry sent to school", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, "Failed to send enquiry: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private String buildName(com.google.firebase.firestore.DocumentSnapshot doc) {
        String first = safe(doc.getString("firstName"));
        String middle = safe(doc.getString("middleName"));
        String last = safe(doc.getString("lastName"));
        return (first + " " + middle + " " + last).trim().replaceAll("\\s+", " ");
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}

