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

public class AddStudentStep4 extends AppCompatActivity {

    private EditText etCurrentAddress;
    private EditText etPermanentAddress;
    private EditText etCity;
    private EditText etDistrict;
    private EditText etState;
    private EditText etPinCode;

    private EditText etEmergencyName;
    private EditText etEmergencyRelation;
    private EditText etEmergencyMobile;

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
                R.layout.activity_add_student_step4
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

        etCurrentAddress =
                findViewById(
                        R.id.etCurrentAddress
                );

        etPermanentAddress =
                findViewById(
                        R.id.etPermanentAddress
                );

        etCity =
                findViewById(
                        R.id.etCity
                );

        etDistrict =
                findViewById(
                        R.id.etDistrict
                );

        etState =
                findViewById(
                        R.id.etState
                );

        etPinCode =
                findViewById(
                        R.id.etPinCode
                );


        etEmergencyName =
                findViewById(
                        R.id.etEmergencyName
                );

        etEmergencyRelation =
                findViewById(
                        R.id.etEmergencyRelation
                );

        etEmergencyMobile =
                findViewById(
                        R.id.etEmergencyMobile
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

            loadAddressData();
        }
    }


    // =========================================================
    // LOAD ADDRESS + EMERGENCY DATA
    // =========================================================

    private void loadAddressData() {

        db.collection("students")
                .document(studentId)
                .get()
                .addOnSuccessListener(
                        documentSnapshot -> {

                            if (!documentSnapshot.exists()) {

                                return;
                            }


                            // =========================================
                            // CURRENT ADDRESS
                            // =========================================

                            etCurrentAddress.setText(
                                    getString(
                                            documentSnapshot,
                                            "currentAddress"
                                    )
                            );


                            // =========================================
                            // PERMANENT ADDRESS
                            // =========================================

                            etPermanentAddress.setText(
                                    getString(
                                            documentSnapshot,
                                            "permanentAddress"
                                    )
                            );


                            // =========================================
                            // CITY
                            // =========================================

                            etCity.setText(
                                    getString(
                                            documentSnapshot,
                                            "city"
                                    )
                            );


                            // =========================================
                            // DISTRICT
                            // =========================================

                            etDistrict.setText(
                                    getString(
                                            documentSnapshot,
                                            "district"
                                    )
                            );


                            // =========================================
                            // STATE
                            // =========================================

                            String state =
                                    getString(
                                            documentSnapshot,
                                            "state"
                                    );

                            if (!state.isEmpty()) {

                                etState.setText(
                                        state
                                );
                            }


                            // =========================================
                            // PIN CODE
                            // =========================================

                            etPinCode.setText(
                                    getString(
                                            documentSnapshot,
                                            "pinCode"
                                    )
                            );


                            // =========================================
                            // EMERGENCY NAME
                            // =========================================

                            etEmergencyName.setText(
                                    getString(
                                            documentSnapshot,
                                            "emergencyName"
                                    )
                            );


                            // =========================================
                            // EMERGENCY RELATION
                            // =========================================

                            etEmergencyRelation.setText(
                                    getString(
                                            documentSnapshot,
                                            "emergencyRelation"
                                    )
                            );


                            // =========================================
                            // EMERGENCY MOBILE
                            // =========================================

                            etEmergencyMobile.setText(
                                    getString(
                                            documentSnapshot,
                                            "emergencyMobile"
                                    )
                            );
                        }
                );
    }


    // =========================================================
    // GET STRING FROM FIRESTORE
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


        // BACK

        btnBack.setOnClickListener(v ->
                finish()
        );


        // NEXT

        btnNext.setOnClickListener(v ->
                validateAndNext()
        );
    }


    // =========================================================
    // VALIDATE + NEXT
    // =========================================================

    private void validateAndNext() {

        String currentAddress =
                etCurrentAddress
                        .getText()
                        .toString()
                        .trim();


        String permanentAddress =
                etPermanentAddress
                        .getText()
                        .toString()
                        .trim();


        String city =
                etCity
                        .getText()
                        .toString()
                        .trim();


        String district =
                etDistrict
                        .getText()
                        .toString()
                        .trim();


        String state =
                etState
                        .getText()
                        .toString()
                        .trim();


        String pinCode =
                etPinCode
                        .getText()
                        .toString()
                        .trim();


        String emergencyName =
                etEmergencyName
                        .getText()
                        .toString()
                        .trim();


        String emergencyRelation =
                etEmergencyRelation
                        .getText()
                        .toString()
                        .trim();


        String emergencyMobile =
                etEmergencyMobile
                        .getText()
                        .toString()
                        .trim();


        // =====================================================
        // VALIDATION
        // =====================================================

        if (currentAddress.isEmpty()) {

            etCurrentAddress.setError(
                    "Enter current address"
            );

            etCurrentAddress.requestFocus();

            return;
        }


        if (!pinCode.isEmpty() &&
                pinCode.length() != 6) {

            etPinCode.setError(
                    "PIN code must be 6 digits"
            );

            etPinCode.requestFocus();

            return;
        }


        if (!emergencyMobile.isEmpty() &&
                emergencyMobile.length() != 10) {

            etEmergencyMobile.setError(
                    "Enter valid 10 digit mobile"
            );

            etEmergencyMobile.requestFocus();

            return;
        }


        // =====================================================
        // OPEN STEP 5
        // =====================================================

        Intent intent =
                new Intent(
                        AddStudentStep4.this,
                        AddStudentStep5.class
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
        // COPY STEP 1 + STEP 2 + STEP 3 DATA
        // =====================================================

        copyData(
                getIntent(),
                intent
        );


        // =====================================================
        // ADDRESS DATA
        // =====================================================

        put(
                intent,
                "currentAddress",
                currentAddress
        );

        put(
                intent,
                "permanentAddress",
                permanentAddress
        );

        put(
                intent,
                "city",
                city
        );

        put(
                intent,
                "district",
                district
        );

        put(
                intent,
                "state",
                state
        );

        put(
                intent,
                "pinCode",
                pinCode
        );


        // =====================================================
        // EMERGENCY DATA
        // =====================================================

        put(
                intent,
                "emergencyName",
                emergencyName
        );

        put(
                intent,
                "emergencyRelation",
                emergencyRelation
        );

        put(
                intent,
                "emergencyMobile",
                emergencyMobile
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
    // COPY PREVIOUS STEPS DATA
    // =========================================================

    private void copyData(
            Intent from,
            Intent to) {

        String[] keys = {

                // STEP 1
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

                // STEP 2
                "admissionNumber",
                "admissionDate",
                "academicYear",
                "className",
                "division",
                "rollNumber",
                "previousSchool",
                "previousClass",
                "leavingCertificate",

                // STEP 3
                "fatherName",
                "fatherMobile",
                "fatherEmail",
                "fatherOccupation",
                "motherName",
                "motherMobile",
                "motherEmail",
                "motherOccupation",
                "guardianName",
                "guardianRelation",
                "guardianMobile"
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