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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class AddStudentStep7 extends AppCompatActivity {

    // =========================================================
    // VIEWS
    // =========================================================

    private ImageButton btnBackStep7;

    private ImageView ivStudentPhoto;

    private TextView tvStudentName;
    private TextView tvPersonalInfo;
    private TextView tvAcademicInfo;
    private TextView tvParentInfo;
    private TextView tvAddressInfo;
    private TextView tvFeesInfo;
    private TextView tvLoginInfo;
    private TextView tvSubmitted;

    private CheckBox checkConfirm;

    private Button btnSubmit;


    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseFirestore db;

    private FirebaseAuth mAuth;


    // =========================================================
    // MODE
    // =========================================================

    private boolean editMode = false;

    private String studentId = "";

    private boolean isSubmitting = false;


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_add_student_step7
        );


        db =
                FirebaseFirestore.getInstance();

        mAuth =
                FirebaseAuth.getInstance();


        initViews();

        checkEditMode();

        loadReviewData();

        setupListeners();

        setupSystemBack();
    }


    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initViews() {

        btnBackStep7 =
                findViewById(
                        R.id.btnBackStep7
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


        tvSubmitted =
                findViewById(
                        R.id.tvSubmitted
                );


        checkConfirm =
                findViewById(
                        R.id.checkConfirm
                );


        btnSubmit =
                findViewById(
                        R.id.btnSubmit
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


        String receivedStudentId =
                intent.getStringExtra(
                        "studentId"
                );


        if (receivedStudentId != null) {

            studentId =
                    receivedStudentId.trim();

        } else {

            studentId = "";
        }


        if (editMode &&
                !studentId.isEmpty()) {

            btnSubmit.setText(
                    "✓ UPDATE STUDENT"
            );

        } else {

            btnSubmit.setText(
                    "✓ ADD STUDENT"
            );
        }
    }


    // =========================================================
    // LOAD REVIEW DATA
    // =========================================================

    private void loadReviewData() {

        Intent intent =
                getIntent();


        // =====================================================
        // PHOTO
        // =====================================================

        String photo =
                getExtra(
                        intent,
                        "studentPhoto"
                );


        if (!photo.isEmpty()) {

            try {

                ivStudentPhoto.setImageURI(
                        Uri.parse(photo)
                );

            } catch (Exception e) {

                ivStudentPhoto.setImageResource(
                        android.R.drawable.ic_menu_camera
                );
            }

        } else {

            ivStudentPhoto.setImageResource(
                    android.R.drawable.ic_menu_camera
            );
        }


        // =====================================================
        // NAME
        // =====================================================

        String firstName =
                getExtra(
                        intent,
                        "firstName"
                );


        String middleName =
                getExtra(
                        intent,
                        "middleName"
                );


        String lastName =
                getExtra(
                        intent,
                        "lastName"
                );


        String fullName =
                (
                        firstName + " " +
                                middleName + " " +
                                lastName
                ).trim();


        if (fullName.isEmpty()) {

            fullName =
                    "Student Name";
        }


        tvStudentName.setText(
                fullName
        );


        // =====================================================
        // PERSONAL
        // =====================================================

        tvPersonalInfo.setText(

                "Date of Birth: " +
                        display(
                                intent,
                                "dateOfBirth"
                        ) +

                        "\nGender: " +
                        display(
                                intent,
                                "gender"
                        ) +

                        "\nBlood Group: " +
                        display(
                                intent,
                                "bloodGroup"
                        ) +

                        "\nAadhaar: " +
                        display(
                                intent,
                                "aadhaar"
                        ) +

                        "\nBirth Place: " +
                        display(
                                intent,
                                "birthPlace"
                        ) +

                        "\nNationality: " +
                        display(
                                intent,
                                "nationality"
                        ) +

                        "\nMother Tongue: " +
                        display(
                                intent,
                                "motherTongue"
                        ) +

                        "\nReligion: " +
                        display(
                                intent,
                                "religion"
                        ) +

                        "\nCategory: " +
                        display(
                                intent,
                                "category"
                        ) +

                        "\nCaste: " +
                        display(
                                intent,
                                "caste"
                        ) +

                        "\nSub-Caste: " +
                        display(
                                intent,
                                "subCaste"
                        ) +

                        "\nSpecial Needs: " +
                        display(
                                intent,
                                "specialNeeds"
                        )
        );


        // =====================================================
        // ACADEMIC
        // =====================================================

        tvAcademicInfo.setText(

                "Admission No: " +
                        display(
                                intent,
                                "admissionNumber"
                        ) +

                        "\nAdmission Date: " +
                        display(
                                intent,
                                "admissionDate"
                        ) +

                        "\nAcademic Year: " +
                        display(
                                intent,
                                "academicYear"
                        ) +

                        "\nClass: " +
                        display(
                                intent,
                                "className"
                        ) +

                        "\nDivision: " +
                        display(
                                intent,
                                "division"
                        ) +

                        "\nRoll No: " +
                        display(
                                intent,
                                "rollNumber"
                        ) +

                        "\nPrevious School: " +
                        display(
                                intent,
                                "previousSchool"
                        ) +

                        "\nPrevious Class: " +
                        display(
                                intent,
                                "previousClass"
                        ) +

                        "\nLeaving Certificate: " +
                        display(
                                intent,
                                "leavingCertificate"
                        )
        );


        // =====================================================
        // PARENT
        // =====================================================

        tvParentInfo.setText(

                "Father: " +
                        display(
                                intent,
                                "fatherName"
                        ) +

                        "\nFather Mobile: " +
                        display(
                                intent,
                                "fatherMobile"
                        ) +

                        "\nFather Email: " +
                        display(
                                intent,
                                "fatherEmail"
                        ) +

                        "\nFather Occupation: " +
                        display(
                                intent,
                                "fatherOccupation"
                        ) +

                        "\n\nMother: " +
                        display(
                                intent,
                                "motherName"
                        ) +

                        "\nMother Mobile: " +
                        display(
                                intent,
                                "motherMobile"
                        ) +

                        "\nMother Email: " +
                        display(
                                intent,
                                "motherEmail"
                        ) +

                        "\nMother Occupation: " +
                        display(
                                intent,
                                "motherOccupation"
                        ) +

                        "\n\nGuardian: " +
                        display(
                                intent,
                                "guardianName"
                        ) +

                        "\nRelation: " +
                        display(
                                intent,
                                "guardianRelation"
                        ) +

                        "\nMobile: " +
                        display(
                                intent,
                                "guardianMobile"
                        )
        );


        // =====================================================
        // ADDRESS
        // =====================================================

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

                        "\nPIN: " +
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


        // =====================================================
        // FEES
        // =====================================================

        tvFeesInfo.setText(

                "Total Fees: ₹ " +
                        display(
                                intent,
                                "totalFees"
                        ) +

                        "\nPaid Fees: ₹ " +
                        display(
                                intent,
                                "paidFees"
                        ) +

                        "\nRemaining Fees: ₹ " +
                        display(
                                intent,
                                "remainingFees"
                        ) +

                        "\nPayment Mode: " +
                        display(
                                intent,
                                "paymentMode"
                        ) +

                        "\nInstallment Type: " +
                        display(
                                intent,
                                "installmentType"
                        ) +

                        "\nInstallment Amount: ₹ " +
                        display(
                                intent,
                                "installmentAmount"
                        ) +

                        "\nPayment Date: " +
                        display(
                                intent,
                                "paymentDate"
                        )
        );


        // =====================================================
        // LOGIN
        // =====================================================

        tvLoginInfo.setText(

                "Email: " +
                        display(
                                intent,
                                "studentEmail"
                        ) +

                        "\nPhone: " +
                        display(
                                intent,
                                "studentPhone"
                        ) +

                        "\nPassword: ••••••••"
        );
    }


    // =========================================================
    // LISTENERS
    // =========================================================

    private void setupListeners() {

        btnBackStep7.setOnClickListener(
                v -> finish()
        );


        btnSubmit.setOnClickListener(
                v -> {

                    if (isSubmitting) {
                        return;
                    }


                    if (!checkConfirm.isChecked()) {

                        Toast.makeText(
                                AddStudentStep7.this,
                                "Please confirm the information first",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }


                    saveStudent();
                }
        );
    }


    // =========================================================
    // SAVE STUDENT
    // =========================================================

    private boolean creatorIsTeacher = false;

    private String teacherSchoolId = "";
    private String teacherAssignedClass = "";
    private String teacherAssignedDivision = "";

    private void saveStudent() {

        if (mAuth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Login session expired. Please login again.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        isSubmitting = true;

        btnSubmit.setEnabled(false);

        Intent intent = getIntent();

        String currentUid =
                mAuth.getCurrentUser().getUid();

        // ---------------------------------------------------------
        // CHECK WHETHER CURRENT USER IS TEACHER
        // ---------------------------------------------------------

        db.collection("staff")
                .whereEqualTo(
                        "authUid",
                        currentUid
                )
                .whereEqualTo(
                        "active",
                        true
                )
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {

                    creatorIsTeacher = false;

                    teacherSchoolId = "";
                    teacherAssignedClass = "";
                    teacherAssignedDivision = "";

                    // -------------------------------------------------
                    // TEACHER
                    // -------------------------------------------------

                    if (!snapshot.isEmpty()) {

                        DocumentSnapshot staff =
                                snapshot.getDocuments()
                                        .get(0);

                        String staffType =
                                safeStaff(
                                        staff.getString(
                                                "staffType"
                                        )
                                );

                        String role =
                                safeStaff(
                                        staff.getString(
                                                "role"
                                        )
                                );

                        boolean isTeacher =
                                "Teacher".equalsIgnoreCase(
                                        staffType
                                )
                                        ||
                                        "Teacher".equalsIgnoreCase(
                                                role
                                        );

                        if (isTeacher) {

                            creatorIsTeacher = true;

                            teacherSchoolId =
                                    safeStaff(
                                            staff.getString(
                                                    "schoolId"
                                            )
                                    );

                            teacherAssignedClass =
                                    safeStaff(
                                            staff.getString(
                                                    "assignedClass"
                                            )
                                    );

                            teacherAssignedDivision =
                                    safeStaff(
                                            staff.getString(
                                                    "assignedDivision"
                                            )
                                    );
                        }
                    }

                    // -------------------------------------------------
                    // TEACHER SAVE
                    // -------------------------------------------------

                    if (creatorIsTeacher) {

                        if (teacherSchoolId.isEmpty()) {

                            saveFailed(
                                    "Teacher school information not found."
                            );

                            return;
                        }

                        if (teacherAssignedClass.isEmpty()) {

                            saveFailed(
                                    "Teacher class is not assigned."
                            );

                            return;
                        }

                        /*
                         * IMPORTANT:
                         * Force the student into teacher's assigned class.
                         */

                        intent.putExtra(
                                "className",
                                teacherAssignedClass
                        );

                        /*
                         * If teacher has a specific division,
                         * force that division.
                         *
                         * If teacher is assigned "All Divisions",
                         * keep the division selected in Step 2.
                         */

                        if (!teacherAssignedDivision.isEmpty()
                                && !teacherAssignedDivision.equalsIgnoreCase(
                                "All Divisions"
                        )) {

                            intent.putExtra(
                                    "division",
                                    teacherAssignedDivision
                            );
                        }

                        String schoolId =
                                teacherSchoolId;

                        if (editMode
                                && !studentId.isEmpty()) {

                            updateExistingStudent(
                                    schoolId,
                                    intent
                            );

                            return;
                        }

                        addNewStudent(
                                schoolId,
                                intent
                        );

                        return;
                    }

                    // -------------------------------------------------
                    // ADMIN SAVE
                    // -------------------------------------------------

                    String schoolId =
                            currentUid;

                    if (editMode
                            && !studentId.isEmpty()) {

                        updateExistingStudent(
                                schoolId,
                                intent
                        );

                        return;
                    }

                    addNewStudent(
                            schoolId,
                            intent
                    );
                })
                .addOnFailureListener(e -> {

                    /*
                     * If staff lookup fails, do NOT accidentally
                     * save a teacher under teacher UID.
                     */

                    saveFailed(
                            "Unable to identify account type: "
                                    + e.getMessage()
                    );
                });
    }

    private String safeStaff(String value) {

        if (value == null) {
            return "";
        }

        return value.trim();
    }
    // =========================================================
    // ADD NEW STUDENT
    // =========================================================

    private void addNewStudent(
            String schoolId,
            Intent intent) {

        btnSubmit.setText(
                "CREATING STUDENT ACCOUNT..."
        );


        String email =
                getExtra(
                        intent,
                        "studentEmail"
                );


        String password =
                getExtra(
                        intent,
                        "studentPassword"
                );


        if (email.isEmpty()) {

            saveFailed(
                    "Student email is required."
            );

            return;
        }


        if (password.isEmpty()) {

            saveFailed(
                    "Student password is missing. Please go back to Step 6."
            );

            return;
        }


        if (password.length() < 6) {

            saveFailed(
                    "Student password must contain at least 6 characters."
            );

            return;
        }


        // =====================================================
        // CREATE FIRESTORE DOCUMENT ID
        // =====================================================

        String newStudentId =
                db.collection("students")
                        .document()
                        .getId();


        // =====================================================
        // CREATE FIREBASE AUTH ACCOUNT
        // =====================================================

        createStudentAuthAccount(
                schoolId,
                intent,
                newStudentId,
                email,
                password
        );
    }


    // =========================================================
    // CREATE STUDENT AUTH ACCOUNT
    // =========================================================
    //
    // IMPORTANT:
    // We use a SECONDARY FirebaseApp.
    // Therefore Admin account remains logged in.
    //
    // =========================================================

    private void createStudentAuthAccount(
            String schoolId,
            Intent intent,
            String newStudentId,
            String email,
            String password) {


        FirebaseApp secondaryApp;


        // =====================================================
        // GET / CREATE SECONDARY APP
        // =====================================================

        try {

            secondaryApp =
                    FirebaseApp.getInstance(
                            "StudentAuthApp"
                    );

        } catch (IllegalStateException e) {

            FirebaseOptions options =
                    FirebaseApp.getInstance()
                            .getOptions();


            secondaryApp =
                    FirebaseApp.initializeApp(
                            this,
                            options,
                            "StudentAuthApp"
                    );
        }


        if (secondaryApp == null) {

            saveFailed(
                    "Unable to initialize student login service."
            );

            return;
        }


        // =====================================================
        // SECONDARY AUTH
        // =====================================================

        FirebaseAuth studentAuth =
                FirebaseAuth.getInstance(
                        secondaryApp
                );


        // =====================================================
        // CREATE AUTH USER
        // =====================================================

        studentAuth
                .createUserWithEmailAndPassword(
                        email.trim(),
                        password
                )
                .addOnCompleteListener(
                        this,
                        task -> {

                            if (!task.isSuccessful()) {

                                String message =
                                        "Unable to create student login account.";


                                if (task.getException() != null &&
                                        task.getException()
                                                .getMessage() != null) {

                                    message =
                                            task.getException()
                                                    .getMessage();
                                }


                                saveFailed(
                                        "Student login account failed:\n" +
                                                message
                                );

                                return;
                            }


                            if (task.getResult() == null ||
                                    task.getResult().getUser() == null) {

                                saveFailed(
                                        "Student account was not created."
                                );

                                return;
                            }


                            String authUid =
                                    task.getResult()
                                            .getUser()
                                            .getUid();


                            // =================================================
                            // SAVE FIRESTORE STUDENT
                            // =================================================

                            saveStudentWithAuthUid(
                                    schoolId,
                                    intent,
                                    newStudentId,
                                    authUid,
                                    studentAuth
                            );
                        }
                );
    }


    // =========================================================
    // SAVE STUDENT WITH AUTH UID
    // =========================================================

    private void saveStudentWithAuthUid(
            String schoolId,
            Intent intent,
            String newStudentId,
            String authUid,
            FirebaseAuth studentAuth) {


        Map<String, Object> student =
                createStudentMap(
                        intent
                );


        // =====================================================
        // BASIC
        // =====================================================

        student.put(
                "studentId",
                newStudentId
        );


        student.put(
                "schoolId",
                schoolId
        );


        student.put(
                "authUid",
                authUid
        );


        student.put(
                "status",
                "Active"
        );


        student.put(
                "active",
                true
        );


        student.put(
                "createdAt",
                FieldValue.serverTimestamp()
        );


        // =====================================================
        // SAVE
        // =====================================================

        db.collection("students")
                .document(newStudentId)
                .set(student)
                .addOnCompleteListener(
                        task -> {

                            // -------------------------------------------------
                            // SIGN OUT ONLY SECONDARY STUDENT AUTH
                            // -------------------------------------------------

                            studentAuth.signOut();


                            if (!task.isSuccessful()) {

                                String message =
                                        "Student save failed.";


                                if (task.getException() != null &&
                                        task.getException()
                                                .getMessage() != null) {

                                    message =
                                            task.getException()
                                                    .getMessage();
                                }


                                saveFailed(
                                        message
                                );

                                return;
                            }


                            // =================================================
                            // UPDATE SCHOOL STUDENT COUNT
                            // =================================================

                            updateStudentCountAfterAdd(
                                    schoolId
                            );
                        }
                );
    }


    // =========================================================
    // UPDATE EXISTING STUDENT
    // =========================================================

    private void updateExistingStudent(
            String schoolId,
            Intent intent) {

        btnSubmit.setText(
                "UPDATING STUDENT..."
        );


        Map<String, Object> updatedData =
                createStudentMap(
                        intent
                );


        // =====================================================
        // IMPORTANT
        // =====================================================
        //
        // Same student ID.
        // Same Firestore document.
        // Do not create a new Auth account.
        //
        // =====================================================

        updatedData.put(
                "studentId",
                studentId
        );


        updatedData.put(
                "schoolId",
                schoolId
        );


        // =====================================================
        // UPDATE SAME DOCUMENT
        // =====================================================

        db.collection("students")
                .document(studentId)
                .set(
                        updatedData,
                        SetOptions.merge()
                )
                .addOnSuccessListener(
                        unused -> {

                            studentUpdatedSuccessfully();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            saveFailed(
                                    "Student update failed:\n" +
                                            e.getMessage()
                            );
                        }
                );
    }


    // =========================================================
    // CREATE STUDENT DATA MAP
    // =========================================================

    private Map<String, Object> createStudentMap(
            Intent intent) {

        Map<String, Object> student =
                new HashMap<>();


        // =====================================================
        // PHOTO
        // =====================================================

        put(
                student,
                intent,
                "studentPhoto"
        );


        // =====================================================
        // PERSONAL
        // =====================================================

        put(student, intent, "firstName");
        put(student, intent, "middleName");
        put(student, intent, "lastName");
        put(student, intent, "dateOfBirth");
        put(student, intent, "gender");
        put(student, intent, "bloodGroup");
        put(student, intent, "aadhaar");
        put(student, intent, "birthPlace");
        put(student, intent, "nationality");
        put(student, intent, "motherTongue");
        put(student, intent, "religion");
        put(student, intent, "category");
        put(student, intent, "caste");
        put(student, intent, "subCaste");
        put(student, intent, "specialNeeds");


        // =====================================================
        // ACADEMIC
        // =====================================================

        put(student, intent, "admissionNumber");
        put(student, intent, "admissionDate");
        put(student, intent, "academicYear");
        put(student, intent, "className");
        put(student, intent, "division");
        put(student, intent, "rollNumber");
        put(student, intent, "previousSchool");
        put(student, intent, "previousClass");
        put(student, intent, "leavingCertificate");


        // =====================================================
        // FATHER
        // =====================================================

        put(student, intent, "fatherName");
        put(student, intent, "fatherMobile");
        put(student, intent, "fatherEmail");
        put(student, intent, "fatherOccupation");


        // =====================================================
        // MOTHER
        // =====================================================

        put(student, intent, "motherName");
        put(student, intent, "motherMobile");
        put(student, intent, "motherEmail");
        put(student, intent, "motherOccupation");


        // =====================================================
        // GUARDIAN
        // =====================================================

        put(student, intent, "guardianName");
        put(student, intent, "guardianRelation");
        put(student, intent, "guardianMobile");


        // =====================================================
        // ADDRESS
        // =====================================================

        put(student, intent, "currentAddress");
        put(student, intent, "permanentAddress");
        put(student, intent, "city");
        put(student, intent, "district");
        put(student, intent, "state");
        put(student, intent, "pinCode");


        // =====================================================
        // EMERGENCY
        // =====================================================

        put(student, intent, "emergencyName");
        put(student, intent, "emergencyRelation");
        put(student, intent, "emergencyMobile");


        // =====================================================
        // FEES
        // =====================================================

        put(student, intent, "totalFees");
        put(student, intent, "paidFees");
        put(student, intent, "remainingFees");
        put(student, intent, "paymentMode");
        put(student, intent, "installmentType");
        put(student, intent, "installmentAmount");
        put(student, intent, "paymentDate");


        // =====================================================
        // LOGIN DATA
        // =====================================================

        put(student, intent, "studentEmail");
        put(student, intent, "studentPhone");


        return student;
    }


    // =========================================================
    // UPDATE STUDENT COUNT
    // =========================================================

    private void updateStudentCountAfterAdd(
            String schoolId) {

        db.collection("schools")
                .document(schoolId)
                .update(
                        "studentCount",
                        FieldValue.increment(1)
                )
                .addOnSuccessListener(
                        unused ->
                                studentAddedSuccessfully()
                )
                .addOnFailureListener(
                        e -> {

                            // =================================================
                            // IF studentCount DOES NOT EXIST
                            // =================================================

                            Map<String, Object> countData =
                                    new HashMap<>();


                            countData.put(
                                    "studentCount",
                                    1
                            );


                            db.collection("schools")
                                    .document(schoolId)
                                    .set(
                                            countData,
                                            SetOptions.merge()
                                    )
                                    .addOnSuccessListener(
                                            unused ->
                                                    studentAddedSuccessfully()
                                    )
                                    .addOnFailureListener(
                                            error ->
                                                    studentAddedSuccessfully()
                                    );
                        }
                );
    }


    // =========================================================
    // STUDENT ADDED SUCCESS
    // =========================================================

    private void studentAddedSuccessfully() {

        isSubmitting = false;


        btnSubmit.setEnabled(
                true
        );


        btnSubmit.setText(
                "✓ STUDENT ADDED"
        );


        tvSubmitted.setVisibility(
                View.VISIBLE
        );


        Toast.makeText(
                this,
                "Student added successfully!",
                Toast.LENGTH_LONG
        ).show();


        goToDashboard();
    }


    // =========================================================
    // STUDENT UPDATED SUCCESS
    // =========================================================

    private void studentUpdatedSuccessfully() {

        isSubmitting = false;


        btnSubmit.setEnabled(
                true
        );


        btnSubmit.setText(
                "✓ STUDENT UPDATED"
        );


        tvSubmitted.setVisibility(
                View.VISIBLE
        );


        Toast.makeText(
                this,
                "Student updated successfully!",
                Toast.LENGTH_LONG
        ).show();


        goToDashboard();
    }


    // =========================================================
    // GO TO ADMIN DASHBOARD
    // =========================================================

    private void goToDashboard() {

        btnSubmit.postDelayed(
                () -> {

                    Intent dashboard;

                    if (creatorIsTeacher) {

                        dashboard =
                                new Intent(
                                        AddStudentStep7.this,
                                        TeacherDashboardActivity.class
                                );

                        // -------------------------------------------------
                        // IMPORTANT:
                        // Always resolve the real teacher staff document ID
                        // from authUid. This avoids losing staffId when the
                        // 7-step Add Student flow passes between activities.
                        // -------------------------------------------------

                        if (mAuth.getCurrentUser() == null) {

                            dashboard.setFlags(
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                            );

                            startActivity(dashboard);
                            finish();
                            return;
                        }

                        String teacherAuthUid =
                                mAuth.getCurrentUser().getUid();

                        db.collection("staff")
                                .whereEqualTo("authUid", teacherAuthUid)
                                .whereEqualTo("active", true)
                                .limit(1)
                                .get()
                                .addOnSuccessListener(snapshot -> {

                                    if (!snapshot.isEmpty()) {

                                        DocumentSnapshot staff =
                                                snapshot.getDocuments().get(0);

                                        // Firestore document ID = staffId
                                        String staffId = staff.getId();

                                        dashboard.putExtra(
                                                "staffId",
                                                staffId
                                        );

                                    } else {

                                        // Fallback if the staff query returns
                                        // no matching document.
                                        String fallbackStaffId =
                                                getIntent().getStringExtra("teacherStaffId");

                                        if (fallbackStaffId == null ||
                                                fallbackStaffId.trim().isEmpty()) {

                                            fallbackStaffId =
                                                    getIntent().getStringExtra("staffId");
                                        }

                                        if (fallbackStaffId != null &&
                                                !fallbackStaffId.trim().isEmpty()) {

                                            dashboard.putExtra(
                                                    "staffId",
                                                    fallbackStaffId.trim()
                                            );
                                        }
                                    }

                                    openTeacherDashboard(dashboard);
                                })
                                .addOnFailureListener(e -> {

                                    // Fallback to whichever staff ID reached Step 7.
                                    String fallbackStaffId =
                                            getIntent().getStringExtra("teacherStaffId");

                                    if (fallbackStaffId == null ||
                                            fallbackStaffId.trim().isEmpty()) {

                                        fallbackStaffId =
                                                getIntent().getStringExtra("staffId");
                                    }

                                    if (fallbackStaffId != null &&
                                            !fallbackStaffId.trim().isEmpty()) {

                                        dashboard.putExtra(
                                                "staffId",
                                                fallbackStaffId.trim()
                                        );
                                    }

                                    openTeacherDashboard(dashboard);
                                });

                        return;
                    }

                    dashboard =
                            new Intent(
                                    AddStudentStep7.this,
                                    AdminDashboardActivity.class
                            );

                    dashboard.setFlags(
                            Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                    );

                    startActivity(dashboard);

                    finish();

                },
                1000
        );
    }

    // =========================================================
    // OPEN TEACHER DASHBOARD
    // =========================================================

    private void openTeacherDashboard(Intent dashboard) {

        dashboard.setFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        startActivity(dashboard);

        finish();
    }


    // =========================================================
    // SAVE FAILED
    // =========================================================

    private void saveFailed(
            String message) {

        isSubmitting = false;


        btnSubmit.setEnabled(
                true
        );


        if (editMode &&
                !studentId.isEmpty()) {

            btnSubmit.setText(
                    "✓ UPDATE STUDENT"
            );

        } else {

            btnSubmit.setText(
                    "✓ ADD STUDENT"
            );
        }


        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }


    // =========================================================
    // PUT VALUE
    // =========================================================

    private void put(
            Map<String, Object> map,
            Intent intent,
            String key) {

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


    // =========================================================
    // GET EXTRA
    // =========================================================

    private String getExtra(
            Intent intent,
            String key) {

        String value =
                intent.getStringExtra(
                        key
                );


        if (value == null) {

            return "";
        }


        return value.trim();
    }


    // =========================================================
    // DISPLAY
    // =========================================================

    private String display(
            Intent intent,
            String key) {

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
