package com.example.myschool;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddStudentStep3 extends AppCompatActivity {

    private EditText etFatherName;
    private EditText etFatherMobile;
    private EditText etFatherEmail;
    private EditText etFatherOccupation;

    private EditText etMotherName;
    private EditText etMotherMobile;
    private EditText etMotherEmail;
    private EditText etMotherOccupation;

    private EditText etGuardianName;
    private EditText etGuardianRelation;
    private EditText etGuardianMobile;

    // =========================================================
    // EDIT MODE
    // =========================================================

    private boolean editMode = false;
    private String studentId = "";

    private FirebaseFirestore db;

    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_add_student_step3
        );

        db =
                FirebaseFirestore.getInstance();

        initViews();

        setupListeners();

        setupSystemBack();

        checkEditMode();
    }

    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initViews() {

        etFatherName =
                findViewById(
                        R.id.etFatherName
                );

        etFatherMobile =
                findViewById(
                        R.id.etFatherMobile
                );

        etFatherEmail =
                findViewById(
                        R.id.etFatherEmail
                );

        etFatherOccupation =
                findViewById(
                        R.id.etFatherOccupation
                );


        etMotherName =
                findViewById(
                        R.id.etMotherName
                );

        etMotherMobile =
                findViewById(
                        R.id.etMotherMobile
                );

        etMotherEmail =
                findViewById(
                        R.id.etMotherEmail
                );

        etMotherOccupation =
                findViewById(
                        R.id.etMotherOccupation
                );


        etGuardianName =
                findViewById(
                        R.id.etGuardianName
                );

        etGuardianRelation =
                findViewById(
                        R.id.etGuardianRelation
                );

        etGuardianMobile =
                findViewById(
                        R.id.etGuardianMobile
                );
    }

    // =========================================================
    // CHECK EDIT MODE
    // =========================================================

    private void checkEditMode() {

        Intent intent =
                getIntent();

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

        if (editMode &&
                !studentId.isEmpty()) {

            loadParentData();
        }
    }

    // =========================================================
    // LOAD PARENT DATA
    // =========================================================

    private void loadParentData() {

        db.collection("students")
                .document(studentId)
                .get()
                .addOnSuccessListener(
                        documentSnapshot -> {

                            if (!documentSnapshot.exists()) {
                                return;
                            }

                            // =========================================
                            // FATHER
                            // =========================================

                            etFatherName.setText(
                                    getString(
                                            documentSnapshot,
                                            "fatherName"
                                    )
                            );

                            etFatherMobile.setText(
                                    getString(
                                            documentSnapshot,
                                            "fatherMobile"
                                    )
                            );

                            etFatherEmail.setText(
                                    getString(
                                            documentSnapshot,
                                            "fatherEmail"
                                    )
                            );

                            etFatherOccupation.setText(
                                    getString(
                                            documentSnapshot,
                                            "fatherOccupation"
                                    )
                            );


                            // =========================================
                            // MOTHER
                            // =========================================

                            etMotherName.setText(
                                    getString(
                                            documentSnapshot,
                                            "motherName"
                                    )
                            );

                            etMotherMobile.setText(
                                    getString(
                                            documentSnapshot,
                                            "motherMobile"
                                    )
                            );

                            etMotherEmail.setText(
                                    getString(
                                            documentSnapshot,
                                            "motherEmail"
                                    )
                            );

                            etMotherOccupation.setText(
                                    getString(
                                            documentSnapshot,
                                            "motherOccupation"
                                    )
                            );


                            // =========================================
                            // GUARDIAN
                            // =========================================

                            etGuardianName.setText(
                                    getString(
                                            documentSnapshot,
                                            "guardianName"
                                    )
                            );

                            etGuardianRelation.setText(
                                    getString(
                                            documentSnapshot,
                                            "guardianRelation"
                                    )
                            );

                            etGuardianMobile.setText(
                                    getString(
                                            documentSnapshot,
                                            "guardianMobile"
                                    )
                            );
                        }
                );
    }

    // =========================================================
    // GET STRING
    // =========================================================

    private String getString(
            DocumentSnapshot document,
            String key) {

        String value =
                document.getString(key);

        if (value == null) {
            return "";
        }

        return value;
    }

    // =========================================================
    // LISTENERS
    // =========================================================

    private void setupListeners() {

        ImageButton btnBack =
                findViewById(
                        R.id.btnBack
                );

        Button btnNext =
                findViewById(
                        R.id.btnNext
                );


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

        String fatherName =
                etFatherName
                        .getText()
                        .toString()
                        .trim();

        String fatherMobile =
                etFatherMobile
                        .getText()
                        .toString()
                        .trim();

        String motherName =
                etMotherName
                        .getText()
                        .toString()
                        .trim();

        String motherMobile =
                etMotherMobile
                        .getText()
                        .toString()
                        .trim();


        // =====================================================
        // VALIDATION
        // =====================================================

        if (fatherName.isEmpty()) {

            etFatherName.setError(
                    "Enter father's name"
            );

            etFatherName.requestFocus();

            return;
        }


        if (fatherMobile.length() != 10) {

            etFatherMobile.setError(
                    "Enter valid 10 digit mobile"
            );

            etFatherMobile.requestFocus();

            return;
        }


        if (motherName.isEmpty()) {

            etMotherName.setError(
                    "Enter mother's name"
            );

            etMotherName.requestFocus();

            return;
        }


        if (motherMobile.length() != 10) {

            etMotherMobile.setError(
                    "Enter valid 10 digit mobile"
            );

            etMotherMobile.requestFocus();

            return;
        }


        // =====================================================
        // OPEN STEP 4
        // =====================================================

        Intent intent =
                new Intent(
                        AddStudentStep3.this,
                        AddStudentStep4.class
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
        // COPY STEP 1 + STEP 2 DATA
        // =====================================================

        copyData(
                getIntent(),
                intent
        );


        // =====================================================
        // FATHER
        // =====================================================

        put(
                intent,
                "fatherName",
                fatherName
        );

        put(
                intent,
                "fatherMobile",
                fatherMobile
        );

        put(
                intent,
                "fatherEmail",
                etFatherEmail
                        .getText()
                        .toString()
                        .trim()
        );

        put(
                intent,
                "fatherOccupation",
                etFatherOccupation
                        .getText()
                        .toString()
                        .trim()
        );


        // =====================================================
        // MOTHER
        // =====================================================

        put(
                intent,
                "motherName",
                motherName
        );

        put(
                intent,
                "motherMobile",
                motherMobile
        );

        put(
                intent,
                "motherEmail",
                etMotherEmail
                        .getText()
                        .toString()
                        .trim()
        );

        put(
                intent,
                "motherOccupation",
                etMotherOccupation
                        .getText()
                        .toString()
                        .trim()
        );


        // =====================================================
        // GUARDIAN
        // =====================================================

        put(
                intent,
                "guardianName",
                etGuardianName
                        .getText()
                        .toString()
                        .trim()
        );

        put(
                intent,
                "guardianRelation",
                etGuardianRelation
                        .getText()
                        .toString()
                        .trim()
        );

        put(
                intent,
                "guardianMobile",
                etGuardianMobile
                        .getText()
                        .toString()
                        .trim()
        );


        startActivity(intent);
    }

    // =========================================================
    // PUT DATA
    // =========================================================

    private void put(
            Intent intent,
            String key,
            String value) {

        intent.putExtra(
                key,
                value
        );
    }

    // =========================================================
    // COPY PREVIOUS DATA
    // =========================================================

    private void copyData(
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

                "specialNeeds",

                "admissionNumber",
                "admissionDate",
                "academicYear",

                "className",
                "division",
                "rollNumber",

                "previousSchool",
                "previousClass",
                "leavingCertificate"
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