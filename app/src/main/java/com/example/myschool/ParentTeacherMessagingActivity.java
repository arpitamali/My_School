package com.example.myschool;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ParentTeacherMessagingActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private Spinner spinnerTeacher;
    private EditText etMessage;
    private Button btnSend;
    private ProgressBar progressBar;
    private TextView tvStudentInfo;
    private TextView tvMessages;

    private String studentId = "";
    private String schoolId = "";
    private String studentName = "";
    private String className = "";
    private String division = "";
    private String parentName = "Parent";
    private String parentPhone = "";
    private String parentEmail = "";

    private final List<TeacherItem> teachers = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_teacher_messaging);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        studentId = safe(getIntent().getStringExtra("studentId"));
        if (studentId.isEmpty()) {
            studentId = getSharedPreferences("MySchoolPrefs", MODE_PRIVATE)
                    .getString("studentId", "");
        }

        findViewById(R.id.btnParentTeacherMessagingBack)
                .setOnClickListener(v -> finish());

        spinnerTeacher = findViewById(R.id.spinnerParentTeacher);
        etMessage = findViewById(R.id.etParentTeacherMessage);
        btnSend = findViewById(R.id.btnSendParentTeacherMessage);
        progressBar = findViewById(R.id.parentTeacherProgressBar);
        tvStudentInfo = findViewById(R.id.tvParentTeacherStudentInfo);
        tvMessages = findViewById(R.id.tvParentTeacherMessages);

        btnSend.setOnClickListener(v -> sendMessage());

        loadStudent();
    }

    private void loadStudent() {
        if (studentId.isEmpty()) {
            Toast.makeText(this, "Student information not found", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        db.collection("students").document(studentId).get()
                .addOnSuccessListener(student -> {
                    progressBar.setVisibility(View.GONE);
                    if (!student.exists()) {
                        Toast.makeText(this, "Student record not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    schoolId = safe(student.getString("schoolId"));
                    className = safe(student.getString("className"));
                    division = safe(student.getString("division"));
                    studentName = buildName(student);
                    parentEmail = auth.getCurrentUser() == null
                            ? "" : safe(auth.getCurrentUser().getEmail());

                    String fatherEmail = safe(student.getString("fatherEmail"));
                    String motherEmail = safe(student.getString("motherEmail"));
                    String guardianName = safe(student.getString("guardianName"));
                    String guardianMobile = safe(student.getString("guardianMobile"));

                    if (!parentEmail.isEmpty() && parentEmail.equalsIgnoreCase(fatherEmail)) {
                        parentName = safe(student.getString("fatherName"));
                        parentPhone = safe(student.getString("fatherMobile"));
                    } else if (!parentEmail.isEmpty() && parentEmail.equalsIgnoreCase(motherEmail)) {
                        parentName = safe(student.getString("motherName"));
                        parentPhone = safe(student.getString("motherMobile"));
                    } else if (!guardianName.isEmpty()) {
                        parentName = guardianName;
                        parentPhone = guardianMobile;
                    } else if (!fatherEmail.isEmpty()) {
                        parentName = safe(student.getString("fatherName"));
                        parentPhone = safe(student.getString("fatherMobile"));
                    }

                    if (parentName.isEmpty()) parentName = "Parent";

                    tvStudentInfo.setText("Student: " + studentName +
                            "\nClass: " + className +
                            (division.isEmpty() ? "" : " - " + division));

                    loadTeachers();
                    listenMyMessages();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load student: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void loadTeachers() {
        db.collection("staff")
                .whereEqualTo("schoolId", schoolId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    teachers.clear();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String assignedClass = safe(doc.getString("assignedClass"));
                        String assignedDivision = safe(doc.getString("assignedDivision"));

                        if (!sameClass(assignedClass, className)) continue;
                        if (!division.isEmpty() && !assignedDivision.isEmpty()
                                && !sameText(assignedDivision, division)) continue;

                        String staffId = safe(doc.getString("staffId"));
                        if (staffId.isEmpty()) staffId = doc.getId();

                        String name = buildName(doc);
                        if (name.isEmpty()) name = "Teacher";

                        String subject = safe(doc.getString("subject"));
                        teachers.add(new TeacherItem(staffId, name, subject));
                    }

                    if (teachers.isEmpty()) {
                        spinnerTeacher.setAdapter(new ArrayAdapter<>(this,
                                android.R.layout.simple_spinner_dropdown_item,
                                new String[]{"No teacher assigned to this class"}));
                        btnSend.setEnabled(false);
                        return;
                    }

                    List<String> labels = new ArrayList<>();
                    for (TeacherItem teacher : teachers) {
                        labels.add(teacher.name +
                                (teacher.subject.isEmpty() ? "" : " • " + teacher.subject));
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, labels);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerTeacher.setAdapter(adapter);
                    btnSend.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    btnSend.setEnabled(false);
                    Toast.makeText(this, "Unable to load teachers: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void sendMessage() {
        if (teachers.isEmpty()) {
            Toast.makeText(this, "No teacher available", Toast.LENGTH_SHORT).show();
            return;
        }

        String message = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            etMessage.setError("Enter your message");
            etMessage.requestFocus();
            return;
        }

        TeacherItem teacher = teachers.get(spinnerTeacher.getSelectedItemPosition());
        String messageId = db.collection("parentMessages").document().getId();

        Map<String, Object> data = new HashMap<>();
        data.put("messageId", messageId);
        data.put("schoolId", schoolId);
        data.put("teacherId", teacher.staffId);
        data.put("teacherName", teacher.name);
        data.put("parentId", auth.getCurrentUser() == null ? "" : auth.getCurrentUser().getUid());
        data.put("parentName", parentName);
        data.put("parentEmail", parentEmail);
        data.put("parentPhone", parentPhone);
        data.put("studentId", studentId);
        data.put("studentName", studentName);
        data.put("className", className);
        data.put("division", division);
        data.put("message", message);
        data.put("reply", "");
        data.put("createdAt", FieldValue.serverTimestamp());

        btnSend.setEnabled(false);

        db.collection("parentMessages").document(messageId).set(data)
                .addOnSuccessListener(unused -> {
                    btnSend.setEnabled(true);
                    etMessage.setText("");
                    Toast.makeText(this, "Message sent to " + teacher.name, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    btnSend.setEnabled(true);
                    Toast.makeText(this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void listenMyMessages() {
        String parentId = auth.getCurrentUser() == null ? "" : auth.getCurrentUser().getUid();
        if (parentId.isEmpty()) return;

        db.collection("parentMessages")
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("parentId", parentId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) return;

                    StringBuilder text = new StringBuilder();
                    if (snapshot == null || snapshot.isEmpty()) {
                        tvMessages.setText("No messages sent yet.");
                        return;
                    }

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String teacher = safe(doc.getString("teacherName"));
                        String message = safe(doc.getString("message"));
                        String reply = safe(doc.getString("reply"));

                        text.append("To: ").append(teacher.isEmpty() ? "Teacher" : teacher)
                                .append("\nYou: ").append(message)
                                .append("\n");

                        if (!reply.isEmpty()) {
                            text.append("Teacher: ").append(reply).append("\n");
                        } else {
                            text.append("Teacher: Waiting for reply\n");
                        }
                        text.append("\n");
                    }

                    tvMessages.setText(text.toString().trim());
                });
    }

    private boolean sameText(String a, String b) {
        return normalize(a).equals(normalize(b));
    }

    private boolean sameClass(String a, String b) {
        return normalize(a).equals(normalize(b));
    }

    private String normalize(String value) {
        return safe(value).toLowerCase(Locale.ROOT)
                .replace("class", "")
                .replace(" ", "")
                .replace("-", "")
                .replace("_", "");
    }

    private String buildName(DocumentSnapshot doc) {
        String first = safe(doc.getString("firstName"));
        String middle = safe(doc.getString("middleName"));
        String last = safe(doc.getString("lastName"));
        String full = (first + " " + middle + " " + last).trim().replaceAll("\\s+", " ");
        return full;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static class TeacherItem {
        String staffId;
        String name;
        String subject;

        TeacherItem(String staffId, String name, String subject) {
            this.staffId = staffId;
            this.name = name;
            this.subject = subject;
        }
    }
}
