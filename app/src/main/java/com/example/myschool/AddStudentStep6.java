package com.example.myschool;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddStudentStep6 extends AppCompatActivity {

    private EditText etStudentEmail;
    private EditText etStudentPhone;
    private EditText etPassword;
    private EditText etConfirmPassword;

    private ImageView ivTogglePassword;
    private ImageView ivToggleConfirmPassword;

    private boolean passwordVisible = false;
    private boolean confirmPasswordVisible = false;

    // =========================================================
    // EDIT MODE
    // =========================================================

    private boolean editMode = false;
    private String studentId = "";

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_add_student_step6
        );

        db =
                FirebaseFirestore.getInstance();

        initViews();

        setupPasswordToggle();

        setupListeners();

        setupSystemBack();

        checkEditMode();
    }

    // =========================================================
    // INITIALIZE
    // =========================================================

    private void initViews() {

        etStudentEmail =
                findViewById(
                        R.id.etStudentEmail
                );

        etStudentPhone =
                findViewById(
                        R.id.etStudentPhone
                );

        etPassword =
                findViewById(
                        R.id.etPassword
                );

        etConfirmPassword =
                findViewById(
                        R.id.etConfirmPassword
                );

        ivTogglePassword =
                findViewById(
                        R.id.ivTogglePassword
                );

        ivToggleConfirmPassword =
                findViewById(
                        R.id.ivToggleConfirmPassword
                );
    }

    // =========================================================
    // PASSWORD SHOW / HIDE
    // =========================================================

    private void setupPasswordToggle() {

        ivTogglePassword.setOnClickListener(v -> {

            passwordVisible =
                    !passwordVisible;

            if (passwordVisible) {

                etPassword.setInputType(
                        InputType.TYPE_CLASS_TEXT |
                                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                );

                ivTogglePassword.setImageResource(
                        android.R.drawable.ic_menu_view
                );

            } else {

                etPassword.setInputType(
                        InputType.TYPE_CLASS_TEXT |
                                InputType.TYPE_TEXT_VARIATION_PASSWORD
                );

                ivTogglePassword.setImageResource(
                        android.R.drawable.ic_menu_view
                );
            }

            etPassword.setSelection(
                    etPassword.length()
            );
        });


        ivToggleConfirmPassword.setOnClickListener(v -> {

            confirmPasswordVisible =
                    !confirmPasswordVisible;

            if (confirmPasswordVisible) {

                etConfirmPassword.setInputType(
                        InputType.TYPE_CLASS_TEXT |
                                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                );

                ivToggleConfirmPassword.setImageResource(
                        android.R.drawable.ic_menu_view
                );

            } else {

                etConfirmPassword.setInputType(
                        InputType.TYPE_CLASS_TEXT |
                                InputType.TYPE_TEXT_VARIATION_PASSWORD
                );

                ivToggleConfirmPassword.setImageResource(
                        android.R.drawable.ic_menu_view
                );
            }

            etConfirmPassword.setSelection(
                    etConfirmPassword.length()
            );
        });
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

            loadLoginData();
        }
    }

    // =========================================================
    // LOAD LOGIN DATA
    // =========================================================

    private void loadLoginData() {

        db.collection("students")
                .document(studentId)
                .get()
                .addOnSuccessListener(
                        documentSnapshot -> {

                            if (!documentSnapshot.exists()) {

                                return;
                            }

                            // =========================================
                            // EMAIL
                            // =========================================

                            etStudentEmail.setText(
                                    getString(
                                            documentSnapshot,
                                            "studentEmail"
                                    )
                            );

                            // =========================================
                            // PHONE
                            // =========================================

                            etStudentPhone.setText(
                                    getString(
                                            documentSnapshot,
                                            "studentPhone"
                                    )
                            );

                            /*
                             * IMPORTANT:
                             *
                             * Existing password is NOT loaded.
                             * We never display/store plaintext password.
                             *
                             * If user wants to change password,
                             * they can enter a new one.
                             */
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

        String email =
                etStudentEmail
                        .getText()
                        .toString()
                        .trim();

        String phone =
                etStudentPhone
                        .getText()
                        .toString()
                        .trim();

        String password =
                etPassword
                        .getText()
                        .toString();

        String confirmPassword =
                etConfirmPassword
                        .getText()
                        .toString();


        // =====================================================
        // EMAIL
        // =====================================================

        if (email.isEmpty()) {

            etStudentEmail.setError(
                    "Enter student email"
            );

            etStudentEmail.requestFocus();

            return;
        }


        if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            etStudentEmail.setError(
                    "Enter valid email"
            );

            etStudentEmail.requestFocus();

            return;
        }


        // =====================================================
        // PHONE
        // =====================================================

        if (phone.length() != 10) {

            etStudentPhone.setError(
                    "Enter valid 10 digit phone"
            );

            etStudentPhone.requestFocus();

            return;
        }


        // =====================================================
        // PASSWORD
        // =====================================================

        /*
         * ADD MODE:
         * Password is required.
         *
         * EDIT MODE:
         * Password can be left blank.
         * Blank means keep existing password.
         */

        if (!editMode &&
                password.isEmpty()) {

            etPassword.setError(
                    "Enter password"
            );

            etPassword.requestFocus();

            return;
        }


        // =====================================================
        // PASSWORD LENGTH
        // =====================================================

        if (!password.isEmpty() &&
                password.length() < 6) {

            etPassword.setError(
                    "Password must be at least 6 characters"
            );

            etPassword.requestFocus();

            return;
        }


        // =====================================================
        // CONFIRM PASSWORD
        // =====================================================

        if (!password.isEmpty() &&
                !password.equals(confirmPassword)) {

            etConfirmPassword.setError(
                    "Passwords do not match"
            );

            etConfirmPassword.requestFocus();

            return;
        }


        // =====================================================
        // OPEN STEP 7
        // =====================================================

        Intent intent =
                new Intent(
                        AddStudentStep6.this,
                        AddStudentStep7.class
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
        // COPY PREVIOUS DATA
        // =====================================================

        copyData(
                getIntent(),
                intent
        );


        // =====================================================
        // LOGIN DATA
        // =====================================================

        intent.putExtra(
                "studentEmail",
                email
        );

        intent.putExtra(
                "studentPhone",
                phone
        );


        /*
         * Password only goes forward when user
         * actually entered a new password.
         */

        intent.putExtra(
                "studentPassword",
                password
        );


        intent.putExtra(
                "confirmPassword",
                confirmPassword
        );


        startActivity(intent);
    }

    // =========================================================
    // COPY PREVIOUS DATA
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
                "guardianMobile",

                // STEP 4
                "currentAddress",
                "permanentAddress",
                "city",
                "district",
                "state",
                "pinCode",
                "emergencyName",
                "emergencyRelation",
                "emergencyMobile",

                // STEP 5
                "totalFees",
                "paidFees",
                "remainingFees",
                "paymentMode",
                "installmentType",
                "installmentAmount",
                "paymentDate"
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