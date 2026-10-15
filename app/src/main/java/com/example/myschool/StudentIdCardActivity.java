package com.example.myschool;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class StudentIdCardActivity extends AppCompatActivity {

    private LinearLayout studentContainer;
    private TextView tvNoStudents;
    private TextView tvStudentCount;

    private Spinner spClassFilter;
    private EditText etSearchStudent;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // All students from Firebase
    private final List<DocumentSnapshot> students = new ArrayList<>();

    // Classes for spinner
    private final List<String> classList = new ArrayList<>();

    private ArrayAdapter<String> classAdapter;

    private String selectedClass = "All Classes";
    private String searchText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_student_id_card);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupBack();
        setupClassSpinner();
        setupSearch();

        loadStudents();
    }


    // =========================================================
    // INIT VIEWS
    // =========================================================

    private void initViews() {

        studentContainer =
                findViewById(R.id.studentContainer);

        tvNoStudents =
                findViewById(R.id.tvNoStudents);

        tvStudentCount =
                findViewById(R.id.tvStudentCount);

        spClassFilter =
                findViewById(R.id.spClassFilter);

        etSearchStudent =
                findViewById(R.id.etSearchStudent);
    }


    // =========================================================
    // BACK BUTTON
    // =========================================================

    private void setupBack() {

        findViewById(R.id.btnBack)
                .setOnClickListener(v -> finish());
    }


    // =========================================================
    // CLASS SPINNER
    // =========================================================

    private void setupClassSpinner() {

        classList.clear();
        classList.add("All Classes");

        classAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                classList
        );

        classAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spClassFilter.setAdapter(classAdapter);

        spClassFilter.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        selectedClass =
                                classList.get(position);

                        applyFilters();
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent) {

                        selectedClass = "All Classes";

                        applyFilters();
                    }
                }
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
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        searchText =
                                s.toString()
                                        .trim()
                                        .toLowerCase();

                        applyFilters();
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s) {
                    }
                }
        );
    }


    // =========================================================
    // LOAD STUDENTS
    // =========================================================

    private void loadStudents() {

        if (mAuth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Admin login required",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String schoolId =
                mAuth.getCurrentUser().getUid();

        db.collection("students")
                .whereEqualTo("schoolId", schoolId)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    students.clear();

                    for (DocumentSnapshot document :
                            querySnapshot.getDocuments()) {

                        /*
                         * Ignore deleted students
                         */
                        String status =
                                getString(
                                        document,
                                        "status"
                                );

                        if ("Deleted".equalsIgnoreCase(status)) {
                            continue;
                        }

                        students.add(document);
                    }

                    /*
                     * Sort students by name
                     */
                    Collections.sort(
                            students,
                            new Comparator<DocumentSnapshot>() {
                                @Override
                                public int compare(
                                        DocumentSnapshot d1,
                                        DocumentSnapshot d2) {

                                    return getStudentName(d1)
                                            .compareToIgnoreCase(
                                                    getStudentName(d2)
                                            );
                                }
                            }
                    );

                    setupClassList();

                    applyFilters();

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Failed to load students",
                            Toast.LENGTH_SHORT
                    ).show();

                    tvNoStudents.setVisibility(
                            View.VISIBLE
                    );

                    tvStudentCount.setText(
                            "0 Students"
                    );
                });
    }


    // =========================================================
    // CREATE CLASS LIST
    // =========================================================

    private void setupClassList() {

        Set<String> uniqueClasses =
                new LinkedHashSet<>();

        for (DocumentSnapshot document : students) {

            String className =
                    getString(
                            document,
                            "className"
                    );

            if (!className.isEmpty()) {

                /*
                 * Remove "Class " prefix if already present
                 * so spinner remains clean.
                 */
                if (className
                        .toLowerCase()
                        .startsWith("class ")) {

                    className =
                            className.substring(6).trim();
                }

                uniqueClasses.add(className);
            }
        }

        List<String> sortedClasses =
                new ArrayList<>(uniqueClasses);

        Collections.sort(
                sortedClasses,
                new Comparator<String>() {
                    @Override
                    public int compare(
                            String a,
                            String b) {

                        try {

                            int numberA =
                                    Integer.parseInt(
                                            a.replaceAll(
                                                    "[^0-9]",
                                                    ""
                                            )
                                    );

                            int numberB =
                                    Integer.parseInt(
                                            b.replaceAll(
                                                    "[^0-9]",
                                                    ""
                                            )
                                    );

                            return Integer.compare(
                                    numberA,
                                    numberB
                            );

                        } catch (Exception e) {

                            return a.compareToIgnoreCase(b);
                        }
                    }
                }
        );

        classList.clear();

        classList.add("All Classes");

        classList.addAll(sortedClasses);

        classAdapter.notifyDataSetChanged();

        /*
         * Keep All Classes selected
         */
        spClassFilter.setSelection(0);
        selectedClass = "All Classes";
    }


    // =========================================================
    // APPLY CLASS + SEARCH FILTER
    // =========================================================

    private void applyFilters() {

        if (studentContainer == null) {
            return;
        }

        studentContainer.removeAllViews();

        int count = 0;

        for (DocumentSnapshot document : students) {

            String className =
                    getString(
                            document,
                            "className"
                    );

            String normalizedClass =
                    normalizeClass(className);

            /*
             * CLASS FILTER
             */
            boolean classMatches =
                    selectedClass.equals("All Classes")
                            || normalizedClass.equals(
                            normalizeClass(selectedClass)
                    );

            if (!classMatches) {
                continue;
            }


            /*
             * SEARCH FILTER
             */

            boolean searchMatches = true;

            if (!searchText.isEmpty()) {

                String name =
                        getStudentName(document)
                                .toLowerCase();

                String admissionNumber =
                        getString(
                                document,
                                "admissionNumber"
                        ).toLowerCase();

                String division =
                        getString(
                                document,
                                "division"
                        ).toLowerCase();

                String studentId =
                        document.getId()
                                .toLowerCase();

                String searchableText =
                        name + " "
                                + admissionNumber + " "
                                + normalizedClass.toLowerCase()
                                + " "
                                + division + " "
                                + studentId;

                searchMatches =
                        searchableText.contains(
                                searchText
                        );
            }


            if (!searchMatches) {
                continue;
            }


            /*
             * SHOW CARD
             */

            addStudentCard(document);

            count++;
        }


        /*
         * UPDATE COUNT
         */

        tvStudentCount.setText(
                count + " Students"
        );


        /*
         * EMPTY STATE
         */

        if (count == 0) {

            tvNoStudents.setVisibility(
                    View.VISIBLE
            );

        } else {

            tvNoStudents.setVisibility(
                    View.GONE
            );
        }
    }


    // =========================================================
    // STUDENT CARD
    // =========================================================

    private void addStudentCard(
            DocumentSnapshot document) {

        View card =
                LayoutInflater.from(this)
                        .inflate(
                                R.layout.item_student_id_card,
                                studentContainer,
                                false
                        );


        TextView tvName =
                card.findViewById(
                        R.id.tvStudentName
                );

        TextView tvClass =
                card.findViewById(
                        R.id.tvStudentClass
                );

        TextView tvAdmission =
                card.findViewById(
                        R.id.tvAdmissionNumber
                );


        String name =
                getStudentName(document);


        String className =
                getString(
                        document,
                        "className"
                );


        String division =
                getString(
                        document,
                        "division"
                );


        String admissionNumber =
                getString(
                        document,
                        "admissionNumber"
                );


        tvName.setText(name);


        if (className.isEmpty()) {

            tvClass.setText(
                    "Class - " + division
            );

        } else if (division.isEmpty()) {

            tvClass.setText(
                    "Class " + className
            );

        } else {

            tvClass.setText(
                    "Class " +
                            className +
                            " - " +
                            division
            );
        }


        tvAdmission.setText(
                "Admission No: " +
                        admissionNumber
        );


        // =====================================================
        // CLICK STUDENT
        // =====================================================

        card.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            StudentIdCardActivity.this,
                            StudentIdCardPreviewActivity.class
                    );

            intent.putExtra(
                    "studentId",
                    document.getId()
            );

            startActivity(intent);
        });


        studentContainer.addView(card);
    }


    // =========================================================
    // STUDENT NAME
    // =========================================================

    private String getStudentName(
            DocumentSnapshot document) {

        String firstName =
                getString(
                        document,
                        "firstName"
                );

        String middleName =
                getString(
                        document,
                        "middleName"
                );

        String lastName =
                getString(
                        document,
                        "lastName"
                );


        String name =
                (
                        firstName + " " +
                                middleName + " " +
                                lastName
                ).trim();


        if (name.isEmpty()) {
            return "Student";
        }


        return name;
    }


    // =========================================================
    // NORMALIZE CLASS
    // =========================================================

    private String normalizeClass(
            String value) {

        if (value == null) {
            return "";
        }

        String result =
                value.trim()
                        .toLowerCase();

        /*
         * Class 8 -> 8
         * class 8 -> 8
         * 8 -> 8
         */
        if (result.startsWith("class ")) {

            result =
                    result.substring(6).trim();
        }

        return result;
    }


    // =========================================================
    // FIRESTORE STRING
    // =========================================================

    private String getString(
            DocumentSnapshot document,
            String key) {

        Object value =
                document.get(key);

        if (value == null) {
            return "";
        }

        return String.valueOf(value).trim();
    }
}