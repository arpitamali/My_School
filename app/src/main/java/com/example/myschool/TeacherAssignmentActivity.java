package com.example.myschool;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TeacherAssignmentActivity extends AppCompatActivity {

    private Spinner spinnerClass;
    private Spinner spinnerSubject;

    private EditText etTitle;
    private EditText etDescription;

    private Button btnUploadAssignment;
    private AppCompatImageButton btnBack;

    private ProgressBar progressBar;
    private RecyclerView recyclerAssignments;

    private FirebaseFirestore db;

    private String staffId = "";
    private String schoolId = "";

    private final List<String> classList =
            new ArrayList<>();

    private final List<String> subjectList =
            new ArrayList<>();

    private final List<AssignmentModel> assignmentList =
            new ArrayList<>();

    private ArrayAdapter<String> classAdapter;
    private ArrayAdapter<String> subjectAdapter;

    private AssignmentAdapter assignmentAdapter;

    private ListenerRegistration assignmentListener;

    // EDIT MODE
    private String editingAssignmentId = "";

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_teacher_assignment
        );

        db = FirebaseFirestore.getInstance();

        staffId =
                getIntent().getStringExtra("staffId");

        if (staffId == null) {
            staffId = "";
        }

        initializeViews();
        setupSpinners();
        setupRecyclerView();
        setupClicks();
        loadTeacher();
    }

    private void initializeViews() {

        btnBack =
                findViewById(
                        R.id.btnAssignmentBack
                );

        spinnerClass =
                findViewById(
                        R.id.spinnerAssignmentClass
                );

        spinnerSubject =
                findViewById(
                        R.id.spinnerAssignmentSubject
                );

        etTitle =
                findViewById(
                        R.id.etAssignmentTitle
                );

        etDescription =
                findViewById(
                        R.id.etAssignmentDescription
                );

        btnUploadAssignment =
                findViewById(
                        R.id.btnUploadAssignment
                );

        progressBar =
                findViewById(
                        R.id.assignmentProgressBar
                );

        recyclerAssignments =
                findViewById(
                        R.id.recyclerAssignments
                );
    }

    private void setupSpinners() {

        classAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        classList
                );

        classAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerClass.setAdapter(
                classAdapter
        );

        subjectAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        subjectList
                );

        subjectAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerSubject.setAdapter(
                subjectAdapter
        );
    }

    private void setupRecyclerView() {

        assignmentAdapter =
                new AssignmentAdapter(
                        assignmentList,
                        new AssignmentAdapter.OnAssignmentActionListener() {

                            @Override
                            public void onEdit(
                                    AssignmentModel item) {

                                openEditAssignment(item);
                            }

                            @Override
                            public void onDelete(
                                    AssignmentModel item) {

                                deleteAssignment(item);
                            }
                        }
                );

        recyclerAssignments.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerAssignments.setAdapter(
                assignmentAdapter
        );
    }

    private void setupClicks() {

        btnBack.setOnClickListener(
                v -> finish()
        );

        btnUploadAssignment.setOnClickListener(
                v -> saveAssignment()
        );
    }

    // =========================================================
    // LOAD TEACHER
    // =========================================================

    private void loadTeacher() {

        if (TextUtils.isEmpty(staffId)) {

            Toast.makeText(
                    this,
                    "Teacher ID not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        db.collection("staff")
                .document(staffId)
                .get()
                .addOnSuccessListener(
                        documentSnapshot -> {

                            if (!documentSnapshot.exists()) {

                                Toast.makeText(
                                        this,
                                        "Teacher profile not found",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            schoolId =
                                    safe(
                                            documentSnapshot.getString(
                                                    "schoolId"
                                            )
                                    );

                            loadClasses();
                            loadSubjects();
                            listenToAssignments();
                        }
                )
                .addOnFailureListener(
                        e -> Toast.makeText(
                                this,
                                "Unable to load teacher: "
                                        + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    // =========================================================
    // CLASSES
    // =========================================================

    private void loadClasses() {

        if (schoolId.isEmpty()) {
            return;
        }

        db.collection("students")
                .whereEqualTo(
                        "schoolId",
                        schoolId
                )
                .get()
                .addOnSuccessListener(
                        querySnapshot -> {

                            Set<String> uniqueClasses =
                                    new HashSet<>();

                            for (DocumentSnapshot doc :
                                    querySnapshot.getDocuments()) {

                                String status =
                                        safe(
                                                doc.getString(
                                                        "status"
                                                )
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

                            classList.add(
                                    "Select Class"
                            );

                            List<String> sorted =
                                    new ArrayList<>(
                                            uniqueClasses
                                    );

                            Collections.sort(
                                    sorted,
                                    (a, b) ->
                                            Integer.compare(
                                                    extractNumber(a),
                                                    extractNumber(b)
                                            )
                            );

                            classList.addAll(
                                    sorted
                            );

                            classAdapter.notifyDataSetChanged();
                        }
                )
                .addOnFailureListener(
                        e -> Toast.makeText(
                                this,
                                "Unable to load classes",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    // =========================================================
    // ALL SUBJECTS
    // =========================================================

    private void loadSubjects() {

        subjectList.clear();

        subjectList.add(
                "Select Subject"
        );

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

        subjectAdapter.notifyDataSetChanged();
    }

    // =========================================================
    // SAVE / UPDATE ASSIGNMENT
    // =========================================================

    private void saveAssignment() {

        if (spinnerClass.getSelectedItem() == null ||
                spinnerSubject.getSelectedItem() == null) {

            Toast.makeText(
                    this,
                    "Please select class and subject",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String className =
                spinnerClass
                        .getSelectedItem()
                        .toString()
                        .trim();

        String subject =
                spinnerSubject
                        .getSelectedItem()
                        .toString()
                        .trim();

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

        if (className.equals(
                "Select Class"
        )) {

            Toast.makeText(
                    this,
                    "Please select class",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (subject.equals(
                "Select Subject"
        )) {

            Toast.makeText(
                    this,
                    "Please select subject",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (TextUtils.isEmpty(title)) {

            etTitle.setError(
                    "Enter assignment title"
            );

            etTitle.requestFocus();

            return;
        }

        if (TextUtils.isEmpty(description)) {

            etDescription.setError(
                    "Enter assignment description"
            );

            etDescription.requestFocus();

            return;
        }

        if (schoolId.isEmpty()) {

            Toast.makeText(
                    this,
                    "School ID not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        setLoading(true);

        // EDIT
        if (!editingAssignmentId.isEmpty()) {

            updateAssignment(
                    editingAssignmentId,
                    className,
                    subject,
                    title,
                    description
            );

            return;
        }

        // NEW ASSIGNMENT

        String assignmentId =
                db.collection("assignments")
                        .document()
                        .getId();

        HashMap<String, Object> data =
                new HashMap<>();

        data.put(
                "assignmentId",
                assignmentId
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
                className
        );

        data.put(
                "subject",
                subject
        );

        data.put(
                "title",
                title
        );

        data.put(
                "description",
                description
        );

        // PDF removed
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

        db.collection("assignments")
                .document(assignmentId)
                .set(data)
                .addOnSuccessListener(
                        unused -> {

                            setLoading(false);

                            clearForm();

                            Toast.makeText(
                                    this,
                                    "Assignment saved successfully",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            setLoading(false);

                            Toast.makeText(
                                    this,
                                    "Failed to save assignment: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // =========================================================
    // EDIT
    // =========================================================

    private void openEditAssignment(
            AssignmentModel item) {

        editingAssignmentId =
                safe(item.assignmentId);

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
                    .equalsIgnoreCase(
                            item.subject
                    )) {

                spinnerSubject.setSelection(i);
                break;
            }
        }

        etTitle.setText(
                safe(item.title)
        );

        etDescription.setText(
                safe(item.description)
        );

        btnUploadAssignment.setText(
                "Update Assignment"
        );

        etTitle.requestFocus();
    }

    private void updateAssignment(
            String id,
            String className,
            String subject,
            String title,
            String description) {

        HashMap<String, Object> updates =
                new HashMap<>();

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

        db.collection("assignments")
                .document(id)
                .update(updates)
                .addOnSuccessListener(
                        unused -> {

                            setLoading(false);

                            clearForm();

                            Toast.makeText(
                                    this,
                                    "Assignment updated successfully",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            setLoading(false);

                            Toast.makeText(
                                    this,
                                    "Update failed: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // =========================================================
    // DELETE
    // =========================================================

    private void deleteAssignment(
            AssignmentModel item) {

        if (item.assignmentId == null ||
                item.assignmentId.isEmpty()) {

            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Assignment")
                .setMessage(
                        "Are you sure you want to delete this assignment?"
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> {

                            setLoading(true);

                            db.collection("assignments")
                                    .document(
                                            item.assignmentId
                                    )
                                    .delete()
                                    .addOnSuccessListener(
                                            unused -> {

                                                setLoading(false);

                                                Toast.makeText(
                                                        this,
                                                        "Assignment deleted",
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                            }
                                    )
                                    .addOnFailureListener(
                                            e -> {

                                                setLoading(false);

                                                Toast.makeText(
                                                        this,
                                                        "Delete failed: "
                                                                + e.getMessage(),
                                                        Toast.LENGTH_LONG
                                                ).show();
                                            }
                                    );
                        }
                )
                .show();
    }

    // =========================================================
    // REALTIME LISTENER
    // =========================================================

    private void listenToAssignments() {

        if (schoolId.isEmpty()) {
            return;
        }

        if (assignmentListener != null) {
            assignmentListener.remove();
        }

        assignmentListener =
                db.collection("assignments")
                        .whereEqualTo(
                                "schoolId",
                                schoolId
                        )
                        .addSnapshotListener(
                                (snapshots, error) -> {

                                    if (error != null ||
                                            snapshots == null) {

                                        return;
                                    }

                                    assignmentList.clear();

                                    for (DocumentSnapshot doc :
                                            snapshots.getDocuments()) {

                                        AssignmentModel model =
                                                new AssignmentModel();

                                        model.assignmentId =
                                                safe(
                                                        doc.getString(
                                                                "assignmentId"
                                                        )
                                                );

                                        model.className =
                                                safe(
                                                        doc.getString(
                                                                "className"
                                                        )
                                                );

                                        model.subject =
                                                safe(
                                                        doc.getString(
                                                                "subject"
                                                        )
                                                );

                                        model.title =
                                                safe(
                                                        doc.getString(
                                                                "title"
                                                        )
                                                );

                                        model.description =
                                                safe(
                                                        doc.getString(
                                                                "description"
                                                        )
                                                );

                                        assignmentList.add(
                                                model
                                        );
                                    }

                                    Collections.reverse(
                                            assignmentList
                                    );

                                    assignmentAdapter
                                            .notifyDataSetChanged();
                                }
                        );
    }

    // =========================================================
    // CLEAR
    // =========================================================

    private void clearForm() {

        etTitle.setText("");
        etDescription.setText("");

        editingAssignmentId = "";

        btnUploadAssignment.setText(
                "Save Assignment"
        );

        spinnerClass.setSelection(0);
        spinnerSubject.setSelection(0);
    }

    private void setLoading(
            boolean loading) {

        progressBar.setVisibility(
                loading
                        ? View.VISIBLE
                        : View.GONE
        );

        btnUploadAssignment.setEnabled(
                !loading
        );
    }

    // =========================================================
    // HELPERS
    // =========================================================

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

    @Override
    protected void onDestroy() {

        if (assignmentListener != null) {

            assignmentListener.remove();
            assignmentListener = null;
        }

        super.onDestroy();
    }

    // =========================================================
    // MODEL
    // =========================================================

    public static class AssignmentModel {

        String assignmentId = "";
        String className = "";
        String subject = "";
        String title = "";
        String description = "";
    }
}