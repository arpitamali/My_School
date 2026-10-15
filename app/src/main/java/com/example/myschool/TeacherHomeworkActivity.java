package com.example.myschool;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TeacherHomeworkActivity extends AppCompatActivity {

    private Spinner spinnerClass;
    private Spinner spinnerSubject;

    private EditText etTitle;
    private EditText etDescription;

    private AppCompatImageButton btnBack;
    private Button btnUploadHomework;
    private ProgressBar progressBar;
    private RecyclerView recyclerHomework;

    private FirebaseFirestore db;

    private String staffId = "";
    private String schoolId = "";

    private ListenerRegistration homeworkListener;

    private final List<String> classList = new ArrayList<>();
    private final List<String> subjectList = new ArrayList<>();
    private final List<HomeworkItem> homeworkList = new ArrayList<>();

    private HomeworkAdapter adapter;

    // EDIT MODE
    private String editingHomeworkId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_teacher_homework);

        db = FirebaseFirestore.getInstance();

        staffId = safe(
                getIntent().getStringExtra("staffId")
        );

        initViews();
        setupRecyclerView();
        setupClicks();
        loadTeacher();
    }

    private void initViews() {

        btnBack = findViewById(
                R.id.btnHomeworkBack
        );

        spinnerClass = findViewById(
                R.id.spinnerHomeworkClass
        );

        spinnerSubject = findViewById(
                R.id.spinnerHomeworkSubject
        );

        etTitle = findViewById(
                R.id.etHomeworkTitle
        );

        etDescription = findViewById(
                R.id.etHomeworkDescription
        );

        btnUploadHomework = findViewById(
                R.id.btnUploadHomework
        );

        progressBar = findViewById(
                R.id.homeworkProgressBar
        );

        recyclerHomework = findViewById(
                R.id.recyclerHomework
        );
    }

    private void setupRecyclerView() {

        adapter = new HomeworkAdapter(
                homeworkList,
                new HomeworkAdapter.OnHomeworkActionListener() {

                    @Override
                    public void onEdit(HomeworkItem item) {
                        openEditHomework(item);
                    }

                    @Override
                    public void onDelete(HomeworkItem item) {
                        confirmDelete(item);
                    }
                }
        );

        recyclerHomework.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerHomework.setAdapter(adapter);
    }

    private void setupClicks() {

        btnBack.setOnClickListener(
                v -> finish()
        );

        btnUploadHomework.setOnClickListener(
                v -> saveHomework()
        );

        spinnerClass.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        // Subject is independent of teacher now.
                        // All subjects are shown.
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent) {
                    }
                }
        );
    }

    private void loadTeacher() {

        if (staffId.isEmpty()) {

            toast("Teacher ID missing.");
            return;
        }

        db.collection("staff")
                .document(staffId)
                .get()
                .addOnSuccessListener(staff -> {

                    if (!staff.exists()) {

                        toast(
                                "Teacher profile not found."
                        );

                        return;
                    }

                    schoolId = safe(
                            staff.getString("schoolId")
                    );

                    if (schoolId.isEmpty()) {

                        toast(
                                "School ID not found."
                        );

                        return;
                    }

                    loadClasses();
                    loadAllSubjects();
                    listenHomework();
                })
                .addOnFailureListener(
                        e -> toast(
                                "Unable to load teacher profile."
                        )
                );
    }

    // ---------------------------------------------------------
    // LOAD CLASSES
    // ---------------------------------------------------------

    private void loadClasses() {

        db.collection("students")
                .whereEqualTo(
                        "schoolId",
                        schoolId
                )
                .get()
                .addOnSuccessListener(snapshot -> {

                    Set<String> uniqueClasses =
                            new HashSet<>();

                    for (DocumentSnapshot doc :
                            snapshot.getDocuments()) {

                        String status =
                                safe(
                                        doc.getString("status")
                                );

                        if ("Deleted"
                                .equalsIgnoreCase(status)) {

                            continue;
                        }

                        String className =
                                safe(
                                        doc.getString(
                                                "className"
                                        )
                                );

                        if (!className.isEmpty()) {

                            uniqueClasses.add(
                                    className
                            );
                        }
                    }

                    classList.clear();

                    classList.addAll(
                            uniqueClasses
                    );

                    Collections.sort(
                            classList,
                            (a, b) ->
                                    Integer.compare(
                                            extractNumber(a),
                                            extractNumber(b)
                                    )
                    );

                    if (classList.isEmpty()) {

                        toast(
                                "No classes found."
                        );

                        return;
                    }

                    ArrayAdapter<String>
                            classAdapter =
                            new ArrayAdapter<>(
                                    this,
                                    android.R.layout
                                            .simple_spinner_item,
                                    classList
                            );

                    classAdapter.setDropDownViewResource(
                            android.R.layout
                                    .simple_spinner_dropdown_item
                    );

                    spinnerClass.setAdapter(
                            classAdapter
                    );

                })
                .addOnFailureListener(
                        e -> toast(
                                "Unable to load classes."
                        )
                );
    }

    // ---------------------------------------------------------
    // ALL SUBJECTS
    // ---------------------------------------------------------

    private void loadAllSubjects() {

        subjectList.clear();

        subjectList.addAll(
                Arrays.asList(
                        "English",
                        "Marathi",
                        "Hindi",
                        "Mathematics",
                        "Science",
                        "Social Science",
                        "Computer",
                        "EVS",
                        "Physical Education",
                        "Art",
                        "Other"
                )
        );

        ArrayAdapter<String>
                subjectAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout
                                .simple_spinner_item,
                        subjectList
                );

        subjectAdapter.setDropDownViewResource(
                android.R.layout
                        .simple_spinner_dropdown_item
        );

        spinnerSubject.setAdapter(
                subjectAdapter
        );
    }

    // ---------------------------------------------------------
    // SAVE / UPDATE HOMEWORK
    // ---------------------------------------------------------

    private void saveHomework() {

        if (spinnerClass.getSelectedItem() == null) {

            toast("Select class.");
            return;
        }

        if (spinnerSubject.getSelectedItem() == null) {

            toast("Select subject.");
            return;
        }

        String className =
                spinnerClass
                        .getSelectedItem()
                        .toString();

        String subject =
                spinnerSubject
                        .getSelectedItem()
                        .toString();

        String title =
                etTitle
                        .getText()
                        .toString()
                        .trim();

        String description =
                etDescription
                        .getText()
                        .toString()
                        .trim();

        if (TextUtils.isEmpty(title)) {

            etTitle.setError(
                    "Enter homework title"
            );

            etTitle.requestFocus();

            return;
        }

        if (TextUtils.isEmpty(description)) {

            etDescription.setError(
                    "Enter description"
            );

            etDescription.requestFocus();

            return;
        }

        setLoading(true);

        if (editingHomeworkId.isEmpty()) {

            // NEW HOMEWORK

            String id =
                    db.collection("homework")
                            .document()
                            .getId();

            HomeworkItem item =
                    new HomeworkItem();

            item.id = id;
            item.className = className;
            item.subject = subject;
            item.title = title;
            item.description = description;

            saveNewHomework(item);

        } else {

            // UPDATE EXISTING HOMEWORK

            updateHomework(
                    editingHomeworkId,
                    className,
                    subject,
                    title,
                    description
            );
        }
    }

    private void saveNewHomework(
            HomeworkItem item) {

        java.util.HashMap<String, Object> data =
                new java.util.HashMap<>();

        data.put(
                "homeworkId",
                item.id
        );

        data.put(
                "schoolId",
                schoolId
        );

        data.put(
                "createdBy",
                staffId
        );

        data.put(
                "className",
                item.className
        );

        data.put(
                "subject",
                item.subject
        );

        data.put(
                "title",
                item.title
        );

        data.put(
                "description",
                item.description
        );

        data.put(
                "materialName",
                ""
        );

        data.put(
                "materialUrl",
                ""
        );

        data.put(
                "createdAt",
                FieldValue.serverTimestamp()
        );

        db.collection("homework")
                .document(item.id)
                .set(data)
                .addOnSuccessListener(
                        unused -> {

                            setLoading(false);

                            clearForm();

                            toast(
                                    "Homework saved successfully."
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            setLoading(false);

                            toast(
                                    "Homework save failed: "
                                            + e.getMessage()
                            );
                        }
                );
    }

    // ---------------------------------------------------------
    // EDIT HOMEWORK
    // ---------------------------------------------------------

    private void openEditHomework(
            HomeworkItem item) {

        editingHomeworkId = item.id;

        for (int i = 0;
             i < classList.size();
             i++) {

            if (classMatches(
                    classList.get(i),
                    item.className
            )) {

                spinnerClass.setSelection(i);
                break;
            }
        }

        for (int i = 0;
             i < subjectList.size();
             i++) {

            if (subjectList
                    .get(i)
                    .equalsIgnoreCase(item.subject)) {

                spinnerSubject.setSelection(i);
                break;
            }
        }

        etTitle.setText(
                item.title
        );

        etDescription.setText(
                item.description
        );

        btnUploadHomework.setText(
                "Update Homework"
        );

        etTitle.requestFocus();

        Toast.makeText(
                this,
                "Edit mode",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void updateHomework(
            String id,
            String className,
            String subject,
            String title,
            String description) {

        java.util.HashMap<String, Object> updates =
                new java.util.HashMap<>();

        updates.put(
                "className",
                className
        );

        updates.put(
                "subject",
                subject
        );

        updates.put(
                "title",
                title
        );

        updates.put(
                "description",
                description
        );

        updates.put(
                "updatedAt",
                FieldValue.serverTimestamp()
        );

        db.collection("homework")
                .document(id)
                .update(updates)
                .addOnSuccessListener(
                        unused -> {

                            setLoading(false);

                            clearForm();

                            toast(
                                    "Homework updated successfully."
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            setLoading(false);

                            toast(
                                    "Homework update failed: "
                                            + e.getMessage()
                            );
                        }
                );
    }

    // ---------------------------------------------------------
    // DELETE HOMEWORK
    // ---------------------------------------------------------

    private void confirmDelete(
            HomeworkItem item) {

        new AlertDialog.Builder(this)
                .setTitle("Delete Homework")
                .setMessage(
                        "Are you sure you want to delete this homework?"
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) ->
                                deleteHomework(item)
                )
                .show();
    }

    private void deleteHomework(
            HomeworkItem item) {

        if (item.id.isEmpty()) {

            toast("Invalid homework.");
            return;
        }

        setLoading(true);

        db.collection("homework")
                .document(item.id)
                .delete()
                .addOnSuccessListener(
                        unused -> {

                            setLoading(false);

                            toast(
                                    "Homework deleted."
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            setLoading(false);

                            toast(
                                    "Delete failed: "
                                            + e.getMessage()
                            );
                        }
                );
    }

    // ---------------------------------------------------------
    // REALTIME HOMEWORK
    // ---------------------------------------------------------

    private void listenHomework() {

        if (homeworkListener != null) {

            homeworkListener.remove();
        }

        homeworkListener =
                db.collection("homework")
                        .whereEqualTo(
                                "schoolId",
                                schoolId
                        )
                        .addSnapshotListener(
                                (snapshot, error) -> {

                                    if (error != null ||
                                            snapshot == null) {

                                        return;
                                    }

                                    homeworkList.clear();

                                    for (DocumentSnapshot doc :
                                            snapshot.getDocuments()) {

                                        HomeworkItem item =
                                                new HomeworkItem();

                                        item.id =
                                                safe(
                                                        doc.getString(
                                                                "homeworkId"
                                                        )
                                                );

                                        item.className =
                                                safe(
                                                        doc.getString(
                                                                "className"
                                                        )
                                                );

                                        item.subject =
                                                safe(
                                                        doc.getString(
                                                                "subject"
                                                        )
                                                );

                                        item.title =
                                                safe(
                                                        doc.getString(
                                                                "title"
                                                        )
                                                );

                                        item.description =
                                                safe(
                                                        doc.getString(
                                                                "description"
                                                        )
                                                );

                                        homeworkList.add(
                                                item
                                        );
                                    }

                                    Collections.reverse(
                                            homeworkList
                                    );

                                    adapter.notifyDataSetChanged();
                                }
                        );
    }

    // ---------------------------------------------------------
    // CLEAR FORM
    // ---------------------------------------------------------

    private void clearForm() {

        etTitle.setText("");
        etDescription.setText("");

        editingHomeworkId = "";

        btnUploadHomework.setText(
                "Save Homework"
        );
    }

    private void setLoading(
            boolean loading) {

        progressBar.setVisibility(
                loading
                        ? View.VISIBLE
                        : View.GONE
        );

        btnUploadHomework.setEnabled(
                !loading
        );
    }

    // ---------------------------------------------------------
    // HELPERS
    // ---------------------------------------------------------

    private int extractNumber(
            String value) {

        try {

            String number =
                    value.replaceAll(
                            "[^0-9]",
                            ""
                    );

            if (number.isEmpty()) {

                return 999;
            }

            return Integer.parseInt(
                    number
            );

        } catch (Exception e) {

            return 999;
        }
    }

    private boolean classMatches(
            String a,
            String b) {

        return normalizeClass(a)
                .equals(
                        normalizeClass(b)
                );
    }

    private String normalizeClass(
            String value) {

        if (value == null) {

            return "";
        }

        return value
                .toLowerCase()
                .replace(
                        "class",
                        ""
                )
                .replace(
                        "standard",
                        ""
                )
                .replace(
                        "std",
                        ""
                )
                .trim()
                .replaceAll(
                        "\\s+",
                        ""
                );
    }

    private String safe(
            String value) {

        return value == null
                ? ""
                : value.trim();
    }

    private void toast(
            String message) {

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    protected void onDestroy() {

        if (homeworkListener != null) {

            homeworkListener.remove();
        }

        super.onDestroy();
    }

    // ---------------------------------------------------------
    // MODEL
    // ---------------------------------------------------------

    static class HomeworkItem {

        String id = "";
        String className = "";
        String subject = "";
        String title = "";
        String description = "";
    }
}