package com.example.myschool;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

public class SchoolRegistrationStep1 extends AppCompatActivity {

    private NestedScrollView scrollView;

    private ImageButton btnBack;

    private EditText etSchoolName;
    private EditText etUdise;
    private EditText etBranch;
    private EditText etPrincipal;
    private EditText etAddress;

    private Button btnSelectLogo;
    private Button btnNext;

    private String logoUri = "";


    // =========================================================
    // IMAGE PICKER
    // =========================================================

    private final ActivityResultLauncher<String[]> imagePicker =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    uri -> {

                        if (uri != null) {

                            try {

                                getContentResolver()
                                        .takePersistableUriPermission(
                                                uri,
                                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        );

                            } catch (Exception ignored) {
                            }

                            logoUri = uri.toString();

                            btnSelectLogo.setText(
                                    "✓ Image Selected"
                            );

                            Toast.makeText(
                                    this,
                                    "School logo selected",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // IMPORTANT:
        // Keyboard open झाल्यावर app resize होईल.
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        );

        setContentView(
                R.layout.activity_school_registration_step1
        );

        initViews();

        loadPreviousData();

        setupBackButton();

        setupListeners();

        setupSystemBack();

        setupKeyboardHandling();
    }


    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initViews() {

        scrollView =
                findViewById(R.id.scrollView);

        btnBack =
                findViewById(R.id.btnBack);

        etSchoolName =
                findViewById(R.id.etSchoolName);

        etUdise =
                findViewById(R.id.etUdise);

        etBranch =
                findViewById(R.id.etBranch);

        etPrincipal =
                findViewById(R.id.etPrincipal);

        etAddress =
                findViewById(R.id.etAddress);

        btnSelectLogo =
                findViewById(R.id.btnSelectLogo);

        btnNext =
                findViewById(R.id.btnNext);
    }


    // =========================================================
    // KEYBOARD HANDLING
    // =========================================================

    private void setupKeyboardHandling() {

        etSchoolName.setOnFocusChangeListener(
                (view, hasFocus) -> {

                    if (hasFocus) {

                        scrollToView(etSchoolName);
                    }
                }
        );


        etUdise.setOnFocusChangeListener(
                (view, hasFocus) -> {

                    if (hasFocus) {

                        scrollToView(etUdise);
                    }
                }
        );


        etBranch.setOnFocusChangeListener(
                (view, hasFocus) -> {

                    if (hasFocus) {

                        scrollToView(etBranch);
                    }
                }
        );


        etPrincipal.setOnFocusChangeListener(
                (view, hasFocus) -> {

                    if (hasFocus) {

                        scrollToView(etPrincipal);
                    }
                }
        );


        etAddress.setOnFocusChangeListener(
                (view, hasFocus) -> {

                    if (hasFocus) {

                        scrollToView(etAddress);
                    }
                }
        );
    }


    private void scrollToView(
            android.view.View view) {

        view.postDelayed(() -> {

            int[] location =
                    new int[2];

            view.getLocationOnScreen(
                    location
            );

            int screenHeight =
                    getResources()
                            .getDisplayMetrics()
                            .heightPixels;

            int keyboardApproxHeight =
                    (int) (
                            screenHeight * 0.40
                    );

            int visibleBottom =
                    screenHeight -
                            keyboardApproxHeight;

            int viewBottom =
                    location[1] +
                            view.getHeight();

            if (viewBottom > visibleBottom) {

                int difference =
                        viewBottom -
                                visibleBottom +
                                40;

                scrollView.smoothScrollBy(
                        0,
                        difference
                );
            }

        }, 350);
    }


    // =========================================================
    // LOAD PREVIOUS DATA
    // =========================================================

    private void loadPreviousData() {

        Intent intent =
                getIntent();


        String schoolName =
                intent.getStringExtra(
                        "schoolName"
                );

        String udise =
                intent.getStringExtra(
                        "udise"
                );

        String branch =
                intent.getStringExtra(
                        "branch"
                );

        String principal =
                intent.getStringExtra(
                        "principal"
                );

        String address =
                intent.getStringExtra(
                        "address"
                );

        String previousLogo =
                intent.getStringExtra(
                        "logoUri"
                );


        if (schoolName != null) {

            etSchoolName.setText(
                    schoolName
            );
        }


        if (udise != null) {

            etUdise.setText(
                    udise
            );
        }


        if (branch != null) {

            etBranch.setText(
                    branch
            );
        }


        if (principal != null) {

            etPrincipal.setText(
                    principal
            );
        }


        if (address != null) {

            etAddress.setText(
                    address
            );
        }


        if (previousLogo != null &&
                !previousLogo.isEmpty()) {

            logoUri =
                    previousLogo;

            btnSelectLogo.setText(
                    "✓ Image Selected"
            );
        }
    }


    // =========================================================
    // BACK BUTTON
    // =========================================================

    private void setupBackButton() {

        btnBack.setOnClickListener(
                v -> goBackToLogin()
        );
    }


    private void setupSystemBack() {

        getOnBackPressedDispatcher()
                .addCallback(
                        this,
                        new OnBackPressedCallback(true) {

                            @Override
                            public void handleOnBackPressed() {

                                goBackToLogin();
                            }
                        }
                );
    }


    private void goBackToLogin() {

        Intent intent =
                new Intent(
                        SchoolRegistrationStep1.this,
                        LoginActivity.class
                );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        startActivity(
                intent
        );

        finish();
    }


    // =========================================================
    // LISTENERS
    // =========================================================

    private void setupListeners() {

        // SELECT LOGO

        btnSelectLogo.setOnClickListener(
                v -> {

                    imagePicker.launch(
                            new String[]{
                                    "image/*"
                            }
                    );
                }
        );


        // NEXT

        btnNext.setOnClickListener(
                v -> validateAndGoNext()
        );
    }


    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateAndGoNext() {

        String schoolName =
                etSchoolName.getText()
                        .toString()
                        .trim();

        String udise =
                etUdise.getText()
                        .toString()
                        .trim();

        String branch =
                etBranch.getText()
                        .toString()
                        .trim();

        String principal =
                etPrincipal.getText()
                        .toString()
                        .trim();

        String address =
                etAddress.getText()
                        .toString()
                        .trim();


        // SCHOOL NAME

        if (schoolName.isEmpty()) {

            etSchoolName.setError(
                    "Enter school name"
            );

            etSchoolName.requestFocus();

            return;
        }


        // UDISE

        if (udise.isEmpty()) {

            etUdise.setError(
                    "Enter UDISE number"
            );

            etUdise.requestFocus();

            return;
        }


        if (udise.length() != 11) {

            etUdise.setError(
                    "UDISE number must be 11 digits"
            );

            etUdise.requestFocus();

            return;
        }


        // BRANCH

        if (branch.isEmpty()) {

            etBranch.setError(
                    "Enter school branch / location"
            );

            etBranch.requestFocus();

            return;
        }


        // PRINCIPAL

        if (principal.isEmpty()) {

            etPrincipal.setError(
                    "Enter principal name"
            );

            etPrincipal.requestFocus();

            return;
        }


        // ADDRESS

        if (address.isEmpty()) {

            etAddress.setError(
                    "Enter complete school address"
            );

            etAddress.requestFocus();

            return;
        }


        // LOGO

        if (logoUri.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please select school logo",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // =====================================================
        // GO TO STEP 2
        // =====================================================

        Intent intent =
                new Intent(
                        SchoolRegistrationStep1.this,
                        SchoolRegistrationStep2.class
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


        startActivity(
                intent
        );
    }
}