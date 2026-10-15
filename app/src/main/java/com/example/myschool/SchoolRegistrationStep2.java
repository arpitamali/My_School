package com.example.myschool;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class SchoolRegistrationStep2 extends AppCompatActivity {

    private ImageButton btnBack;

    private EditText etAdminName;
    private EditText etEmail;
    private EditText etMobile;
    private EditText etPassword;
    private EditText etConfirmPassword;

    private ImageView ivTogglePassword;
    private ImageView ivToggleConfirmPassword;

    private Button btnNext;


    // STEP 1 DATA

    private String schoolName = "";
    private String udise = "";
    private String branch = "";
    private String principal = "";
    private String address = "";
    private String logoUri = "";


    // PASSWORD

    private boolean passwordVisible = false;
    private boolean confirmPasswordVisible = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_school_registration_step2
        );

        initViews();

        loadStep1Data();

        loadPreviousAdminData();

        setupPasswordToggle();

        setupBackButton();

        setupNextButton();

        setupSystemBackButton();
    }


    private void initViews() {

        btnBack = findViewById(R.id.btnBack);

        etAdminName = findViewById(R.id.etAdminName);

        etEmail = findViewById(R.id.etEmail);

        etMobile = findViewById(R.id.etMobile);

        etPassword = findViewById(R.id.etPassword);

        etConfirmPassword =
                findViewById(R.id.etConfirmPassword);

        ivTogglePassword =
                findViewById(R.id.ivTogglePassword);

        ivToggleConfirmPassword =
                findViewById(R.id.ivToggleConfirmPassword);

        btnNext = findViewById(R.id.btnNext);
    }


    private void loadStep1Data() {

        Intent intent = getIntent();

        schoolName =
                getSafeString(intent, "schoolName");

        udise =
                getSafeString(intent, "udise");

        branch =
                getSafeString(intent, "branch");

        principal =
                getSafeString(intent, "principal");

        address =
                getSafeString(intent, "address");

        logoUri =
                getSafeString(intent, "logoUri");
    }


    private void loadPreviousAdminData() {

        Intent intent = getIntent();

        String adminName =
                intent.getStringExtra("adminName");

        String email =
                intent.getStringExtra("email");

        String mobile =
                intent.getStringExtra("mobile");

        String password =
                intent.getStringExtra("password");


        if (adminName != null)
            etAdminName.setText(adminName);

        if (email != null)
            etEmail.setText(email);

        if (mobile != null)
            etMobile.setText(mobile);

        if (password != null) {

            etPassword.setText(password);

            etConfirmPassword.setText(password);
        }
    }


    private String getSafeString(
            Intent intent,
            String key) {

        String value =
                intent.getStringExtra(key);

        return value == null ? "" : value;
    }


    // =========================================================
    // PASSWORD EYE
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
                        R.drawable.ic_eye
                );

            } else {

                etPassword.setInputType(
                        InputType.TYPE_CLASS_TEXT |
                                InputType.TYPE_TEXT_VARIATION_PASSWORD
                );

                ivTogglePassword.setImageResource(
                        R.drawable.ic_eye_off
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
                        R.drawable.ic_eye
                );

            } else {

                etConfirmPassword.setInputType(
                        InputType.TYPE_CLASS_TEXT |
                                InputType.TYPE_TEXT_VARIATION_PASSWORD
                );

                ivToggleConfirmPassword.setImageResource(
                        R.drawable.ic_eye_off
                );
            }


            etConfirmPassword.setSelection(
                    etConfirmPassword.length()
            );
        });
    }


    // =========================================================
    // BACK
    // =========================================================

    private void setupBackButton() {

        btnBack.setOnClickListener(v ->
                goBackToStep1()
        );
    }


    private void setupSystemBackButton() {

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {

                        goBackToStep1();
                    }
                }
        );
    }


    private void goBackToStep1() {

        Intent intent = new Intent(
                SchoolRegistrationStep2.this,
                SchoolRegistrationStep1.class
        );


        intent.putExtra(
                "schoolName",
                schoolName
        );

        intent.putExtra(
                "udise",
                udise
        );

        intent.putExtra(
                "branch",
                branch
        );

        intent.putExtra(
                "principal",
                principal
        );

        intent.putExtra(
                "address",
                address
        );

        intent.putExtra(
                "logoUri",
                logoUri
        );


        intent.putExtra(
                "adminName",
                etAdminName.getText()
                        .toString()
                        .trim()
        );

        intent.putExtra(
                "email",
                etEmail.getText()
                        .toString()
                        .trim()
        );

        intent.putExtra(
                "mobile",
                etMobile.getText()
                        .toString()
                        .trim()
        );

        intent.putExtra(
                "password",
                etPassword.getText()
                        .toString()
        );


        startActivity(intent);

        finish();
    }


    // =========================================================
    // NEXT
    // =========================================================

    private void setupNextButton() {

        btnNext.setOnClickListener(v ->
                validateAndGoNext()
        );
    }


    private void validateAndGoNext() {

        String adminName =
                etAdminName.getText()
                        .toString()
                        .trim();

        String email =
                etEmail.getText()
                        .toString()
                        .trim();

        String mobile =
                etMobile.getText()
                        .toString()
                        .trim();

        String password =
                etPassword.getText()
                        .toString();

        String confirmPassword =
                etConfirmPassword.getText()
                        .toString();


        if (TextUtils.isEmpty(adminName)) {

            etAdminName.setError(
                    "Enter admin full name"
            );

            etAdminName.requestFocus();
            return;
        }


        if (TextUtils.isEmpty(email)) {

            etEmail.setError(
                    "Enter email address"
            );

            etEmail.requestFocus();
            return;
        }


        if (!Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            etEmail.setError(
                    "Enter valid email address"
            );

            etEmail.requestFocus();
            return;
        }


        if (TextUtils.isEmpty(mobile)) {

            etMobile.setError(
                    "Enter mobile number"
            );

            etMobile.requestFocus();
            return;
        }


        if (mobile.length() != 10) {

            etMobile.setError(
                    "Mobile number must be 10 digits"
            );

            etMobile.requestFocus();
            return;
        }


        if (TextUtils.isEmpty(password)) {

            etPassword.setError(
                    "Create password"
            );

            etPassword.requestFocus();
            return;
        }


        if (password.length() < 6) {

            etPassword.setError(
                    "Password must be at least 6 characters"
            );

            etPassword.requestFocus();
            return;
        }


        if (TextUtils.isEmpty(confirmPassword)) {

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


        if (TextUtils.isEmpty(logoUri)) {

            android.widget.Toast.makeText(
                    this,
                    "School logo is missing. Please go back and select it.",
                    android.widget.Toast.LENGTH_LONG
            ).show();

            return;
        }


        Intent intent = new Intent(
                SchoolRegistrationStep2.this,
                SchoolRegistrationStep3.class
        );


        intent.putExtra("schoolName", schoolName);

        intent.putExtra("udise", udise);

        intent.putExtra("branch", branch);

        intent.putExtra("principal", principal);

        intent.putExtra("address", address);

        intent.putExtra("logoUri", logoUri);


        intent.putExtra("adminName", adminName);

        intent.putExtra("email", email);

        intent.putExtra("mobile", mobile);

        intent.putExtra("password", password);


        startActivity(intent);
    }
}