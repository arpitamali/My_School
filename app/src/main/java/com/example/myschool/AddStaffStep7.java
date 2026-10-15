package com.example.myschool;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddStaffStep7 extends AppCompatActivity {

    private ImageButton btnBack;

    private ImageView ivStaffPhoto;

    private TextView tvStaffName;
    private TextView tvPersonalInfo;
    private TextView tvProfessionalInfo;
    private TextView tvRoleInfo;
    private TextView tvAddressInfo;
    private TextView tvLoginInfo;
    private TextView tvSubmitted;

    private CheckBox checkConfirm;

    private Button btnSubmit;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private boolean isSubmitting = false;
    private boolean isEdit = false;

    private String staffId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_add_staff_step7
        );

        db =
                FirebaseFirestore.getInstance();

        mAuth =
                FirebaseAuth.getInstance();

        isEdit =
                getIntent().getBooleanExtra(
                        "isEdit",
                        false
                );

        staffId =
                getIntent().getStringExtra(
                        "staffId"
                );

        initViews();
        loadReviewData();
        setupListeners();
        setupSystemBack();
    }

    private void initViews() {

        btnBack =
                findViewById(R.id.btnBack);

        ivStaffPhoto =
                findViewById(R.id.ivStaffPhoto);

        tvStaffName =
                findViewById(R.id.tvStaffName);

        tvPersonalInfo =
                findViewById(R.id.tvPersonalInfo);

        tvProfessionalInfo =
                findViewById(R.id.tvProfessionalInfo);

        tvRoleInfo =
                findViewById(R.id.tvRoleInfo);

        tvAddressInfo =
                findViewById(R.id.tvAddressInfo);

        tvLoginInfo =
                findViewById(R.id.tvLoginInfo);

        tvSubmitted =
                findViewById(R.id.tvSubmitted);

        checkConfirm =
                findViewById(R.id.checkConfirm);

        btnSubmit =
                findViewById(R.id.btnSubmit);
    }

    private void loadReviewData() {

        Intent intent =
                getIntent();

        String photo =
                getExtra(
                        intent,
                        "staffPhoto"
                );

        if (!photo.isEmpty()) {

            try {

                ivStaffPhoto.setImageURI(
                        Uri.parse(photo)
                );

            } catch (Exception e) {

                ivStaffPhoto.setImageResource(
                        android.R.drawable.ic_menu_camera
                );
            }

        } else {

            ivStaffPhoto.setImageResource(
                    android.R.drawable.ic_menu_camera
            );
        }

        String fullName =
                (
                        getExtra(intent, "firstName")
                                + " "
                                + getExtra(intent, "middleName")
                                + " "
                                + getExtra(intent, "lastName")
                ).trim();

        if (fullName.isEmpty()) {
            fullName = "Staff / Teacher";
        }

        tvStaffName.setText(
                fullName
        );

        tvPersonalInfo.setText(
                "Date of Birth: " +
                        display(intent, "dateOfBirth") +

                        "\nGender: " +
                        display(intent, "gender") +

                        "\nBlood Group: " +
                        display(intent, "bloodGroup") +

                        "\nAadhaar: " +
                        display(intent, "aadhaar") +

                        "\nBirth Place: " +
                        display(intent, "birthPlace") +

                        "\nNationality: " +
                        display(intent, "nationality") +

                        "\nMother Tongue: " +
                        display(intent, "motherTongue") +

                        "\nReligion: " +
                        display(intent, "religion") +

                        "\nCategory: " +
                        display(intent, "category") +

                        "\nCaste: " +
                        display(intent, "caste") +

                        "\nSub-Caste: " +
                        display(intent, "subCaste") +

                        "\nSpecial Needs: " +
                        display(intent, "specialNeeds")
        );

        tvProfessionalInfo.setText(
                "Employee ID: " +
                        display(intent, "employeeId") +

                        "\nJoining Date: " +
                        display(intent, "joiningDate") +

                        "\nQualification: " +
                        display(intent, "qualification") +

                        "\nExperience: " +
                        display(intent, "experience") +

                        "\nEmployment Type: " +
                        display(intent, "employmentType")
        );

        String staffType =
                display(intent, "staffType");

        if (
                staffType.equalsIgnoreCase("Teacher")
        ) {

            tvRoleInfo.setText(

                    "Staff Type: " +
                            staffType +

                            "\nRole: " +
                            display(intent, "role") +

                            "\nSubject: " +
                            display(intent, "subject") +

                            "\nAssigned Class: " +
                            display(intent, "assignedClass") +

                            "\nAssigned Division: " +
                            display(intent, "assignedDivision")
            );

        } else {

            tvRoleInfo.setText(

                    "Staff Type: " +
                            staffType +

                            "\nRole: " +
                            display(intent, "role")
            );
        }

        tvAddressInfo.setText(

                "Current Address:\n" +
                        display(
                                intent,
                                "currentAddress"
                        ) +

                        "\n\nPermanent Address:\n" +
                        display(
                                intent,
                                "permanentAddress"
                        ) +

                        "\n\nCity / Village: " +
                        display(
                                intent,
                                "city"
                        ) +

                        "\nDistrict: " +
                        display(
                                intent,
                                "district"
                        ) +

                        "\nState: " +
                        display(
                                intent,
                                "state"
                        ) +

                        "\nPIN Code: " +
                        display(
                                intent,
                                "pinCode"
                        ) +

                        "\n\nEmergency Contact: " +
                        display(
                                intent,
                                "emergencyName"
                        ) +

                        "\nRelation: " +
                        display(
                                intent,
                                "emergencyRelation"
                        ) +

                        "\nMobile: " +
                        display(
                                intent,
                                "emergencyMobile"
                        )
        );

        tvLoginInfo.setText(

                "Email: " +
                        display(
                                intent,
                                "staffEmail"
                        ) +

                        "\nMobile: " +
                        display(
                                intent,
                                "staffPhone"
                        ) +

                        "\nPassword: ••••••••"
        );
    }

    private void setupListeners() {

        btnBack.setOnClickListener(
                v -> finish()
        );

        btnSubmit.setOnClickListener(v -> {

            if (isSubmitting) {
                return;
            }

            if (!checkConfirm.isChecked()) {

                Toast.makeText(
                        this,
                        "Please confirm the information first",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (isEdit) {
                updateExistingStaff();
            } else {
                addNewStaff();
            }
        });
    }

    // =========================================================
    // ADD NEW STAFF
    // =========================================================

    private void addNewStaff() {

        if (
                mAuth == null
                        ||
                        mAuth.getCurrentUser() == null
        ) {

            Toast.makeText(
                    this,
                    "Admin login session expired.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        String schoolId =
                mAuth.getCurrentUser()
                        .getUid();

        Intent intent =
                getIntent();

        String email =
                getExtra(
                        intent,
                        "staffEmail"
                );

        String password =
                getExtra(
                        intent,
                        "staffPassword"
                );

        if (email.isEmpty()) {

            saveFailed(
                    "Staff email is required."
            );

            return;
        }

        if (password.isEmpty()) {

            saveFailed(
                    "Staff password is required."
            );

            return;
        }

        if (password.length() < 6) {

            saveFailed(
                    "Password must contain at least 6 characters."
            );

            return;
        }

        String employeeId =
                getExtra(
                        intent,
                        "employeeId"
                );

        if (employeeId.isEmpty()) {

            saveFailed(
                    "Employee ID is required."
            );

            return;
        }

        isSubmitting = true;

        btnSubmit.setEnabled(false);
        btnSubmit.setText(
                "ADDING STAFF..."
        );

        createStaffAuthAccount(
                schoolId,
                intent,
                employeeId,
                email,
                password
        );
    }

    private void createStaffAuthAccount(
            String schoolId,
            Intent intent,
            String staffId,
            String email,
            String password
    ) {

        FirebaseApp secondaryApp;

        try {

            secondaryApp =
                    FirebaseApp.getInstance(
                            "StaffAuthApp"
                    );

        } catch (IllegalStateException e) {

            FirebaseOptions options =
                    FirebaseApp.getInstance()
                            .getOptions();

            secondaryApp =
                    FirebaseApp.initializeApp(
                            this,
                            options,
                            "StaffAuthApp"
                    );
        }

        if (secondaryApp == null) {

            saveFailed(
                    "Unable to initialize staff login service."
            );

            return;
        }

        FirebaseAuth staffAuth =
                FirebaseAuth.getInstance(
                        secondaryApp
                );

        staffAuth
                .createUserWithEmailAndPassword(
                        email,
                        password
                )
                .addOnCompleteListener(
                        this,
                        task -> {

                            if (!task.isSuccessful()) {

                                String message =
                                        "Unable to create staff login account.";

                                if (
                                        task.getException() != null
                                                &&
                                                task.getException().getMessage() != null
                                ) {

                                    message =
                                            task.getException()
                                                    .getMessage();
                                }

                                saveFailed(message);

                                return;
                            }

                            if (
                                    task.getResult() == null
                                            ||
                                            task.getResult().getUser() == null
                            ) {

                                saveFailed(
                                        "Staff account was not created."
                                );

                                return;
                            }

                            String authUid =
                                    task.getResult()
                                            .getUser()
                                            .getUid();

                            Map<String, Object> staff =
                                    createStaffMap(
                                            intent
                                    );

                            staff.put(
                                    "staffId",
                                    staffId
                            );

                            staff.put(
                                    "schoolId",
                                    schoolId
                            );

                            staff.put(
                                    "authUid",
                                    authUid
                            );

                            staff.put(
                                    "active",
                                    true
                            );

                            staff.put(
                                    "status",
                                    "Active"
                            );

                            staff.put(
                                    "createdAt",
                                    com.google.firebase.firestore.FieldValue
                                            .serverTimestamp()
                            );

                            db.collection("staff")
                                    .document(staffId)
                                    .set(staff)
                                    .addOnCompleteListener(
                                            saveTask -> {

                                                staffAuth.signOut();

                                                if (!saveTask.isSuccessful()) {

                                                    String message =
                                                            "Staff save failed.";

                                                    if (
                                                            saveTask.getException() != null
                                                                    &&
                                                                    saveTask.getException()
                                                                            .getMessage() != null
                                                    ) {
                                                        message =
                                                                saveTask.getException()
                                                                        .getMessage();
                                                    }

                                                    saveFailed(message);

                                                    return;
                                                }

                                                staffAddedSuccessfully();
                                            }
                                    );
                        }
                );
    }

    // =========================================================
    // EDIT EXISTING STAFF
    // =========================================================

    private void updateExistingStaff() {

        if (
                staffId == null
                        ||
                        staffId.trim().isEmpty()
        ) {

            saveFailed(
                    "Staff ID is missing."
            );

            return;
        }

        isSubmitting = true;

        btnSubmit.setEnabled(false);

        btnSubmit.setText(
                "UPDATING STAFF..."
        );

        Intent intent =
                getIntent();

        Map<String, Object> update =
                createStaffMap(
                        intent
                );

        // Do NOT touch:
        // authUid
        // schoolId
        // createdAt
        // staffId

        update.put(
                "updatedAt",
                com.google.firebase.firestore.FieldValue
                        .serverTimestamp()
        );

        update.put(
                "active",
                true
        );

        update.put(
                "status",
                "Active"
        );

        db.collection("staff")
                .document(staffId)
                .update(update)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            this,
                            "Staff / Teacher updated successfully!",
                            Toast.LENGTH_LONG
                    ).show();

                    btnSubmit.setText(
                            "✓ UPDATED"
                    );

                    btnSubmit.postDelayed(
                            () -> {

                                Intent dashboard =
                                        new Intent(
                                                AddStaffStep7.this,
                                                AdminDashboardActivity.class
                                        );

                                dashboard.setFlags(
                                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                                                |
                                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                                );

                                startActivity(
                                        dashboard
                                );

                                finish();
                            },
                            800
                    );
                })
                .addOnFailureListener(e ->
                        saveFailed(
                                e.getMessage() != null
                                        ? e.getMessage()
                                        : "Staff update failed."
                        )
                );
    }

    // =========================================================
    // CREATE STAFF DATA
    // =========================================================

    private Map<String, Object> createStaffMap(
            Intent intent
    ) {

        Map<String, Object> map =
                new HashMap<>();

        put(
                map,
                intent,
                "staffPhoto"
        );

        put(
                map,
                intent,
                "firstName"
        );

        put(
                map,
                intent,
                "middleName"
        );

        put(
                map,
                intent,
                "lastName"
        );

        put(
                map,
                intent,
                "dateOfBirth"
        );

        put(
                map,
                intent,
                "gender"
        );

        put(
                map,
                intent,
                "bloodGroup"
        );

        put(
                map,
                intent,
                "aadhaar"
        );

        put(
                map,
                intent,
                "birthPlace"
        );

        put(
                map,
                intent,
                "nationality"
        );

        put(
                map,
                intent,
                "motherTongue"
        );

        put(
                map,
                intent,
                "religion"
        );

        put(
                map,
                intent,
                "category"
        );

        put(
                map,
                intent,
                "caste"
        );

        put(
                map,
                intent,
                "subCaste"
        );

        put(
                map,
                intent,
                "specialNeeds"
        );

        // Professional
        put(
                map,
                intent,
                "employeeId"
        );

        put(
                map,
                intent,
                "joiningDate"
        );

        put(
                map,
                intent,
                "qualification"
        );

        put(
                map,
                intent,
                "experience"
        );

        put(
                map,
                intent,
                "employmentType"
        );

        // Role
        put(
                map,
                intent,
                "staffType"
        );

        put(
                map,
                intent,
                "role"
        );

        put(
                map,
                intent,
                "subject"
        );

        put(
                map,
                intent,
                "assignedClass"
        );

        put(
                map,
                intent,
                "assignedDivision"
        );

        // Address
        put(
                map,
                intent,
                "currentAddress"
        );

        put(
                map,
                intent,
                "permanentAddress"
        );

        put(
                map,
                intent,
                "city"
        );

        put(
                map,
                intent,
                "district"
        );

        put(
                map,
                intent,
                "state"
        );

        put(
                map,
                intent,
                "pinCode"
        );

        // Emergency
        put(
                map,
                intent,
                "emergencyName"
        );

        put(
                map,
                intent,
                "emergencyRelation"
        );

        put(
                map,
                intent,
                "emergencyMobile"
        );

        // Login information
        put(
                map,
                intent,
                "staffEmail"
        );

        put(
                map,
                intent,
                "staffPhone"
        );

        // IMPORTANT:
        // Password is NOT stored in Firestore.

        return map;
    }

    private void put(
            Map<String, Object> map,
            Intent intent,
            String key
    ) {

        String value =
                getExtra(
                        intent,
                        key
                );

        if (!value.isEmpty()) {
            map.put(
                    key,
                    value
            );
        }
    }

    private String getExtra(
            Intent intent,
            String key
    ) {

        String value =
                intent.getStringExtra(
                        key
                );

        if (value == null) {
            return "";
        }

        return value.trim();
    }

    private String display(
            Intent intent,
            String key
    ) {

        String value =
                getExtra(
                        intent,
                        key
                );

        if (value.isEmpty()) {
            return "Not provided";
        }

        return value;
    }

    private void staffAddedSuccessfully() {

        isSubmitting = false;

        btnSubmit.setEnabled(true);

        btnSubmit.setText(
                "✓ STAFF ADDED"
        );

        tvSubmitted.setVisibility(
                View.VISIBLE
        );

        Toast.makeText(
                this,
                "Staff / Teacher added successfully!",
                Toast.LENGTH_LONG
        ).show();

        btnSubmit.postDelayed(
                () -> {

                    Intent intent =
                            new Intent(
                                    AddStaffStep7.this,
                                    AdminDashboardActivity.class
                            );

                    intent.setFlags(
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    |
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                    );

                    startActivity(intent);

                    finish();

                },
                1000
        );
    }

    private void saveFailed(
            String message
    ) {

        isSubmitting = false;

        btnSubmit.setEnabled(true);

        btnSubmit.setText(
                isEdit
                        ? "✓ UPDATE STAFF / TEACHER"
                        : "✓ ADD STAFF / TEACHER"
        );

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }

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