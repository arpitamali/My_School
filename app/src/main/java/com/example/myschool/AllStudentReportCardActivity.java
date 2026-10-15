package com.example.myschool;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
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

public class AllStudentReportCardActivity extends AppCompatActivity {

    // =========================================================
    // VIEWS
    // =========================================================

    private ImageButton btnBack;

    private Spinner spClassFilter;

    private EditText etSearchStudent;

    private TextView tvStudentCount;

    private TextView tvNoStudents;

    private LinearLayout studentContainer;


    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseFirestore db;

    private FirebaseAuth auth;


    // =========================================================
    // STUDENTS
    // =========================================================

    private final List<DocumentSnapshot> allStudents =
            new ArrayList<>();

    private final List<DocumentSnapshot> filteredStudents =
            new ArrayList<>();


    // =========================================================
    // CLASS LIST
    // =========================================================

    private final List<String> classList =
            new ArrayList<>();


    // =========================================================
    // CURRENT FILTER
    // =========================================================

    private String selectedClass =
            "All Classes";


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_all_student_report_card
        );


        db =
                FirebaseFirestore.getInstance();

        auth =
                FirebaseAuth.getInstance();


        initViews();

        setupBackButton();

        setupSearch();

        setupClassSpinner();

        loadStudents();
    }


    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initViews() {

        btnBack =
                findViewById(
                        R.id.btnBack
                );


        spClassFilter =
                findViewById(
                        R.id.spClassFilter
                );


        etSearchStudent =
                findViewById(
                        R.id.etSearchStudent
                );


        tvStudentCount =
                findViewById(
                        R.id.tvStudentCount
                );


        tvNoStudents =
                findViewById(
                        R.id.tvNoStudents
                );


        studentContainer =
                findViewById(
                        R.id.studentContainer
                );
    }


    // =========================================================
    // BACK BUTTON
    // =========================================================

    private void setupBackButton() {

        btnBack.setOnClickListener(
                v -> finish()
        );
    }


    // =========================================================
    // SEARCH
    // =========================================================

    private void setupSearch() {

        etSearchStudent.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }


                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {

                        applyFilters();
                    }


                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );
    }


    // =========================================================
    // CLASS SPINNER
    // =========================================================

    private void setupClassSpinner() {

        classList.clear();

        classList.add(
                "All Classes"
        );


        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        classList
                );


        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );


        spClassFilter.setAdapter(
                adapter
        );


        spClassFilter.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {

                        if (position >= 0
                                && position < classList.size()) {

                            selectedClass =
                                    classList.get(position);

                            applyFilters();
                        }
                    }


                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent
                    ) {
                    }
                }
        );
    }


    // =========================================================
    // LOAD STUDENTS
    // =========================================================

    private void loadStudents() {

        if (auth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_SHORT
            ).show();

            showEmpty();

            return;
        }


        String schoolId =
                auth.getCurrentUser()
                        .getUid();


        db.collection("students")
                .whereEqualTo(
                        "schoolId",
                        schoolId
                )
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            allStudents.clear();


                            Set<String> uniqueClasses =
                                    new HashSet<>();


                            for (DocumentSnapshot doc :
                                    snapshot.getDocuments()) {

                                String status =
                                        safe(
                                                doc.getString(
                                                        "status"
                                                )
                                        );


                                // Do not show deleted students.
                                if ("Deleted".equalsIgnoreCase(
                                        status
                                )) {
                                    continue;
                                }


                                allStudents.add(
                                        doc
                                );


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


                            // Sort students alphabetically.
                            sortStudents();


                            // Build class spinner.
                            updateClassSpinner(
                                    uniqueClasses
                            );


                            // Display.
                            applyFilters();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            showEmpty();

                            Toast.makeText(
                                    this,
                                    "Failed to load students: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    // =========================================================
    // UPDATE CLASS SPINNER
    // =========================================================

    private void updateClassSpinner(
            Set<String> uniqueClasses
    ) {

        String oldSelection =
                selectedClass;


        classList.clear();

        classList.add(
                "All Classes"
        );


        List<String> sortedClasses =
                new ArrayList<>(
                        uniqueClasses
                );


        Collections.sort(
                sortedClasses,
                (a, b) ->
                        compareClasses(
                                a,
                                b
                        )
        );


        classList.addAll(
                sortedClasses
        );


        ArrayAdapter<String> adapter =
                (ArrayAdapter<String>)
                        spClassFilter.getAdapter();


        if (adapter != null) {

            adapter.notifyDataSetChanged();
        }


        // Restore previous selection.
        int position =
                classList.indexOf(
                        oldSelection
                );


        if (position < 0) {

            position = 0;

            selectedClass =
                    "All Classes";
        }


        spClassFilter.setSelection(
                position
        );
    }


    // =========================================================
    // APPLY SEARCH + CLASS FILTER
    // =========================================================

    private void applyFilters() {

        filteredStudents.clear();


        String search =
                safe(
                        etSearchStudent == null
                                ? ""
                                : etSearchStudent
                                .getText()
                                .toString()
                )
                        .toLowerCase(
                                Locale.ROOT
                        );


        for (DocumentSnapshot doc :
                allStudents) {

            String className =
                    safe(
                            doc.getString(
                                    "className"
                            )
                    );


            // Class filter.
            if (!"All Classes".equalsIgnoreCase(
                    selectedClass
            )) {

                if (!classMatches(
                        selectedClass,
                        className
                )) {
                    continue;
                }
            }


            String studentId =
                    getStudentId(doc);


            String firstName =
                    safe(
                            doc.getString(
                                    "firstName"
                            )
                    );


            String middleName =
                    safe(
                            doc.getString(
                                    "middleName"
                            )
                    );


            String lastName =
                    safe(
                            doc.getString(
                                    "lastName"
                            )
                    );


            String fullName =
                    buildFullName(
                            firstName,
                            middleName,
                            lastName
                    );


            String admissionNumber =
                    safe(
                            doc.getString(
                                    "admissionNumber"
                            )
                    );


            String rollNumber =
                    safe(
                            doc.getString(
                                    "rollNumber"
                            )
                    );


            String division =
                    safe(
                            doc.getString(
                                    "division"
                            )
                    );


            // Search through multiple fields.
            boolean matchesSearch =
                    search.isEmpty()
                            ||
                            fullName
                                    .toLowerCase(
                                            Locale.ROOT
                                    )
                                    .contains(search)
                            ||
                            studentId
                                    .toLowerCase(
                                            Locale.ROOT
                                    )
                                    .contains(search)
                            ||
                            admissionNumber
                                    .toLowerCase(
                                            Locale.ROOT
                                    )
                                    .contains(search)
                            ||
                            rollNumber
                                    .toLowerCase(
                                            Locale.ROOT
                                    )
                                    .contains(search)
                            ||
                            className
                                    .toLowerCase(
                                            Locale.ROOT
                                    )
                                    .contains(search)
                            ||
                            division
                                    .toLowerCase(
                                            Locale.ROOT
                                    )
                                    .contains(search);


            if (matchesSearch) {

                filteredStudents.add(
                        doc
                );
            }
        }


        displayStudents();
    }


    // =========================================================
    // DISPLAY STUDENTS
    // =========================================================

    private void displayStudents() {

        studentContainer.removeAllViews();


        int count =
                filteredStudents.size();


        tvStudentCount.setText(
                count + (
                        count == 1
                                ? " Student"
                                : " Students"
                )
        );


        if (count == 0) {

            tvNoStudents.setVisibility(
                    View.VISIBLE
            );

            return;
        }


        tvNoStudents.setVisibility(
                View.GONE
        );


        for (DocumentSnapshot student :
                filteredStudents) {

            studentContainer.addView(
                    createStudentCard(
                            student
                    )
            );
        }
    }


    // =========================================================
    // CREATE STUDENT CARD
    // =========================================================

    private View createStudentCard(
            DocumentSnapshot doc
    ) {

        MaterialCardView card =
                new MaterialCardView(this);


        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );


        cardParams.setMargins(
                dp(0),
                dp(0),
                dp(0),
                dp(12)
        );


        card.setLayoutParams(
                cardParams
        );


        card.setRadius(
                dp(16)
        );


        card.setCardElevation(
                dp(2)
        );


        card.setStrokeWidth(
                dp(1)
        );


        card.setStrokeColor(
                Color.parseColor(
                        "#E3DDF2"
                )
        );


        card.setCardBackgroundColor(
                Color.WHITE
        );


        LinearLayout main =
                new LinearLayout(this);


        main.setOrientation(
                LinearLayout.VERTICAL
        );


        main.setPadding(
                dp(16),
                dp(14),
                dp(16),
                dp(14)
        );


        // -----------------------------------------------------
        // STUDENT NAME
        // -----------------------------------------------------

        TextView tvName =
                new TextView(this);


        String fullName =
                buildFullName(
                        safe(
                                doc.getString(
                                        "firstName"
                                )
                        ),
                        safe(
                                doc.getString(
                                        "middleName"
                                )
                        ),
                        safe(
                                doc.getString(
                                        "lastName"
                                )
                        )
                );


        tvName.setText(
                fullName.isEmpty()
                        ? "Student"
                        : fullName
        );


        tvName.setTextSize(
                17
        );


        tvName.setTextColor(
                Color.parseColor(
                        "#24113F"
                )
        );


        tvName.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );


        // -----------------------------------------------------
        // CLASS / DIVISION
        // -----------------------------------------------------

        TextView tvClass =
                new TextView(this);


        String className =
                safe(
                        doc.getString(
                                "className"
                        )
                );


        String division =
                safe(
                        doc.getString(
                                "division"
                        )
                );


        String classText =
                "Class: "
                        + (
                        className.isEmpty()
                                ? "-"
                                : className
                )
                        + "    Division: "
                        + (
                        division.isEmpty()
                                ? "-"
                                : division
                );


        tvClass.setText(
                classText
        );


        tvClass.setTextSize(
                13
        );


        tvClass.setTextColor(
                Color.parseColor(
                        "#77727F"
                )
        );


        tvClass.setPadding(
                0,
                dp(5),
                0,
                0
        );


        // -----------------------------------------------------
        // ADMISSION / ROLL
        // -----------------------------------------------------

        TextView tvDetails =
                new TextView(this);


        String admissionNumber =
                safe(
                        doc.getString(
                                "admissionNumber"
                        )
                );


        String rollNumber =
                safe(
                        doc.getString(
                                "rollNumber"
                        )
                );


        tvDetails.setText(
                "Admission No: "
                        + (
                        admissionNumber.isEmpty()
                                ? "-"
                                : admissionNumber
                )
                        + "    Roll No: "
                        + (
                        rollNumber.isEmpty()
                                ? "-"
                                : rollNumber
                )
        );


        tvDetails.setTextSize(
                12
        );


        tvDetails.setTextColor(
                Color.parseColor(
                        "#88838F"
                )
        );


        tvDetails.setPadding(
                0,
                dp(3),
                0,
                0
        );


        // -----------------------------------------------------
        // REPORT BUTTON
        // -----------------------------------------------------

        TextView tvAction =
                new TextView(this);


        tvAction.setText(
                "VIEW REPORT CARD  →"
        );


        tvAction.setTextSize(
                13
        );


        tvAction.setTextColor(
                Color.parseColor(
                        "#5427D8"
                )
        );


        tvAction.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );


        tvAction.setGravity(
                Gravity.CENTER_VERTICAL
        );


        tvAction.setPadding(
                0,
                dp(12),
                0,
                dp(0)
        );


        // -----------------------------------------------------
        // ADD VIEWS
        // -----------------------------------------------------

        main.addView(
                tvName
        );

        main.addView(
                tvClass
        );

        main.addView(
                tvDetails
        );

        main.addView(
                tvAction
        );


        card.addView(
                main
        );


        // -----------------------------------------------------
        // CARD CLICK
        // -----------------------------------------------------

        card.setOnClickListener(
                v -> openReportCard(
                        doc
                )
        );


        tvAction.setOnClickListener(
                v -> openReportCard(
                        doc
                )
        );


        return card;
    }


    // =========================================================
    // OPEN EXISTING REPORT CARD
    // =========================================================

    private void openReportCard(
            DocumentSnapshot student
    ) {

        String studentId =
                getStudentId(
                        student
                );


        if (studentId.isEmpty()) {

            Toast.makeText(
                    this,
                    "Student ID not found.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        String schoolId =
                safe(
                        student.getString(
                                "schoolId"
                        )
                );


        Intent intent =
                new Intent(
                        this,
                        StudentReportCardActivity.class
                );


        intent.putExtra(
                "studentId",
                studentId
        );


        intent.putExtra(
                "schoolId",
                schoolId
        );


        startActivity(
                intent
        );
    }


    // =========================================================
    // SORT STUDENTS
    // =========================================================

    private void sortStudents() {

        Collections.sort(
                allStudents,
                (a, b) -> {

                    String nameA =
                            buildFullName(
                                    safe(
                                            a.getString(
                                                    "firstName"
                                            )
                                    ),
                                    safe(
                                            a.getString(
                                                    "middleName"
                                            )
                                    ),
                                    safe(
                                            a.getString(
                                                    "lastName"
                                            )
                                    )
                            );


                    String nameB =
                            buildFullName(
                                    safe(
                                            b.getString(
                                                    "firstName"
                                            )
                                    ),
                                    safe(
                                            b.getString(
                                                    "middleName"
                                            )
                                    ),
                                    safe(
                                            b.getString(
                                                    "lastName"
                                            )
                                    )
                            );


                    return nameA.compareToIgnoreCase(
                            nameB
                    );
                }
        );
    }


    // =========================================================
    // CLASS MATCHING
    // Handles:
    // "8"
    // "Class 8"
    // "Std 8"
    // "Standard 8"
    // =========================================================

    private boolean classMatches(
            String first,
            String second
    ) {

        return normalizeClass(
                first
        ).equals(
                normalizeClass(
                        second
                )
        );
    }


    private String normalizeClass(
            String value
    ) {

        String v =
                safe(value)
                        .toLowerCase(
                                Locale.ROOT
                        )
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
                        .replace(
                                "-",
                                ""
                        )
                        .trim();


        return v.replaceAll(
                "\\s+",
                ""
        );
    }


    // =========================================================
    // CLASS SORT
    // =========================================================

    private int compareClasses(
            String a,
            String b
    ) {

        String na =
                normalizeClass(a);

        String nb =
                normalizeClass(b);


        try {

            int ia =
                    Integer.parseInt(
                            na
                    );

            int ib =
                    Integer.parseInt(
                            nb
                    );

            return Integer.compare(
                    ia,
                    ib
            );

        } catch (Exception ignored) {

            return a.compareToIgnoreCase(
                    b
            );
        }
    }


    // =========================================================
    // STUDENT ID
    // =========================================================

    private String getStudentId(
            DocumentSnapshot doc
    ) {

        String studentId =
                safe(
                        doc.getString(
                                "studentId"
                        )
                );


        if (studentId.isEmpty()) {

            studentId =
                    doc.getId();
        }


        return studentId;
    }


    // =========================================================
    // FULL NAME
    // =========================================================

    private String buildFullName(
            String first,
            String middle,
            String last
    ) {

        String name =
                (
                        safe(first)
                                + " "
                                + safe(middle)
                                + " "
                                + safe(last)
                ).trim();


        return name.replaceAll(
                "\\s+",
                " "
        );
    }


    // =========================================================
    // EMPTY
    // =========================================================

    private void showEmpty() {

        allStudents.clear();

        filteredStudents.clear();

        studentContainer.removeAllViews();

        tvStudentCount.setText(
                "0 Students"
        );

        tvNoStudents.setVisibility(
                View.VISIBLE
        );
    }


    // =========================================================
    // SAFE
    // =========================================================

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value.trim();
    }


    // =========================================================
    // DP
    // =========================================================

    private int dp(
            int value
    ) {

        return Math.round(
                value
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }
}