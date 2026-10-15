package com.example.myschool;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class AddStudentStep2 extends AppCompatActivity {

    private EditText etAdmissionNumber;
    private EditText etAdmissionDate;
    private EditText etAcademicYear;
    private EditText etRollNumber;
    private EditText etPreviousSchool;
    private EditText etPreviousClass;
    private EditText etLeavingCertificate;

    private Spinner spClass;
    private Spinner spDivision;

    private boolean editMode = false;
    private String studentId = "";

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_add_student_step2
        );

        db = FirebaseFirestore.getInstance();

        initViews();
        setupSpinners();
        setupDatePicker();
        setupListeners();
        setupSystemBack();

        checkEditMode();
    }

    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initViews() {

        etAdmissionNumber =
                findViewById(R.id.etAdmissionNumber);

        etAdmissionDate =
                findViewById(R.id.etAdmissionDate);

        etAcademicYear =
                findViewById(R.id.etAcademicYear);

        etRollNumber =
                findViewById(R.id.etRollNumber);

        etPreviousSchool =
                findViewById(R.id.etPreviousSchool);

        etPreviousClass =
                findViewById(R.id.etPreviousClass);

        etLeavingCertificate =
                findViewById(R.id.etLeavingCertificate);

        spClass =
                findViewById(R.id.spClass);

        spDivision =
                findViewById(R.id.spDivision);
    }

    // =========================================================
    // SPINNERS
    // =========================================================

    private void setupSpinners() {

        setSpinner(
                spClass,
                new String[]{
                        "Select Class",
                        "Nursery",
                        "LKG",
                        "UKG",
                        "1",
                        "2",
                        "3",
                        "4",
                        "5",
                        "6",
                        "7",
                        "8",
                        "9",
                        "10",
                        "11",
                        "12"
                }
        );

        setSpinner(
                spDivision,
                new String[]{
                        "Select Division",
                        "A",
                        "B",
                        "C",
                        "D",
                        "E"
                }
        );
    }

    private void setSpinner(
            Spinner spinner,
            String[] values) {

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        values
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinner.setAdapter(adapter);
    }

    // =========================================================
    // DATE PICKER
    // =========================================================

    private void setupDatePicker() {

        etAdmissionDate.setOnClickListener(v -> {

            Calendar c =
                    Calendar.getInstance();

            DatePickerDialog dialog =
                    new DatePickerDialog(
                            this,
                            (view, year, month, day) -> {

                                etAdmissionDate.setText(
                                        String.format(
                                                "%02d/%02d/%04d",
                                                day,
                                                month + 1,
                                                year
                                        )
                                );
                            },
                            c.get(Calendar.YEAR),
                            c.get(Calendar.MONTH),
                            c.get(Calendar.DAY_OF_MONTH)
                    );

            dialog.getDatePicker().setMaxDate(
                    System.currentTimeMillis()
            );

            dialog.show();
        });
    }

    // =========================================================
    // CHECK EDIT MODE
    // =========================================================

    private void checkEditMode() {

        Intent intent = getIntent();

        editMode =
                intent.getBooleanExtra(
                        "editMode",
                        false
                );

        studentId =
                intent.getStringExtra(
                        "studentId"
                );

        if (studentId == null) {
            studentId = "";
        }

        // If coming from Step 1 Edit Mode,
        // load academic data from Firestore.
        if (editMode && !studentId.isEmpty()) {

            loadAcademicData();

        } else {

            // Normal Add Student flow
            // Nothing needs to be loaded.
        }
    }

    // =========================================================
    // LOAD ACADEMIC DATA
    // =========================================================

    private void loadAcademicData() {

        db.collection("students")
                .document(studentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!documentSnapshot.exists()) {
                        return;
                    }

                    // -------------------------------------------------
                    // ADMISSION NUMBER
                    // -------------------------------------------------

                    etAdmissionNumber.setText(
                            getString(
                                    documentSnapshot,
                                    "admissionNumber"
                            )
                    );

                    // -------------------------------------------------
                    // ADMISSION DATE
                    // -------------------------------------------------

                    etAdmissionDate.setText(
                            getString(
                                    documentSnapshot,
                                    "admissionDate"
                            )
                    );

                    // -------------------------------------------------
                    // ACADEMIC YEAR
                    // -------------------------------------------------

                    etAcademicYear.setText(
                            getString(
                                    documentSnapshot,
                                    "academicYear"
                            )
                    );

                    // -------------------------------------------------
                    // ROLL NUMBER
                    // -------------------------------------------------

                    etRollNumber.setText(
                            getString(
                                    documentSnapshot,
                                    "rollNumber"
                            )
                    );

                    // -------------------------------------------------
                    // PREVIOUS SCHOOL
                    // -------------------------------------------------

                    etPreviousSchool.setText(
                            getString(
                                    documentSnapshot,
                                    "previousSchool"
                            )
                    );

                    // -------------------------------------------------
                    // PREVIOUS CLASS
                    // -------------------------------------------------

                    etPreviousClass.setText(
                            getString(
                                    documentSnapshot,
                                    "previousClass"
                            )
                    );

                    // -------------------------------------------------
                    // LEAVING CERTIFICATE
                    // -------------------------------------------------

                    etLeavingCertificate.setText(
                            getString(
                                    documentSnapshot,
                                    "leavingCertificate"
                            )
                    );

                    // -------------------------------------------------
                    // CLASS
                    // -------------------------------------------------

                    setSpinnerValue(
                            spClass,
                            getString(
                                    documentSnapshot,
                                    "className"
                            )
                    );

                    // -------------------------------------------------
                    // DIVISION
                    // -------------------------------------------------

                    setSpinnerValue(
                            spDivision,
                            getString(
                                    documentSnapshot,
                                    "division"
                            )
                    );
                });
    }

    // =========================================================
    // GET FIRESTORE STRING
    // =========================================================

    private String getString(
            com.google.firebase.firestore.DocumentSnapshot document,
            String key) {

        String value =
                document.getString(key);

        if (value == null) {
            return "";
        }

        return value;
    }

    // =========================================================
    // SET SPINNER VALUE
    // =========================================================

    private void setSpinnerValue(
            Spinner spinner,
            String value) {

        if (value == null ||
                value.trim().isEmpty()) {

            return;
        }

        ArrayAdapter adapter =
                (ArrayAdapter) spinner.getAdapter();

        if (adapter == null) {
            return;
        }

        int position =
                adapter.getPosition(value);

        if (position >= 0) {

            spinner.setSelection(
                    position
            );
        }
    }

    // =========================================================
    // LISTENERS
    // =========================================================

    private void setupListeners() {

        Button btnNext =
                findViewById(R.id.btnNext);

        ImageButton btnBack =
                findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v ->
                finish()
        );

        btnNext.setOnClickListener(v ->
                validateAndNext()
        );
    }

    // =========================================================
    // VALIDATE + NEXT
    // =========================================================

    private void validateAndNext() {

        String admissionNumber =
                etAdmissionNumber.getText()
                        .toString()
                        .trim();

        String admissionDate =
                etAdmissionDate.getText()
                        .toString()
                        .trim();

        String academicYear =
                etAcademicYear.getText()
                        .toString()
                        .trim();

        String className =
                spClass.getSelectedItem()
                        .toString();

        String division =
                spDivision.getSelectedItem()
                        .toString();

        // =====================================================
        // VALIDATION
        // =====================================================

        if (admissionNumber.isEmpty()) {

            etAdmissionNumber.setError(
                    "Enter admission number"
            );

            etAdmissionNumber.requestFocus();

            return;
        }

        if (admissionDate.isEmpty()) {

            etAdmissionDate.setError(
                    "Select admission date"
            );

            return;
        }

        if (academicYear.isEmpty()) {

            etAcademicYear.setError(
                    "Enter academic year"
            );

            etAcademicYear.requestFocus();

            return;
        }

        if (className.equals("Select Class")) {

            return;
        }

        if (division.equals("Select Division")) {

            return;
        }

        // =====================================================
        // OPEN STEP 3
        // =====================================================

        Intent oldIntent =
                getIntent();

        Intent intent =
                new Intent(
                        AddStudentStep2.this,
                        AddStudentStep3.class
                );

        // =====================================================
        // KEEP EDIT MODE
        // =====================================================

        intent.putExtra(
                "editMode",
                editMode
        );

        intent.putExtra(
                "studentId",
                studentId
        );

        // =====================================================
        // COPY STEP 1 DATA
        // =====================================================

        copyStudentData(
                oldIntent,
                intent
        );

        // =====================================================
        // STEP 2 DATA
        // =====================================================

        intent.putExtra(
                "admissionNumber",
                admissionNumber
        );

        intent.putExtra(
                "admissionDate",
                admissionDate
        );

        intent.putExtra(
                "academicYear",
                academicYear
        );

        intent.putExtra(
                "className",
                className
        );

        intent.putExtra(
                "division",
                division
        );

        intent.putExtra(
                "rollNumber",
                etRollNumber.getText()
                        .toString()
                        .trim()
        );

        intent.putExtra(
                "previousSchool",
                etPreviousSchool.getText()
                        .toString()
                        .trim()
        );

        intent.putExtra(
                "previousClass",
                etPreviousClass.getText()
                        .toString()
                        .trim()
        );

        intent.putExtra(
                "leavingCertificate",
                etLeavingCertificate.getText()
                        .toString()
                        .trim()
        );

        startActivity(intent);
    }

    // =========================================================
    // COPY STEP 1 DATA
    // =========================================================

    private void copyStudentData(
            Intent from,
            Intent to) {

        String[] keys = {

                "studentPhoto",

                "firstName",
                "middleName",
                "lastName",

                "dateOfBirth",
                "gender",
                "bloodGroup",
                "aadhaar",
                "birthPlace",
                "nationality",
                "motherTongue",
                "religion",
                "category",
                "caste",
                "subCaste",
                "specialNeeds"
        };

        for (String key : keys) {

            String value =
                    from.getStringExtra(key);

            if (value != null) {

                to.putExtra(
                        key,
                        value
                );
            }
        }
    }

    // =========================================================
    // SYSTEM BACK
    // =========================================================

    private void setupSystemBack() {

        getOnBackPressedDispatcher()
                .addCallback(
                        this,
                        new OnBackPressedCallback(true) {

                            @Override
                            public void handleOnBackPressed() {

                                finish();
                            }
                        }
                );
    }
}