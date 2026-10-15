package com.example.myschool;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class BonafideCertificateActivity extends AppCompatActivity {

    // =========================================================
    // VIEWS
    // =========================================================

    private Spinner spClassFilter;
    private Spinner spTemplate;

    private AutoCompleteTextView actvStudentSearch;

    private TextView tvStudentInfo;
    private TextView tvSignatureStatus;

    private EditText etCertificateNumber;
    private EditText etPurpose;
    private EditText etCertificateDate;
    private EditText etPrincipalName;

    private Button btnPreview;


    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;


    // =========================================================
    // STUDENTS
    // =========================================================

    private final List<DocumentSnapshot> students =
            new ArrayList<>();

    private final List<DocumentSnapshot> filteredStudents =
            new ArrayList<>();

    private final List<String> studentNames =
            new ArrayList<>();

    private final List<String> classList =
            new ArrayList<>();


    private ArrayAdapter<String> studentAdapter;
    private ArrayAdapter<String> classAdapter;


    private DocumentSnapshot selectedStudent;

    private String selectedClass = "All Classes";


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_bonafide_certificate
        );


        db =
                FirebaseFirestore.getInstance();

        mAuth =
                FirebaseAuth.getInstance();


        initViews();

        setupTemplates();

        setupDatePicker();

        setupClassFilter();

        setupStudentSearch();

        setupButtons();

        loadStudents();
    }


    // =========================================================
    // INIT VIEWS
    // =========================================================

    private void initViews() {

        spClassFilter =
                findViewById(
                        R.id.spClassFilter
                );


        spTemplate =
                findViewById(
                        R.id.spTemplate
                );


        actvStudentSearch =
                findViewById(
                        R.id.actvStudentSearch
                );


        tvStudentInfo =
                findViewById(
                        R.id.tvStudentInfo
                );


        tvSignatureStatus =
                findViewById(
                        R.id.tvSignatureStatus
                );


        etCertificateNumber =
                findViewById(
                        R.id.etCertificateNumber
                );


        etPurpose =
                findViewById(
                        R.id.etPurpose
                );


        etCertificateDate =
                findViewById(
                        R.id.etCertificateDate
                );


        etPrincipalName =
                findViewById(
                        R.id.etPrincipalName
                );


        btnPreview =
                findViewById(
                        R.id.btnPreview
                );


        ImageButton btnBack =
                findViewById(
                        R.id.btnBack
                );


        btnBack.setOnClickListener(
                v -> finish()
        );
    }


    // =========================================================
    // TEMPLATE
    // =========================================================

    private void setupTemplates() {

        String[] templates = {

                "Classic School",
                "Modern",
                "Elegant",
                "Formal",
                "Premium"
        };


        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        templates
                );


        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );


        spTemplate.setAdapter(
                adapter
        );
    }


    // =========================================================
    // DATE PICKER
    // =========================================================

    private void setupDatePicker() {

        etCertificateDate.setOnClickListener(
                v -> {

                    Calendar calendar =
                            Calendar.getInstance();


                    DatePickerDialog dialog =
                            new DatePickerDialog(
                                    this,

                                    (view, year, month, day) -> {

                                        etCertificateDate.setText(
                                                String.format(
                                                        "%02d/%02d/%04d",
                                                        day,
                                                        month + 1,
                                                        year
                                                )
                                        );
                                    },

                                    calendar.get(
                                            Calendar.YEAR
                                    ),

                                    calendar.get(
                                            Calendar.MONTH
                                    ),

                                    calendar.get(
                                            Calendar.DAY_OF_MONTH
                                    )
                            );


                    dialog.show();
                }
        );


        Calendar calendar =
                Calendar.getInstance();


        etCertificateDate.setText(
                String.format(
                        "%02d/%02d/%04d",

                        calendar.get(
                                Calendar.DAY_OF_MONTH
                        ),

                        calendar.get(
                                Calendar.MONTH
                        ) + 1,

                        calendar.get(
                                Calendar.YEAR
                        )
                )
        );
    }


    // =========================================================
    // CLASS FILTER
    // =========================================================

    private void setupClassFilter() {

        classList.clear();

        classList.add(
                "All Classes"
        );


        classAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        classList
                );


        classAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );


        spClassFilter.setAdapter(
                classAdapter
        );


        spClassFilter.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        if (position >= 0 &&
                                position < classList.size()) {

                            selectedClass =
                                    classList.get(position);

                            applyStudentFilter();
                        }
                    }


                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent) {

                        selectedClass =
                                "All Classes";

                        applyStudentFilter();
                    }
                }
        );
    }


    // =========================================================
    // STUDENT SEARCH
    // =========================================================

    private void setupStudentSearch() {

        actvStudentSearch.setThreshold(1);


        actvStudentSearch.setOnItemClickListener(
                (parent, view, position, id) -> {

                    String selectedName =
                            parent.getItemAtPosition(position)
                                    .toString();


                    DocumentSnapshot foundStudent =
                            findStudentByDisplayName(
                                    selectedName
                            );


                    if (foundStudent != null) {

                        selectedStudent =
                                foundStudent;

                        showStudentInfo();
                    }
                }
        );


        actvStudentSearch.setOnClickListener(
                v -> {

                    if (!studentNames.isEmpty()) {

                        actvStudentSearch.showDropDown();
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
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        String schoolId =
                mAuth.getCurrentUser()
                        .getUid();


        db.collection("students")
                .whereEqualTo(
                        "schoolId",
                        schoolId
                )
                .get()
                .addOnSuccessListener(
                        querySnapshot -> {

                            students.clear();


                            for (
                                    DocumentSnapshot document :
                                    querySnapshot.getDocuments()
                            ) {

                                String status =
                                        getString(
                                                document,
                                                "status"
                                        );


                                /*
                                 * Ignore deleted students
                                 */
                                if ("Deleted".equalsIgnoreCase(
                                        status
                                )) {
                                    continue;
                                }


                                students.add(
                                        document
                                );
                            }


                            /*
                             * Sort students alphabetically
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


                            applyStudentFilter();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    "Failed to load students:\n"
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    // =========================================================
    // CLASS LIST
    // =========================================================

    private void setupClassList() {

        Set<String> uniqueClasses =
                new LinkedHashSet<>();


        for (DocumentSnapshot document :
                students) {

            String className =
                    getString(
                            document,
                            "className"
                    );


            if (!className.isEmpty()) {

                uniqueClasses.add(
                        className
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

                            return a.compareToIgnoreCase(
                                    b
                            );
                        }
                    }
                }
        );


        classList.clear();

        classList.add(
                "All Classes"
        );

        classList.addAll(
                sortedClasses
        );


        classAdapter.notifyDataSetChanged();


        spClassFilter.setSelection(0);

        selectedClass =
                "All Classes";
    }


    // =========================================================
    // APPLY CLASS FILTER
    // =========================================================

    private void applyStudentFilter() {

        filteredStudents.clear();

        studentNames.clear();


        String currentSearch =
                actvStudentSearch
                        .getText()
                        .toString()
                        .trim()
                        .toLowerCase();


        for (DocumentSnapshot document :
                students) {

            String className =
                    getString(
                            document,
                            "className"
                    );


            boolean classMatches =
                    selectedClass.equals(
                            "All Classes"
                    )
                            ||
                            normalizeClass(
                                    className
                            ).equals(
                                    normalizeClass(
                                            selectedClass
                                    )
                            );


            if (!classMatches) {
                continue;
            }


            /*
             * Search by student name
             */
            String name =
                    getStudentName(
                            document
                    );


            String admissionNumber =
                    getString(
                            document,
                            "admissionNumber"
                    );


            String division =
                    getString(
                            document,
                            "division"
                    );


            boolean searchMatches =
                    currentSearch.isEmpty()
                            ||
                            name.toLowerCase()
                                    .contains(
                                            currentSearch
                                    )
                            ||
                            admissionNumber
                                    .toLowerCase()
                                    .contains(
                                            currentSearch
                                    )
                            ||
                            division
                                    .toLowerCase()
                                    .contains(
                                            currentSearch
                                    );


            if (!searchMatches) {
                continue;
            }


            filteredStudents.add(
                    document
            );


            /*
             * Display name in search dropdown
             */
            String displayName =
                    name;


            if (!admissionNumber.isEmpty()) {

                displayName =
                        name
                                + "  •  "
                                + admissionNumber;
            }


            studentNames.add(
                    displayName
            );
        }


        /*
         * Create search adapter
         */
        studentAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        studentNames
                );


        actvStudentSearch.setAdapter(
                studentAdapter
        );


        /*
         * If search text is entered,
         * show matching students.
         */
        if (!currentSearch.isEmpty() &&
                !studentNames.isEmpty()) {

            actvStudentSearch.post(
                    () -> actvStudentSearch.showDropDown()
            );
        }
    }


    // =========================================================
    // FIND STUDENT FROM SEARCH RESULT
    // =========================================================

    private DocumentSnapshot findStudentByDisplayName(
            String displayName) {

        for (int i = 0;
             i < studentNames.size();
             i++) {

            if (studentNames.get(i)
                    .equals(displayName)) {

                if (i < filteredStudents.size()) {

                    return filteredStudents.get(i);
                }
            }
        }


        return null;
    }


    // =========================================================
    // SHOW STUDENT INFO
    // =========================================================

    private void showStudentInfo() {

        if (selectedStudent == null) {

            tvStudentInfo.setText(
                    "Search and select a student"
            );

            return;
        }


        String name =
                getStudentName(
                        selectedStudent
                );


        String fatherName =
                getString(
                        selectedStudent,
                        "fatherName"
                );


        String className =
                getString(
                        selectedStudent,
                        "className"
                );


        String division =
                getString(
                        selectedStudent,
                        "division"
                );


        String admissionNumber =
                getString(
                        selectedStudent,
                        "admissionNumber"
                );


        String academicYear =
                getString(
                        selectedStudent,
                        "academicYear"
                );


        tvStudentInfo.setText(

                "Name: " +
                        display(name) +

                        "\nFather / Guardian: " +
                        display(fatherName) +

                        "\nClass: " +
                        display(className) +

                        "\nDivision: " +
                        display(division) +

                        "\nAdmission No: " +
                        display(admissionNumber) +

                        "\nAcademic Year: " +
                        display(academicYear)
        );
    }


    // =========================================================
    // BUTTONS
    // =========================================================

    private void setupButtons() {

        btnPreview.setOnClickListener(
                v -> {

                    if (selectedStudent == null) {

                        Toast.makeText(
                                this,
                                "Please search and select a student",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }


                    String certificateNumber =
                            etCertificateNumber
                                    .getText()
                                    .toString()
                                    .trim();


                    String purpose =
                            etPurpose
                                    .getText()
                                    .toString()
                                    .trim();


                    String date =
                            etCertificateDate
                                    .getText()
                                    .toString()
                                    .trim();


                    String principalName =
                            etPrincipalName
                                    .getText()
                                    .toString()
                                    .trim();


                    if (certificateNumber.isEmpty()) {

                        etCertificateNumber.setError(
                                "Enter certificate number"
                        );

                        etCertificateNumber.requestFocus();

                        return;
                    }


                    if (purpose.isEmpty()) {

                        etPurpose.setError(
                                "Enter purpose"
                        );

                        etPurpose.requestFocus();

                        return;
                    }


                    if (principalName.isEmpty()) {

                        etPrincipalName.setError(
                                "Enter principal name"
                        );

                        etPrincipalName.requestFocus();

                        return;
                    }


                    String template =
                            spTemplate
                                    .getSelectedItem()
                                    .toString();


                    openPreview(
                            certificateNumber,
                            purpose,
                            date,
                            principalName,
                            template
                    );
                }
        );
    }


    // =========================================================
    // PREVIEW
    // =========================================================

    private void openPreview(
            String certificateNumber,
            String purpose,
            String date,
            String principalName,
            String template) {

        if (selectedStudent == null) {
            return;
        }


        Intent intent =
                new Intent(
                        BonafideCertificateActivity.this,
                        BonafidePreviewActivity.class
                );


        intent.putExtra(
                "studentId",
                selectedStudent.getId()
        );


        intent.putExtra(
                "certificateNumber",
                certificateNumber
        );


        intent.putExtra(
                "purpose",
                purpose
        );


        intent.putExtra(
                "date",
                date
        );


        intent.putExtra(
                "principalName",
                principalName
        );


        intent.putExtra(
                "template",
                template
        );


        startActivity(intent);
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


        if (result.startsWith("class ")) {

            result =
                    result.substring(6)
                            .trim();
        }


        return result;
    }


    // =========================================================
    // GET STRING
    // =========================================================

    private String getString(
            DocumentSnapshot document,
            String key) {

        Object value =
                document.get(key);


        if (value == null) {

            return "";
        }


        return String.valueOf(
                value
        ).trim();
    }


    // =========================================================
    // DISPLAY
    // =========================================================

    private String display(
            String value) {

        if (value == null ||
                value.trim().isEmpty()) {

            return "Not provided";
        }


        return value;
    }
}