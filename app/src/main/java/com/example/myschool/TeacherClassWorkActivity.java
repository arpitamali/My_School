package com.example.myschool;

import android.app.DatePickerDialog;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TeacherClassWorkActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private MaterialButton btnHomeworkTab;
    private MaterialButton btnAnnouncementTab;
    private MaterialButton btnSave;
    private EditText etTitle;
    private EditText etSubject;
    private EditText etDescription;
    private EditText etDueDate;
    private TextView tvTarget;
    private LinearLayout listContainer;

    private String mode = "homework";
    private String schoolId = "";
    private String staffId = "";
    private String className = "";
    private String division = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_class_work);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews();
        readIntent();
        setupClicks();
        applyMode();
        loadItems();
    }

    private void initViews() {
        btnHomeworkTab = findViewById(R.id.btnClassWorkHomeworkTab);
        btnAnnouncementTab = findViewById(R.id.btnClassWorkAnnouncementTab);
        btnSave = findViewById(R.id.btnSaveClassWork);
        etTitle = findViewById(R.id.etClassWorkTitle);
        etSubject = findViewById(R.id.etClassWorkSubject);
        etDescription = findViewById(R.id.etClassWorkDescription);
        etDueDate = findViewById(R.id.etClassWorkDueDate);
        tvTarget = findViewById(R.id.tvClassWorkTarget);
        listContainer = findViewById(R.id.classWorkListContainer);
    }

    private void readIntent() {
        mode = safe(getIntent().getStringExtra("mode"));
        if (!mode.equals("announcement")) mode = "homework";
        staffId = safe(getIntent().getStringExtra("staffId"));
    }

    private void setupClicks() {
        findViewById(R.id.btnClassWorkBack).setOnClickListener(v -> finish());
        btnHomeworkTab.setOnClickListener(v -> {
            mode = "homework";
            applyMode();
            loadItems();
        });
        btnAnnouncementTab.setOnClickListener(v -> {
            mode = "announcement";
            applyMode();
            loadItems();
        });
        etDueDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveItem());
    }

    private void loadAssignment() {
        if (staffId.isEmpty()) {
            loadFailure("Teacher profile ID missing.");
            return;
        }
        db.collection("staff").document(staffId).get().addOnSuccessListener(staff -> {
            if (!staff.exists()) {
                loadFailure("Teacher profile not found.");
                return;
            }
            schoolId = safe(staff.getString("schoolId"));
            className = safe(staff.getString("assignedClass"));
            division = safe(staff.getString("assignedDivision"));
            String target = className + (division.isEmpty() ? "" : " - " + division);
            tvTarget.setText("Target class: " + target);
            loadItems();
        }).addOnFailureListener(e -> loadFailure("Unable to load teacher profile."));
    }

    private void loadFailure(String message) {
        schoolId = "";
        toast(message);
    }

    private void applyMode() {
        boolean homework = mode.equals("homework");
        btnHomeworkTab.setEnabled(!homework);
        btnAnnouncementTab.setEnabled(homework);
        etSubject.setVisibility(homework ? android.view.View.VISIBLE : android.view.View.GONE);
        etDueDate.setVisibility(homework ? android.view.View.VISIBLE : android.view.View.GONE);
        etTitle.setHint(homework ? "Homework title" : "Announcement title");
        etDescription.setHint(homework ? "Homework instructions" : "Announcement message");
        btnSave.setText(homework ? "Publish Homework" : "Publish Announcement");
        if (schoolId.isEmpty()) loadAssignment();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) ->
                etDueDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year)),
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveItem() {
        if (schoolId.isEmpty() || className.isEmpty()) {
            toast("Teacher class information is missing.");
            return;
        }

        String title = safe(etTitle.getText().toString());
        String description = safe(etDescription.getText().toString());
        String subject = safe(etSubject.getText().toString());
        String dueDate = safe(etDueDate.getText().toString());

        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            etTitle.requestFocus();
            return;
        }
        if (description.isEmpty()) {
            etDescription.setError("Description is required");
            etDescription.requestFocus();
            return;
        }
        if (mode.equals("homework") && subject.isEmpty()) {
            etSubject.setError("Subject is required");
            etSubject.requestFocus();
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Publishing...");

        Map<String, Object> data = new HashMap<>();
        data.put("schoolId", schoolId);
        data.put("className", className);
        data.put("division", division);
        data.put("title", title);
        data.put("description", description);
        data.put("createdBy", auth.getCurrentUser() == null ? "" : auth.getCurrentUser().getUid());
        data.put("teacherId", staffId);
        data.put("createdAt", FieldValue.serverTimestamp());

        String collection;
        if (mode.equals("homework")) {
            collection = "homework";
            data.put("subject", subject);
            data.put("dueDate", dueDate);
        } else {
            collection = "classAnnouncements";
        }

        db.collection(collection).add(data)
                .addOnSuccessListener(v -> {
                    btnSave.setEnabled(true);
                    applyMode();
                    clearForm();
                    toast(mode.equals("homework") ? "Homework published." : "Announcement published.");
                    loadItems();
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    applyMode();
                    toast("Publish failed: " + e.getMessage());
                });
    }

    private void loadItems() {
        listContainer.removeAllViews();
        if (schoolId.isEmpty() || className.isEmpty()) return;

        String collection = mode.equals("homework") ? "homework" : "classAnnouncements";
        db.collection(collection)
                .whereEqualTo("schoolId", schoolId)
                .whereEqualTo("className", className)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String itemDivision = safe(doc.getString("division"));
                        if (!divisionMatches(division, itemDivision)) continue;
                        addItemCard(collection, doc);
                    }
                    if (listContainer.getChildCount() == 0) {
                        TextView empty = new TextView(this);
                        empty.setText("No " + (mode.equals("homework") ? "homework" : "announcements") + " published yet.");
                        empty.setTextColor(0xFF777B86);
                        empty.setGravity(Gravity.CENTER);
                        empty.setPadding(20, 30, 20, 30);
                        listContainer.addView(empty);
                    }
                })
                .addOnFailureListener(e -> toast("Unable to load items."));
    }

    private void addItemCard(String collection, DocumentSnapshot doc) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, 8);
        card.setLayoutParams(params);
        card.setRadius(16);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(14, 12, 14, 12);

        TextView type = new TextView(this);
        type.setText(mode.equals("homework") ? safe(doc.getString("subject")) : "Class Announcement");
        type.setTextColor(0xFF5427D8);
        type.setTextSize(11);
        type.setTypeface(null, 1);
        root.addView(type);

        TextView title = new TextView(this);
        title.setText(safe(doc.getString("title")));
        title.setTextColor(0xFF25252B);
        title.setTextSize(15);
        title.setTypeface(null, 1);
        title.setPadding(0, 4, 0, 3);
        root.addView(title);

        TextView description = new TextView(this);
        description.setText(safe(doc.getString("description")));
        description.setTextColor(0xFF6B6F7B);
        description.setTextSize(12);
        root.addView(description);

        if (mode.equals("homework")) {
            TextView due = new TextView(this);
            due.setText("Due: " + safe(doc.getString("dueDate"), "Not specified"));
            due.setTextColor(0xFF8A5A00);
            due.setTextSize(11);
            due.setPadding(0, 6, 0, 0);
            root.addView(due);
        }

        MaterialButton delete = new MaterialButton(this);
        delete.setText("Delete");
        delete.setAllCaps(false);
        delete.setOnClickListener(v -> db.collection(collection).document(doc.getId()).delete()
                .addOnSuccessListener(x -> loadItems())
                .addOnFailureListener(e -> toast("Delete failed.")));
        root.addView(delete);

        card.addView(root);
        listContainer.addView(card);
    }

    private void clearForm() {
        etTitle.setText("");
        etSubject.setText("");
        etDescription.setText("");
        etDueDate.setText("");
    }

    private boolean divisionMatches(String teacherDivision, String itemDivision) {
        return teacherDivision.isEmpty() || teacherDivision.equalsIgnoreCase("All Divisions")
                || itemDivision.equalsIgnoreCase(teacherDivision);
    }

    private String safe(String value) { return value == null ? "" : value.trim(); }
    private String safe(String value, String fallback) { return safe(value).isEmpty() ? fallback : safe(value); }
    private void toast(String message) { Toast.makeText(this, message, Toast.LENGTH_LONG).show(); }
}
