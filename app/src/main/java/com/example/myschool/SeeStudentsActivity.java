package com.example.myschool;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SeeStudentsActivity extends AppCompatActivity {

    // =========================================================
    // VIEWS
    // =========================================================

    private ImageButton btnBack;

    private RecyclerView recyclerStudents;

    private ProgressBar progressBar;

    private TextView tvEmpty;

    private TextView tvStudentCount;

    private EditText etSearchStudent;


    // =========================================================
    // FIREBASE
    // =========================================================

    private StudentAdapter adapter;

    private FirebaseAuth auth;

    private FirebaseFirestore db;


    // =========================================================
    // STUDENT LIST
    // =========================================================

    private final List<StudentModel> studentList =
            new ArrayList<>();

    // Original complete list
    private final List<StudentModel> allStudents =
            new ArrayList<>();


    // Used to prevent duplicate students
    private final Set<String> studentIds =
            new HashSet<>();


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_see_students
        );


        auth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();


        initViews();

        setupRecyclerView();

        setupSearch();

        loadStudents();
    }


    // =========================================================
    // INITIALIZE
    // =========================================================

    private void initViews() {

        btnBack =
                findViewById(
                        R.id.btnBack
                );


        recyclerStudents =
                findViewById(
                        R.id.recyclerStudents
                );


        progressBar =
                findViewById(
                        R.id.progressBar
                );


        tvEmpty =
                findViewById(
                        R.id.tvEmpty
                );


        tvStudentCount =
                findViewById(
                        R.id.tvStudentCount
                );


        etSearchStudent =
                findViewById(
                        R.id.etSearchStudent
                );


        btnBack.setOnClickListener(
                v -> finish()
        );
    }


    // =========================================================
    // RECYCLER
    // =========================================================

    private void setupRecyclerView() {

        recyclerStudents.setLayoutManager(
                new LinearLayoutManager(this)
        );


        adapter =
                new StudentAdapter(
                        this,
                        studentList
                );


        recyclerStudents.setAdapter(
                adapter
        );
    }


    // =========================================================
    // SEARCH
    // =========================================================

    private void setupSearch() {

        if (etSearchStudent == null) {
            return;
        }


        etSearchStudent.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }


                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        filterStudents(
                                s.toString()
                        );
                    }


                    @Override
                    public void afterTextChanged(
                            Editable s) {
                    }
                }
        );
    }


    // =========================================================
    // FILTER STUDENTS
    // =========================================================

    private void filterStudents(String query) {

        query =
                query == null
                        ? ""
                        : query.trim().toLowerCase();


        studentList.clear();


        // -----------------------------------------------------
        // EMPTY SEARCH
        // -----------------------------------------------------

        if (query.isEmpty()) {

            studentList.addAll(
                    allStudents
            );

        } else {

            // -------------------------------------------------
            // SEARCH
            // -------------------------------------------------

            for (StudentModel student : allStudents) {

                if (student == null) {
                    continue;
                }


                String firstName =
                        safe(student.getFirstName())
                                .toLowerCase();


                String middleName =
                        safe(student.getMiddleName())
                                .toLowerCase();


                String lastName =
                        safe(student.getLastName())
                                .toLowerCase();


                String fullName =
                        (
                                firstName
                                        + " "
                                        + middleName
                                        + " "
                                        + lastName
                        )
                                .replaceAll(
                                        "\\s+",
                                        " "
                                )
                                .trim();


                String admissionNumber =
                        safe(
                                student.getAdmissionNumber()
                        )
                                .toLowerCase();


                String studentId =
                        safe(
                                student.getStudentId()
                        )
                                .toLowerCase();


                String className =
                        safe(
                                student.getClassName()
                        )
                                .toLowerCase();


                String division =
                        safe(
                                student.getDivision()
                        )
                                .toLowerCase();


                if (
                        firstName.contains(query)
                                || middleName.contains(query)
                                || lastName.contains(query)
                                || fullName.contains(query)
                                || admissionNumber.contains(query)
                                || studentId.contains(query)
                                || className.contains(query)
                                || division.contains(query)
                ) {

                    studentList.add(student);
                }
            }
        }


        // -----------------------------------------------------
        // UPDATE
        // -----------------------------------------------------

        adapter.notifyDataSetChanged();


        updateSearchResultCount();
    }


    // =========================================================
    // SEARCH RESULT COUNT
    // =========================================================

    private void updateSearchResultCount() {

        int count =
                studentList.size();


        if (etSearchStudent != null
                && !etSearchStudent
                .getText()
                .toString()
                .trim()
                .isEmpty()) {

            tvStudentCount.setText(
                    count +
                            (
                                    count == 1
                                            ? " Student found"
                                            : " Students found"
                            )
            );

        } else {

            tvStudentCount.setText(
                    count +
                            (
                                    count == 1
                                            ? " Student"
                                            : " Students"
                            )
            );
        }


        if (count == 0) {

            recyclerStudents.setVisibility(
                    View.GONE
            );


            if (etSearchStudent != null
                    && !etSearchStudent
                    .getText()
                    .toString()
                    .trim()
                    .isEmpty()) {

                tvEmpty.setText(
                        "No students found."
                );

            } else {

                tvEmpty.setText(
                        "No students added yet."
                );
            }


            tvEmpty.setVisibility(
                    View.VISIBLE
            );

        } else {

            tvEmpty.setVisibility(
                    View.GONE
            );


            recyclerStudents.setVisibility(
                    View.VISIBLE
            );
        }
    }


    // =========================================================
    // LOAD STUDENTS
    // =========================================================

    private void loadStudents() {

        if (auth.getCurrentUser() == null) {

            showEmpty(
                    "Please login again."
            );

            return;
        }


        String schoolId =
                auth.getCurrentUser()
                        .getUid();


        progressBar.setVisibility(
                View.VISIBLE
        );


        recyclerStudents.setVisibility(
                View.GONE
        );


        tvEmpty.setVisibility(
                View.GONE
        );


        studentList.clear();

        allStudents.clear();

        studentIds.clear();


        // =====================================================
        // FIRST:
        // students/{studentId}
        // =====================================================

        db.collection("students")
                .get()
                .addOnSuccessListener(
                        topLevelSnapshot -> {

                            addTopLevelStudents(
                                    topLevelSnapshot,
                                    schoolId
                            );


                            // =================================================
                            // SECOND:
                            // schools/{schoolId}/students
                            // =================================================

                            loadOldSchoolStudents(
                                    schoolId
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            // Even if top-level fails,
                            // try old school path.

                            loadOldSchoolStudents(
                                    schoolId
                            );
                        }
                );
    }


    // =========================================================
    // TOP LEVEL STUDENTS
    // =========================================================

    private void addTopLevelStudents(
            @NonNull com.google.firebase.firestore.QuerySnapshot snapshot,
            String schoolId) {

        for (
                DocumentSnapshot document :
                snapshot.getDocuments()
        ) {

            StudentModel student =
                    document.toObject(
                            StudentModel.class
                    );


            if (student == null) {
                continue;
            }


            // -------------------------------------------------
            // If schoolId exists, show only current school.
            //
            // If old document has NO schoolId, also show it.
            // -------------------------------------------------

            String documentSchoolId =
                    document.getString(
                            "schoolId"
                    );


            if (documentSchoolId != null
                    && !documentSchoolId.isEmpty()
                    && !documentSchoolId.equals(schoolId)) {

                continue;
            }


            student.setStudentId(
                    document.getId()
            );


            addStudentIfNew(
                    student
            );
        }
    }


    // =========================================================
    // OLD SCHOOL PATH
    // =========================================================

    private void loadOldSchoolStudents(
            String schoolId) {

        db.collection("schools")
                .document(schoolId)
                .collection("students")
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            for (
                                    DocumentSnapshot document :
                                    snapshot.getDocuments()
                            ) {

                                StudentModel student =
                                        document.toObject(
                                                StudentModel.class
                                        );


                                if (student == null) {
                                    continue;
                                }


                                student.setStudentId(
                                        document.getId()
                                );


                                if (student.getSchoolId() == null
                                        || student.getSchoolId().isEmpty()) {

                                    student.setSchoolId(
                                            schoolId
                                    );
                                }


                                addStudentIfNew(
                                        student
                                );
                            }


                            finishLoading();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            // Top-level data may still exist.

                            finishLoading();
                        }
                );
    }


    // =========================================================
    // ADD WITHOUT DUPLICATE
    // =========================================================

    private void addStudentIfNew(
            StudentModel student) {

        String id =
                student.getStudentId();


        if (id == null
                || id.trim().isEmpty()) {

            // Fallback unique key

            id =
                    (
                            safe(
                                    student.getAdmissionNumber()
                            )
                                    + "_"
                                    + safe(
                                    student.getFirstName()
                            )
                                    + "_"
                                    + safe(
                                    student.getLastName()
                            )
                    );
        }


        if (studentIds.contains(id)) {
            return;
        }


        studentIds.add(id);

        allStudents.add(student);
    }


    // =========================================================
    // FINISH LOADING
    // =========================================================

    private void finishLoading() {

        progressBar.setVisibility(
                View.GONE
        );


        sortStudents();


        // -----------------------------------------------------
        // Put complete list into displayed list
        // -----------------------------------------------------

        studentList.clear();

        studentList.addAll(
                allStudents
        );


        adapter.notifyDataSetChanged();


        int count =
                studentList.size();


        tvStudentCount.setText(
                count +
                        (
                                count == 1
                                        ? " Student"
                                        : " Students"
                        )
        );


        if (count == 0) {

            recyclerStudents.setVisibility(
                    View.GONE
            );


            tvEmpty.setText(
                    "No students added yet."
            );


            tvEmpty.setVisibility(
                    View.VISIBLE
            );

        } else {

            tvEmpty.setVisibility(
                    View.GONE
            );


            recyclerStudents.setVisibility(
                    View.VISIBLE
            );
        }
    }


    // =========================================================
    // SORT
    // =========================================================

    private void sortStudents() {

        Collections.sort(
                allStudents,
                new Comparator<StudentModel>() {

                    @Override
                    public int compare(
                            StudentModel first,
                            StudentModel second) {

                        Timestamp firstTime =
                                first.getCreatedAt();


                        Timestamp secondTime =
                                second.getCreatedAt();


                        if (firstTime == null
                                && secondTime == null) {

                            return 0;
                        }


                        if (firstTime == null) {

                            return 1;
                        }


                        if (secondTime == null) {

                            return -1;
                        }


                        return secondTime.compareTo(
                                firstTime
                        );
                    }
                }
        );
    }


    // =========================================================
    // SAFE
    // =========================================================

    private String safe(String value) {

        if (value == null
                || value.trim().isEmpty()) {

            return "";
        }

        return value.trim();
    }


    // =========================================================
    // EMPTY
    // =========================================================

    private void showEmpty(
            String message) {

        if (progressBar != null) {

            progressBar.setVisibility(
                    View.GONE
            );
        }


        if (recyclerStudents != null) {

            recyclerStudents.setVisibility(
                    View.GONE
            );
        }


        if (tvStudentCount != null) {

            tvStudentCount.setText(
                    "0 Students"
            );
        }


        if (tvEmpty != null) {

            tvEmpty.setText(
                    message
            );


            tvEmpty.setVisibility(
                    View.VISIBLE
            );
        }
    }


    // =========================================================
    // RELOAD
    // =========================================================

    @Override
    protected void onResume() {

        super.onResume();


        if (auth != null
                && db != null
                && recyclerStudents != null) {

            loadStudents();
        }
    }
}