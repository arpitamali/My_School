package com.example.myschool;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class StudentDetailsActivity
        extends AppCompatActivity {


    // =========================================================
    // VIEWS
    // =========================================================

    private ImageButton btnBack;

    private ImageView ivStudentPhoto;

    private TextView tvStudentName;

    private TextView tvPersonalInfo;

    private TextView tvAcademicInfo;

    private TextView tvParentInfo;

    private TextView tvAddressInfo;

    private TextView tvFeesInfo;

    private TextView tvLoginInfo;

    private Button btnEditStudent;


    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseFirestore db;


    // =========================================================
    // STUDENT ID
    // =========================================================

    private String studentId = "";


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(
            Bundle savedInstanceState) {

        super.onCreate(
                savedInstanceState
        );


        setContentView(
                R.layout.activity_student_details
        );


        db =
                FirebaseFirestore
                        .getInstance();


        initViews();


        getStudentId();


        setupBackButton();


        setupEditButton();


        loadStudentData();
    }


    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initViews() {

        btnBack =
                findViewById(
                        R.id.btnBack
                );


        ivStudentPhoto =
                findViewById(
                        R.id.ivStudentPhoto
                );


        tvStudentName =
                findViewById(
                        R.id.tvStudentName
                );


        tvPersonalInfo =
                findViewById(
                        R.id.tvPersonalInfo
                );


        tvAcademicInfo =
                findViewById(
                        R.id.tvAcademicInfo
                );


        tvParentInfo =
                findViewById(
                        R.id.tvParentInfo
                );


        tvAddressInfo =
                findViewById(
                        R.id.tvAddressInfo
                );


        tvFeesInfo =
                findViewById(
                        R.id.tvFeesInfo
                );


        tvLoginInfo =
                findViewById(
                        R.id.tvLoginInfo
                );


        btnEditStudent =
                findViewById(
                        R.id.btnEditStudent
                );
    }


    // =========================================================
    // GET STUDENT ID
    // =========================================================

    private void getStudentId() {

        Intent intent =
                getIntent();


        if (intent == null) {

            studentId = "";

            return;
        }


        studentId =
                intent.getStringExtra(
                        "studentId"
                );


        if (studentId == null) {

            studentId = "";
        }


        studentId =
                studentId.trim();
    }


    // =========================================================
    // BACK
    // =========================================================

    private void setupBackButton() {

        btnBack.setOnClickListener(
                v -> finish()
        );
    }


    // =========================================================
    // EDIT BUTTON
    // =========================================================

    private void setupEditButton() {

        btnEditStudent.setOnClickListener(
                v -> {


                    if (studentId == null ||
                            studentId.isEmpty()) {


                        Toast.makeText(
                                StudentDetailsActivity.this,
                                "Student ID missing",
                                Toast.LENGTH_SHORT
                        ).show();


                        return;
                    }


                    Intent intent =
                            new Intent(
                                    StudentDetailsActivity.this,
                                    AddStudentStep1.class
                            );


                    // IMPORTANT:
                    // EDIT MODE

                    intent.putExtra(
                            "editMode",
                            true
                    );


                    // IMPORTANT:
                    // SAME STUDENT ID

                    intent.putExtra(
                            "studentId",
                            studentId
                    );


                    startActivity(
                            intent
                    );
                }
        );
    }


    // =========================================================
    // LOAD STUDENT DATA
    // =========================================================

    private void loadStudentData() {


        if (studentId == null ||
                studentId.isEmpty()) {


            Toast.makeText(
                    this,
                    "Student information missing",
                    Toast.LENGTH_LONG
            ).show();


            return;
        }


        db.collection("students")
                .document(studentId)
                .get()
                .addOnSuccessListener(
                        document -> {


                            if (!document.exists()) {


                                Toast.makeText(
                                        StudentDetailsActivity.this,
                                        "Student not found",
                                        Toast.LENGTH_LONG
                                ).show();


                                return;
                            }


                            showStudentData(
                                    document
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {


                            Toast.makeText(
                                    StudentDetailsActivity.this,
                                    "Failed to load student:\n" +
                                            e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    // =========================================================
    // SHOW ALL STUDENT DATA
    // =========================================================

    private void showStudentData(
            DocumentSnapshot document) {


        // =====================================================
        // PHOTO
        // =====================================================

        String photo =
                getString(
                        document,
                        "studentPhoto"
                );


        if (!photo.isEmpty()) {


            try {


                if (photo.startsWith(
                        "http://"
                ) ||
                        photo.startsWith(
                                "https://"
                        )) {


                    Glide.with(
                                    StudentDetailsActivity.this
                            )
                            .load(photo)
                            .placeholder(
                                    android.R.drawable
                                            .ic_menu_camera
                            )
                            .error(
                                    android.R.drawable
                                            .ic_menu_camera
                            )
                            .centerCrop()
                            .into(
                                    ivStudentPhoto
                            );


                } else {


                    try {


                        ivStudentPhoto.setImageURI(
                                Uri.parse(photo)
                        );


                    } catch (Exception ignored) {


                        ivStudentPhoto.setImageResource(
                                android.R.drawable
                                        .ic_menu_camera
                        );
                    }
                }


            } catch (Exception ignored) {


                ivStudentPhoto.setImageResource(
                        android.R.drawable
                                .ic_menu_camera
                );
            }


        } else {


            ivStudentPhoto.setImageResource(
                    android.R.drawable
                            .ic_menu_camera
            );
        }


        // =====================================================
        // NAME
        // =====================================================

        String studentName =
                buildStudentName(
                        document
                );


        tvStudentName.setText(
                studentName
        );


        // =====================================================
        // PERSONAL INFORMATION
        // =====================================================

        String personalInfo =

                "Date of Birth: " +
                        display(
                                getString(
                                        document,
                                        "dateOfBirth"
                                )
                        ) +

                        "\nGender: " +
                        display(
                                getString(
                                        document,
                                        "gender"
                                )
                        ) +

                        "\nBlood Group: " +
                        display(
                                getString(
                                        document,
                                        "bloodGroup"
                                )
                        ) +

                        "\nAadhaar: " +
                        display(
                                getString(
                                        document,
                                        "aadhaar"
                                )
                        ) +

                        "\nBirth Place: " +
                        display(
                                getString(
                                        document,
                                        "birthPlace"
                                )
                        ) +

                        "\nNationality: " +
                        display(
                                getString(
                                        document,
                                        "nationality"
                                )
                        ) +

                        "\nMother Tongue: " +
                        display(
                                getString(
                                        document,
                                        "motherTongue"
                                )
                        ) +

                        "\nReligion: " +
                        display(
                                getString(
                                        document,
                                        "religion"
                                )
                        ) +

                        "\nCategory: " +
                        display(
                                getString(
                                        document,
                                        "category"
                                )
                        ) +

                        "\nCaste: " +
                        display(
                                getString(
                                        document,
                                        "caste"
                                )
                        ) +

                        "\nSub-Caste: " +
                        display(
                                getString(
                                        document,
                                        "subCaste"
                                )
                        ) +

                        "\nSpecial Needs: " +
                        display(
                                getString(
                                        document,
                                        "specialNeeds"
                                )
                        );


        tvPersonalInfo.setText(
                personalInfo
        );


        // =====================================================
        // ACADEMIC INFORMATION
        // =====================================================

        String academicInfo =

                "Admission No: " +
                        display(
                                getString(
                                        document,
                                        "admissionNumber"
                                )
                        ) +

                        "\nAdmission Date: " +
                        display(
                                getString(
                                        document,
                                        "admissionDate"
                                )
                        ) +

                        "\nAcademic Year: " +
                        display(
                                getString(
                                        document,
                                        "academicYear"
                                )
                        ) +

                        "\nClass: " +
                        display(
                                getString(
                                        document,
                                        "className"
                                )
                        ) +

                        "\nDivision: " +
                        display(
                                getString(
                                        document,
                                        "division"
                                )
                        ) +

                        "\nRoll No: " +
                        display(
                                getString(
                                        document,
                                        "rollNumber"
                                )
                        ) +

                        "\nPrevious School: " +
                        display(
                                getString(
                                        document,
                                        "previousSchool"
                                )
                        ) +

                        "\nPrevious Class: " +
                        display(
                                getString(
                                        document,
                                        "previousClass"
                                )
                        ) +

                        "\nLeaving Certificate: " +
                        display(
                                getString(
                                        document,
                                        "leavingCertificate"
                                )
                        );


        tvAcademicInfo.setText(
                academicInfo
        );


        // =====================================================
        // PARENT INFORMATION
        // =====================================================

        String parentInfo =

                "Father: " +
                        display(
                                getString(
                                        document,
                                        "fatherName"
                                )
                        ) +

                        "\nFather Mobile: " +
                        display(
                                getString(
                                        document,
                                        "fatherMobile"
                                )
                        ) +

                        "\nFather Email: " +
                        display(
                                getString(
                                        document,
                                        "fatherEmail"
                                )
                        ) +

                        "\nFather Occupation: " +
                        display(
                                getString(
                                        document,
                                        "fatherOccupation"
                                )
                        ) +

                        "\n\nMother: " +
                        display(
                                getString(
                                        document,
                                        "motherName"
                                )
                        ) +

                        "\nMother Mobile: " +
                        display(
                                getString(
                                        document,
                                        "motherMobile"
                                )
                        ) +

                        "\nMother Email: " +
                        display(
                                getString(
                                        document,
                                        "motherEmail"
                                )
                        ) +

                        "\nMother Occupation: " +
                        display(
                                getString(
                                        document,
                                        "motherOccupation"
                                )
                        ) +

                        "\n\nGuardian: " +
                        display(
                                getString(
                                        document,
                                        "guardianName"
                                )
                        ) +

                        "\nGuardian Relation: " +
                        display(
                                getString(
                                        document,
                                        "guardianRelation"
                                )
                        ) +

                        "\nGuardian Mobile: " +
                        display(
                                getString(
                                        document,
                                        "guardianMobile"
                                )
                        );


        tvParentInfo.setText(
                parentInfo
        );


        // =====================================================
        // ADDRESS
        // =====================================================

        String addressInfo =

                "Current Address:\n" +
                        display(
                                getString(
                                        document,
                                        "currentAddress"
                                )
                        ) +

                        "\n\nPermanent Address:\n" +
                        display(
                                getString(
                                        document,
                                        "permanentAddress"
                                )
                        ) +

                        "\n\nCity / Village: " +
                        display(
                                getString(
                                        document,
                                        "city"
                                )
                        ) +

                        "\nDistrict: " +
                        display(
                                getString(
                                        document,
                                        "district"
                                )
                        ) +

                        "\nState: " +
                        display(
                                getString(
                                        document,
                                        "state"
                                )
                        ) +

                        "\nPIN Code: " +
                        display(
                                getString(
                                        document,
                                        "pinCode"
                                )
                        ) +

                        "\n\nEmergency Contact: " +
                        display(
                                getString(
                                        document,
                                        "emergencyName"
                                )
                        ) +

                        "\nRelation: " +
                        display(
                                getString(
                                        document,
                                        "emergencyRelation"
                                )
                        ) +

                        "\nMobile: " +
                        display(
                                getString(
                                        document,
                                        "emergencyMobile"
                                )
                        );


        tvAddressInfo.setText(
                addressInfo
        );


        // =====================================================
        // FEES
        // =====================================================

        double totalFees =
                getDouble(
                        document,
                        "totalFees"
                );


        double paidFees =
                getDouble(
                        document,
                        "paidFees"
                );


        double remainingFees =
                totalFees -
                        paidFees;


        if (remainingFees < 0) {

            remainingFees = 0;
        }


        String paymentMode =
                getString(
                        document,
                        "paymentMode"
                );


        String installmentType =
                getString(
                        document,
                        "installmentType"
                );


        String installmentAmount =
                getString(
                        document,
                        "installmentAmount"
                );


        String paymentDate =
                getString(
                        document,
                        "paymentDate"
                );


        String feeInfo =

                "Total Fees: ₹ " +
                        formatAmount(
                                totalFees
                        ) +

                        "\nPaid Fees: ₹ " +
                        formatAmount(
                                paidFees
                        ) +

                        "\nRemaining Fees: ₹ " +
                        formatAmount(
                                remainingFees
                        ) +

                        "\nPayment Mode: " +
                        display(
                                paymentMode
                        ) +

                        "\nInstallment Type: " +
                        display(
                                installmentType
                        ) +

                        "\nInstallment Amount: ₹ " +
                        display(
                                installmentAmount
                        ) +

                        "\nPayment Date: " +
                        display(
                                paymentDate
                        );


        tvFeesInfo.setText(
                feeInfo
        );


        // =====================================================
        // LOGIN INFORMATION
        // =====================================================

        String studentEmail =
                getString(
                        document,
                        "studentEmail"
                );


        String studentPhone =
                getString(
                        document,
                        "studentPhone"
                );


        String loginInfo =

                "Email: " +
                        display(
                                studentEmail
                        ) +

                        "\nPhone: " +
                        display(
                                studentPhone
                        ) +

                        "\nPassword: ••••••••";


        tvLoginInfo.setText(
                loginInfo
        );
    }


    // =========================================================
    // BUILD STUDENT NAME
    // =========================================================

    private String buildStudentName(
            DocumentSnapshot document) {


        String first =
                getString(
                        document,
                        "firstName"
                );


        String middle =
                getString(
                        document,
                        "middleName"
                );


        String last =
                getString(
                        document,
                        "lastName"
                );


        String name =
                (
                        first +
                                " " +
                                middle +
                                " " +
                                last
                ).trim();


        if (name.isEmpty()) {

            name =
                    getString(
                            document,
                            "studentName"
                    );
        }


        if (name.isEmpty()) {

            name =
                    "Student";
        }


        return name;
    }


    // =========================================================
    // GET STRING
    // =========================================================

    private String getString(
            DocumentSnapshot document,
            String key) {


        Object value =
                document.get(
                        key
                );


        if (value == null) {

            return "";
        }


        return String.valueOf(
                value
        ).trim();
    }


    // =========================================================
    // GET DOUBLE
    // =========================================================

    private double getDouble(
            DocumentSnapshot document,
            String key) {


        Object value =
                document.get(
                        key
                );


        if (value == null) {

            return 0;
        }


        if (value instanceof Number) {

            return (
                    (Number) value
            ).doubleValue();
        }


        try {

            return Double.parseDouble(
                    String.valueOf(
                            value
                    ).trim()
            );

        } catch (Exception e) {

            return 0;
        }
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


        return value.trim();
    }


    // =========================================================
    // FORMAT AMOUNT
    // =========================================================

    private String formatAmount(
            double amount) {


        return String.format(
                java.util.Locale.getDefault(),
                "%.2f",
                amount
        );
    }
}