package com.example.myschool;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddStaffStep5 extends AppCompatActivity {

    private ImageButton btnTopBack;
    private Button btnNext;

    private EditText etStaffEmail;
    private EditText etStaffMobile;
    private EditText etPassword;
    private EditText etConfirmPassword;

    private boolean isEdit = false;
    private String staffId = "";

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_add_staff_step5
        );

        db = FirebaseFirestore.getInstance();

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
        setupListeners();
        setupSystemBack();

        if (isEdit && staffId != null && !staffId.trim().isEmpty()) {
            loadExistingStaff();
        }
    }

    private void initViews() {

        btnTopBack =
                findViewById(R.id.btnTopBack);

        btnNext =
                findViewById(R.id.btnNext);

        etStaffEmail =
                findViewById(R.id.etStaffEmail);

        etStaffMobile =
                findViewById(R.id.etStaffMobile);

        etPassword =
                findViewById(R.id.etPassword);

        etConfirmPassword =
                findViewById(R.id.etConfirmPassword);
    }

    private void setupListeners() {

        btnTopBack.setOnClickListener(v ->
                finish()
        );

        btnNext.setOnClickListener(v ->
                validateAndNext()
        );
    }

    private void loadExistingStaff() {

        db.collection("staff")
                .document(staffId)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {
                        return;
                    }

                    etStaffEmail.setText(
                            getString(
                                    document,
                                    "staffEmail"
                            )
                    );

                    etStaffMobile.setText(
                            getString(
                                    document,
                                    "staffPhone"
                            )
                    );

                    // Password intentionally blank.
                    etPassword.setText("");
                    etConfirmPassword.setText("");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Unable to load login information.",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void validateAndNext() {

        String email =
                etStaffEmail.getText()
                        .toString()
                        .trim();

        String mobile =
                etStaffMobile.getText()
                        .toString()
                        .trim();

        String password =
                etPassword.getText()
                        .toString();

        String confirmPassword =
                etConfirmPassword.getText()
                        .toString();

        if (email.isEmpty()) {

            etStaffEmail.setError(
                    "Email is required"
            );

            etStaffEmail.requestFocus();

            return;
        }

        if (!Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            etStaffEmail.setError(
                    "Enter valid email address"
            );

            etStaffEmail.requestFocus();

            return;
        }

        if (mobile.isEmpty()) {

            etStaffMobile.setError(
                    "Mobile number is required"
            );

            etStaffMobile.requestFocus();

            return;
        }

        if (!mobile.matches(
                "^[6-9][0-9]{9}$"
        )) {

            etStaffMobile.setError(
                    "Enter valid 10 digit mobile number"
            );

            etStaffMobile.requestFocus();

            return;
        }

        // ADD MODE
        if (!isEdit) {

            if (password.isEmpty()) {

                etPassword.setError(
                        "Password is required"
                );

                etPassword.requestFocus();

                return;
            }

            if (password.length() < 6) {

                etPassword.setError(
                        "Password must contain at least 6 characters"
                );

                etPassword.requestFocus();

                return;
            }

            if (confirmPassword.isEmpty()) {

                etConfirmPassword.setError(
                        "Confirm password"
                );

                etConfirmPassword.requestFocus();

                return;
            }

            if (!password.equals(confirmPassword)) {

                etConfirmPassword.setError(
                        "Passwords do not match"
                );

                etConfirmPassword.requestFocus();

                return;
            }
        }

        // EDIT MODE
        if (isEdit && !password.isEmpty()) {

            if (password.length() < 6) {

                etPassword.setError(
                        "Password must contain at least 6 characters"
                );

                etPassword.requestFocus();

                return;
            }

            if (!password.equals(confirmPassword)) {

                etConfirmPassword.setError(
                        "Passwords do not match"
                );

                etConfirmPassword.requestFocus();

                return;
            }
        }

        Intent oldIntent = getIntent();

        Intent intent =
                new Intent(
                        AddStaffStep5.this,
                        AddStaffStep6.class
                );

        Bundle extras =
                oldIntent.getExtras();

        if (extras != null) {
            intent.putExtras(extras);
        }

        intent.putExtra(
                "isEdit",
                isEdit
        );

        intent.putExtra(
                "staffId",
                staffId
        );

        intent.putExtra(
                "staffEmail",
                email
        );

        intent.putExtra(
                "staffPhone",
                mobile
        );

        // Password will only be carried in Add mode
        // or if user typed a new password.
        intent.putExtra(
                "staffPassword",
                password
        );

        startActivity(intent);
    }

    private String getString(
            DocumentSnapshot document,
            String key
    ) {

        Object value =
                document.get(key);

        if (value == null) {
            return "";
        }

        return String.valueOf(value);
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