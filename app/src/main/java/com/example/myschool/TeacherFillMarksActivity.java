package com.example.myschool;

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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TeacherFillMarksActivity extends AppCompatActivity {

    // =========================================================
    // VIEWS
    // =========================================================

    private Spinner spinnerClass;
    private Spinner spinnerDivision;

    private EditText etExamName;
    private EditText etSubject;
    private EditText etOutOf;

    private AppCompatImageButton btnBack;

    private Button btnSaveMarks;

    private ProgressBar progressBar;

    private RecyclerView recyclerStudents;


    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseFirestore db;
    private FirebaseAuth auth;


    // =========================================================
    // IDs
    // =========================================================

    private String staffId = "";
    private String schoolId = "";


    // =========================================================
    // LISTS
    // =========================================================

    private final List<String> classList =
            new ArrayList<>();

    private final List<String> divisionList =
            new ArrayList<>();

    private final List<MarksStudentModel> studentList =
            new ArrayList<>();


    // =========================================================
    // ADAPTERS
    // =========================================================

    private ArrayAdapter<String> classAdapter;

    private ArrayAdapter<String> divisionAdapter;

    private FillMarksAdapter marksAdapter;


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_teacher_fill_marks
        );

        db = FirebaseFirestore.getInstance();

        auth = FirebaseAuth.getInstance();

        staffId =
                getIntent().getStringExtra(
                        "staffId"
                );

        if (staffId == null) {
            staffId = "";
        }

        initializeViews();

        setupClassSpinner();

        setupDivisionSpinner();

        setupRecyclerView();

        setupClicks();

        loadTeacher();
    }


    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initializeViews() {

        btnBack =
                findViewById(
                        R.id.btnFillMarksBack
                );

        spinnerClass =
                findViewById(
                        R.id.spinnerFillMarksClass
                );

        spinnerDivision =
                findViewById(
                        R.id.spinnerFillMarksDivision
                );

        etExamName =
                findViewById(
                        R.id.etFillMarksExamName
                );

        etSubject =
                findViewById(
                        R.id.etFillMarksSubject
                );

        etOutOf =
                findViewById(
                        R.id.etFillMarksOutOf
                );

        btnSaveMarks =
                findViewById(
                        R.id.btnSaveAllMarks
                );

        progressBar =
                findViewById(
                        R.id.fillMarksProgressBar
                );

        recyclerStudents =
                findViewById(
                        R.id.recyclerFillMarksStudents
                );
    }


    // =========================================================
    // CLASS SPINNER
    // =========================================================

    private void setupClassSpinner() {

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

        spinnerClass.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        if (position < 0 ||
                                position >= classList.size()) {

                            return;
                        }

                        String selectedClass =
                                classList.get(position);

                        if (selectedClass.equals(
                                "Select Class")) {

                            clearDivisionList();

                            clearStudents();

                            return;
                        }

                        loadDivisions(
                                selectedClass
                        );
                    }


                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent) {

                    }
                }
        );
    }


    // =========================================================
    // DIVISION SPINNER
    // =========================================================

    private void setupDivisionSpinner() {

        divisionAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        divisionList
                );

        divisionAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerDivision.setAdapter(
                divisionAdapter
        );

        spinnerDivision.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        if (position < 0 ||
                                position >= divisionList.size()) {

                            return;
                        }

                        String selectedDivision =
                                divisionList.get(position);

                        if (selectedDivision.equals(
                                "Select Division")) {

                            clearStudents();

                            return;
                        }

                        if (spinnerClass.getSelectedItem()
                                == null) {

                            return;
                        }

                        String selectedClass =
                                spinnerClass
                                        .getSelectedItem()
                                        .toString();

                        if (selectedClass.equals(
                                "Select Class")) {

                            clearStudents();

                            return;
                        }

                        loadStudents(
                                selectedClass,
                                selectedDivision
                        );
                    }


                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent) {

                    }
                }
        );
    }


    // =========================================================
    // RECYCLER
    // =========================================================

    private void setupRecyclerView() {

        marksAdapter =
                new FillMarksAdapter(
                        this,
                        studentList
                );

        recyclerStudents.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerStudents.setAdapter(
                marksAdapter
        );
    }


    // =========================================================
    // CLICKS
    // =========================================================

    private void setupClicks() {

        btnBack.setOnClickListener(
                v -> finish()
        );

        btnSaveMarks.setOnClickListener(
                v -> saveAllMarks()
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
                                    documentSnapshot
                                            .getString(
                                                    "schoolId"
                                            );

                            if (schoolId == null) {
                                schoolId = "";
                            }

                            loadClasses();
                        }
                )
                .addOnFailureListener(
                        e -> Toast.makeText(
                                this,
                                "Unable to load teacher",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }


    // =========================================================
    // LOAD CLASSES
    // =========================================================

    private void loadClasses() {

        if (TextUtils.isEmpty(schoolId)) {
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
                                    querySnapshot
                                            .getDocuments()) {

                                String status =
                                        getStringValue(
                                                doc,
                                                "status"
                                        );

                                if (isInactiveStudent(
                                        status)) {

                                    continue;
                                }

                                String className =
                                        getStringValue(
                                                doc,
                                                "className"
                                        );

                                String normalized =
                                        normalizeClass(
                                                className
                                        );

                                if (!TextUtils.isEmpty(
                                        normalized)) {

                                    uniqueClasses.add(
                                            normalized
                                    );
                                }
                            }

                            List<String> sortedClasses =
                                    new ArrayList<>(
                                            uniqueClasses
                                    );

                            Collections.sort(
                                    sortedClasses,
                                    new Comparator<String>() {

                                        @Override
                                        public int compare(
                                                String a,
                                                String b) {

                                            Integer aNum =
                                                    getClassNumber(
                                                            a
                                                    );

                                            Integer bNum =
                                                    getClassNumber(
                                                            b
                                                    );

                                            if (aNum != null &&
                                                    bNum != null) {

                                                return Integer.compare(
                                                        aNum,
                                                        bNum
                                                );
                                            }

                                            return a.compareToIgnoreCase(
                                                    b
                                            );
                                        }
                                    }
                            );

                            classList.clear();

                            classList.add(
                                    "Select Class"
                            );

                            for (String item :
                                    sortedClasses) {

                                classList.add(
                                        "Class " + item
                                );
                            }

                            classAdapter.notifyDataSetChanged();

                            clearDivisionList();

                            clearStudents();
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
    // LOAD DIVISIONS
    // =========================================================

    private void loadDivisions(
            String selectedClass) {

        clearDivisionList();

        clearStudents();

        String normalizedClass =
                normalizeClass(
                        selectedClass
                );

        if (TextUtils.isEmpty(
                normalizedClass)) {

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

                            Set<String> uniqueDivisions =
                                    new HashSet<>();

                            for (DocumentSnapshot doc :
                                    querySnapshot
                                            .getDocuments()) {

                                String status =
                                        getStringValue(
                                                doc,
                                                "status"
                                        );

                                if (isInactiveStudent(
                                        status)) {

                                    continue;
                                }

                                String studentClass =
                                        getStringValue(
                                                doc,
                                                "className"
                                        );

                                if (!normalizedClass.equals(
                                        normalizeClass(
                                                studentClass
                                        ))) {

                                    continue;
                                }

                                String division =
                                        normalizeDivision(
                                                getStringValue(
                                                        doc,
                                                        "division"
                                                )
                                        );

                                if (!TextUtils.isEmpty(
                                        division)) {

                                    uniqueDivisions.add(
                                            division
                                    );
                                }
                            }

                            List<String> sorted =
                                    new ArrayList<>(
                                            uniqueDivisions
                                    );

                            Collections.sort(
                                    sorted,
                                    String.CASE_INSENSITIVE_ORDER
                            );

                            divisionList.clear();

                            divisionList.add(
                                    "Select Division"
                            );

                            divisionList.addAll(
                                    sorted
                            );

                            divisionAdapter.notifyDataSetChanged();

                            spinnerDivision.setSelection(
                                    0
                            );
                        }
                )
                .addOnFailureListener(
                        e -> Toast.makeText(
                                this,
                                "Unable to load divisions",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }


    // =========================================================
    // LOAD STUDENTS
    // =========================================================

    private void loadStudents(
            String selectedClass,
            String selectedDivision) {

        clearStudents();

        String normalizedClass =
                normalizeClass(
                        selectedClass
                );

        String normalizedDivision =
                normalizeDivision(
                        selectedDivision
                );

        if (TextUtils.isEmpty(
                normalizedClass) ||
                TextUtils.isEmpty(
                        normalizedDivision)) {

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

                            for (DocumentSnapshot doc :
                                    querySnapshot
                                            .getDocuments()) {

                                String status =
                                        getStringValue(
                                                doc,
                                                "status"
                                        );

                                if (isInactiveStudent(
                                        status)) {

                                    continue;
                                }

                                String studentClass =
                                        getStringValue(
                                                doc,
                                                "className"
                                        );

                                String studentDivision =
                                        getStringValue(
                                                doc,
                                                "division"
                                        );

                                if (!normalizedClass.equals(
                                        normalizeClass(
                                                studentClass
                                        ))) {

                                    continue;
                                }

                                if (!normalizedDivision.equals(
                                        normalizeDivision(
                                                studentDivision
                                        ))) {

                                    continue;
                                }


                                MarksStudentModel student =
                                        new MarksStudentModel();


                                student.studentId =
                                        getStringValue(
                                                doc,
                                                "studentId"
                                        );

                                if (TextUtils.isEmpty(
                                        student.studentId)) {

                                    student.studentId =
                                            doc.getId();
                                }


                                student.studentName =
                                        buildFullName(
                                                getStringValue(
                                                        doc,
                                                        "firstName"
                                                ),
                                                getStringValue(
                                                        doc,
                                                        "middleName"
                                                ),
                                                getStringValue(
                                                        doc,
                                                        "lastName"
                                                )
                                        );


                                student.rollNumber =
                                        getStringValue(
                                                doc,
                                                "rollNumber"
                                        );


                                student.className =
                                        studentClass;


                                student.division =
                                        studentDivision;


                                studentList.add(
                                        student
                                );
                            }

                            sortStudents();

                            marksAdapter.notifyDataSetChanged();

                            if (studentList.isEmpty()) {

                                Toast.makeText(
                                        this,
                                        "No students found",
                                        Toast.LENGTH_SHORT
                                ).show();

                            } else {

                                Toast.makeText(
                                        this,
                                        studentList.size()
                                                + " students found",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                )
                .addOnFailureListener(
                        e -> Toast.makeText(
                                this,
                                "Unable to load students",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }


    // =========================================================
    // SAVE ALL MARKS
    // =========================================================

    private void saveAllMarks() {

        String examName =
                etExamName.getText()
                        .toString()
                        .trim();

        String subject =
                etSubject.getText()
                        .toString()
                        .trim();

        String outOfText =
                etOutOf.getText()
                        .toString()
                        .trim();


        // =====================================================
        // VALIDATION
        // =====================================================

        if (spinnerClass.getSelectedItem() == null ||
                spinnerClass.getSelectedItem()
                        .toString()
                        .equals("Select Class")) {

            Toast.makeText(
                    this,
                    "Please select class",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        if (spinnerDivision.getSelectedItem() == null ||
                spinnerDivision.getSelectedItem()
                        .toString()
                        .equals("Select Division")) {

            Toast.makeText(
                    this,
                    "Please select division",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        if (examName.isEmpty()) {

            etExamName.setError(
                    "Enter exam name"
            );

            etExamName.requestFocus();

            return;
        }


        if (subject.isEmpty()) {

            etSubject.setError(
                    "Enter subject"
            );

            etSubject.requestFocus();

            return;
        }


        Double outOf =
                parseNumber(
                        outOfText
                );

        if (outOf == null ||
                outOf <= 0) {

            etOutOf.setError(
                    "Enter valid Out Of marks"
            );

            etOutOf.requestFocus();

            return;
        }


        if (studentList.isEmpty()) {

            Toast.makeText(
                    this,
                    "No students found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // =====================================================
        // VALIDATE STUDENT MARKS
        // =====================================================

        for (MarksStudentModel student :
                studentList) {

            if (TextUtils.isEmpty(
                    student.marks)) {

                Toast.makeText(
                        this,
                        "Enter marks for "
                                + student.studentName,
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            Double obtained =
                    parseNumber(
                            student.marks
                    );

            if (obtained == null) {

                Toast.makeText(
                        this,
                        "Invalid marks for "
                                + student.studentName,
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (obtained < 0 ||
                    obtained > outOf) {

                Toast.makeText(
                        this,
                        student.studentName
                                + ": Marks must be between 0 and "
                                + formatNumber(outOf),
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }
        }


        // =====================================================
        // SAVE
        // =====================================================

        progressBar.setVisibility(
                View.VISIBLE
        );

        btnSaveMarks.setEnabled(
                false
        );

        btnSaveMarks.setText(
                "Saving..."
        );


        WriteBatch batch =
                db.batch();


        for (MarksStudentModel student :
                studentList) {

            Double obtained =
                    parseNumber(
                            student.marks
                    );


            double percentage =
                    obtained * 100.0 / outOf;


            String grade =
                    calculateGrade(
                            percentage
                    );


            // Unique document ID
            String normalizedExam =
                    examName
                            .toLowerCase(
                                    Locale.ROOT
                            )
                            .replaceAll(
                                    "[^a-z0-9]+",
                                    "_"
                            );

            String normalizedSubject =
                    subject
                            .toLowerCase(
                                    Locale.ROOT
                            )
                            .replaceAll(
                                    "[^a-z0-9]+",
                                    "_"
                            );


            String documentId =
                    schoolId
                            + "_"
                            + student.studentId
                            + "_"
                            + normalizedExam
                            + "_"
                            + normalizedSubject;


            HashMap<String, Object> markData =
                    new HashMap<>();


            // -------------------------------------------------
            // COMMON FIELDS
            // -------------------------------------------------

            markData.put(
                    "schoolId",
                    schoolId
            );

            markData.put(
                    "studentId",
                    student.studentId
            );

            markData.put(
                    "studentName",
                    student.studentName
            );

            markData.put(
                    "rollNumber",
                    student.rollNumber
            );

            markData.put(
                    "className",
                    student.className
            );

            markData.put(
                    "division",
                    student.division
            );


            // -------------------------------------------------
            // EXAM
            // -------------------------------------------------

            markData.put(
                    "examName",
                    examName
            );

            markData.put(
                    "subject",
                    subject
            );


            // -------------------------------------------------
            // MARKS
            // -------------------------------------------------

            markData.put(
                    "maxMarks",
                    outOf
            );

            markData.put(
                    "obtainedMarks",
                    obtained
            );

            markData.put(
                    "percentage",
                    percentage
            );

            markData.put(
                    "grade",
                    grade
            );


            // -------------------------------------------------
            // TEACHER
            // -------------------------------------------------

            markData.put(
                    "teacherId",
                    staffId
            );

            if (auth.getCurrentUser() != null) {

                markData.put(
                        "teacherUid",
                        auth.getCurrentUser()
                                .getUid()
                );

            } else {

                markData.put(
                        "teacherUid",
                        ""
                );
            }


            markData.put(
                    "updatedAt",
                    FieldValue.serverTimestamp()
            );


            batch.set(
                    db.collection(
                                    "studentMarks"
                            )
                            .document(
                                    documentId
                            ),
                    markData
            );
        }


        // =====================================================
        // COMMIT
        // =====================================================

        batch.commit()
                .addOnSuccessListener(
                        unused -> {

                            progressBar.setVisibility(
                                    View.GONE
                            );

                            btnSaveMarks.setEnabled(
                                    true
                            );

                            btnSaveMarks.setText(
                                    "Save All Marks"
                            );


                            Toast.makeText(
                                    this,
                                    "All marks saved successfully",
                                    Toast.LENGTH_SHORT
                            ).show();


                            etExamName.setText("");

                            etSubject.setText("");

                            etOutOf.setText("");


                            for (MarksStudentModel student :
                                    studentList) {

                                student.marks = "";
                            }


                            marksAdapter.notifyDataSetChanged();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            progressBar.setVisibility(
                                    View.GONE
                            );

                            btnSaveMarks.setEnabled(
                                    true
                            );

                            btnSaveMarks.setText(
                                    "Save All Marks"
                            );


                            Toast.makeText(
                                    this,
                                    "Failed to save marks: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    // =========================================================
    // GRADE
    // =========================================================

    private String calculateGrade(
            double percentage) {

        if (percentage >= 90) {
            return "A+";
        }

        if (percentage >= 80) {
            return "A";
        }

        if (percentage >= 70) {
            return "B+";
        }

        if (percentage >= 60) {
            return "B";
        }

        if (percentage >= 50) {
            return "C";
        }

        if (percentage >= 35) {
            return "D";
        }

        return "F";
    }


    // =========================================================
    // PARSE NUMBER
    // =========================================================

    private Double parseNumber(
            String value) {

        try {

            if (value == null ||
                    value.trim().isEmpty()) {

                return null;
            }

            return Double.parseDouble(
                    value.trim()
            );

        } catch (Exception e) {

            return null;
        }
    }


    // =========================================================
    // FORMAT NUMBER
    // =========================================================

    private String formatNumber(
            double value) {

        if (value == (long) value) {

            return String.format(
                    Locale.getDefault(),
                    "%d",
                    (long) value
            );
        }

        return String.format(
                Locale.getDefault(),
                "%.2f",
                value
        );
    }


    // =========================================================
    // CLEAR STUDENTS
    // =========================================================

    private void clearStudents() {

        studentList.clear();

        if (marksAdapter != null) {

            marksAdapter.notifyDataSetChanged();
        }
    }


    // =========================================================
    // CLEAR DIVISION
    // =========================================================

    private void clearDivisionList() {

        divisionList.clear();

        divisionList.add(
                "Select Division"
        );

        if (divisionAdapter != null) {

            divisionAdapter.notifyDataSetChanged();
        }

        if (spinnerDivision != null) {

            spinnerDivision.setSelection(
                    0
            );
        }
    }


    // =========================================================
    // INACTIVE STUDENT
    // =========================================================

    private boolean isInactiveStudent(
            String status) {

        if (status == null) {
            return false;
        }

        return status.equalsIgnoreCase(
                "Deleted"
        )
                ||
                status.equalsIgnoreCase(
                        "Left School"
                );
    }


    // =========================================================
    // NORMALIZE CLASS
    // =========================================================

    private String normalizeClass(
            String value) {

        if (value == null) {
            return "";
        }

        String text =
                value.trim()
                        .toLowerCase();

        if (text.isEmpty()) {
            return "";
        }


        text = text.replace(
                "standard",
                ""
        );

        text = text.replace(
                "std.",
                ""
        );

        text = text.replace(
                "std",
                ""
        );

        text = text.trim();


        if (text.startsWith("class")) {

            text = text.substring(
                    5
            ).trim();
        }


        // Remove common suffix
        if (text.endsWith("th") ||
                text.endsWith("st") ||
                text.endsWith("nd") ||
                text.endsWith("rd")) {

            text = text.substring(
                    0,
                    text.length() - 2
            );
        }


        StringBuilder digits =
                new StringBuilder();

        for (int i = 0;
             i < text.length();
             i++) {

            char c =
                    text.charAt(i);

            if (Character.isDigit(c)) {

                digits.append(c);
            }
        }


        if (digits.length() > 0) {

            try {

                return String.valueOf(
                        Integer.parseInt(
                                digits.toString()
                        )
                );

            } catch (Exception ignored) {

                return digits.toString();
            }
        }


        return text.trim();
    }


    // =========================================================
    // NORMALIZE DIVISION
    // =========================================================

    private String normalizeDivision(
            String value) {

        if (value == null) {
            return "";
        }

        String text =
                value.trim();

        if (text.isEmpty()) {
            return "";
        }


        text = text
                .replace(
                        "Division",
                        ""
                )
                .replace(
                        "division",
                        ""
                )
                .replace(
                        "Div.",
                        ""
                )
                .replace(
                        "div.",
                        ""
                )
                .replace(
                        "Div",
                        ""
                )
                .replace(
                        "div",
                        ""
                )
                .trim();


        return text.toUpperCase(
                Locale.ROOT
        );
    }


    // =========================================================
    // GET CLASS NUMBER
    // =========================================================

    private Integer getClassNumber(
            String className) {

        String normalized =
                normalizeClass(
                        className
                );

        if (TextUtils.isEmpty(
                normalized)) {

            return null;
        }

        try {

            return Integer.parseInt(
                    normalized
            );

        } catch (Exception e) {

            return null;
        }
    }


    // =========================================================
    // GET STRING
    // =========================================================

    private String getStringValue(
            DocumentSnapshot doc,
            String field) {

        String value =
                doc.getString(field);

        return value == null
                ? ""
                : value;
    }


    // =========================================================
    // BUILD NAME
    // =========================================================

    private String buildFullName(
            String first,
            String middle,
            String last) {

        StringBuilder name =
                new StringBuilder();


        if (!TextUtils.isEmpty(first)) {

            name.append(
                    first.trim()
            );
        }


        if (!TextUtils.isEmpty(middle)) {

            if (name.length() > 0) {

                name.append(" ");
            }

            name.append(
                    middle.trim()
            );
        }


        if (!TextUtils.isEmpty(last)) {

            if (name.length() > 0) {

                name.append(" ");
            }

            name.append(
                    last.trim()
            );
        }


        if (name.length() == 0) {

            return "Student";
        }


        return name.toString();
    }


    // =========================================================
    // SORT STUDENTS
    // =========================================================

    private void sortStudents() {

        studentList.sort(
                (a, b) -> {

                    try {

                        int rollA =
                                Integer.parseInt(
                                        a.rollNumber
                                                .trim()
                                );

                        int rollB =
                                Integer.parseInt(
                                        b.rollNumber
                                                .trim()
                                );

                        return Integer.compare(
                                rollA,
                                rollB
                        );

                    } catch (Exception e) {

                        return a.studentName
                                .compareToIgnoreCase(
                                        b.studentName
                                );
                    }
                }
        );
    }


    // =========================================================
    // MODEL
    // =========================================================

    public static class MarksStudentModel {

        String studentId = "";

        String studentName = "";

        String rollNumber = "";

        String className = "";

        String division = "";

        String marks = "";
    }
}