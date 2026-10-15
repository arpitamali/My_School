package com.example.myschool;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddStaffStep4 extends AppCompatActivity {

    private NestedScrollView scrollView;

    private ImageButton btnBack;
    private Button btnNext;

    private EditText etCurrentAddress;
    private EditText etPermanentAddress;
    private EditText etCity;
    private EditText etDistrict;
    private EditText etState;
    private EditText etPinCode;

    private EditText etEmergencyName;
    private EditText etEmergencyRelation;
    private EditText etEmergencyMobile;

    private boolean isEdit = false;
    private String staffId = "";

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        );

        setContentView(
                R.layout.activity_add_staff_step4
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

        scrollView =
                findViewById(R.id.scrollView);

        btnBack =
                findViewById(R.id.btnBack);

        btnNext =
                findViewById(R.id.btnNext);

        etCurrentAddress =
                findViewById(R.id.etCurrentAddress);

        etPermanentAddress =
                findViewById(R.id.etPermanentAddress);

        etCity =
                findViewById(R.id.etCity);

        etDistrict =
                findViewById(R.id.etDistrict);

        etState =
                findViewById(R.id.etState);

        etPinCode =
                findViewById(R.id.etPinCode);

        etEmergencyName =
                findViewById(R.id.etEmergencyName);

        etEmergencyRelation =
                findViewById(R.id.etEmergencyRelation);

        etEmergencyMobile =
                findViewById(R.id.etEmergencyMobile);
    }

    private void setupListeners() {

        btnBack.setOnClickListener(v ->
                finish()
        );

        btnNext.setOnClickListener(v -> {

            if (!validateForm()) {
                return;
            }

            goToNextStep();
        });
    }

    private boolean validateForm() {

        String emergencyName =
                etEmergencyName.getText()
                        .toString()
                        .trim();

        String emergencyRelation =
                etEmergencyRelation.getText()
                        .toString()
                        .trim();

        String emergencyMobile =
                etEmergencyMobile.getText()
                        .toString()
                        .trim();

        String pinCode =
                etPinCode.getText()
                        .toString()
                        .trim();

        String currentAddress =
                etCurrentAddress.getText()
                        .toString()
                        .trim();

        if (currentAddress.isEmpty()) {
            etCurrentAddress.setError(
                    "Enter current address"
            );
            etCurrentAddress.requestFocus();
            return false;
        }

        if (emergencyName.isEmpty()) {
            etEmergencyName.setError(
                    "Emergency contact name is required"
            );
            etEmergencyName.requestFocus();
            return false;
        }

        if (emergencyRelation.isEmpty()) {
            etEmergencyRelation.setError(
                    "Relation is required"
            );
            etEmergencyRelation.requestFocus();
            return false;
        }

        if (emergencyMobile.isEmpty()) {
            etEmergencyMobile.setError(
                    "Emergency mobile is required"
            );
            etEmergencyMobile.requestFocus();
            return false;
        }

        if (!emergencyMobile.matches(
                "^[6-9][0-9]{9}$"
        )) {
            etEmergencyMobile.setError(
                    "Enter a valid 10 digit mobile number"
            );
            etEmergencyMobile.requestFocus();
            return false;
        }

        if (
                !pinCode.isEmpty()
                        &&
                        !pinCode.matches(
                                "^[0-9]{6}$"
                        )
        ) {
            etPinCode.setError(
                    "PIN code must contain 6 digits"
            );
            etPinCode.requestFocus();
            return false;
        }

        return true;
    }

    private void loadExistingStaff() {

        db.collection("staff")
                .document(staffId)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {
                        return;
                    }

                    etCurrentAddress.setText(
                            getString(
                                    document,
                                    "currentAddress"
                            )
                    );

                    etPermanentAddress.setText(
                            getString(
                                    document,
                                    "permanentAddress"
                            )
                    );

                    etCity.setText(
                            getString(
                                    document,
                                    "city"
                            )
                    );

                    etDistrict.setText(
                            getString(
                                    document,
                                    "district"
                            )
                    );

                    etState.setText(
                            getString(
                                    document,
                                    "state"
                            )
                    );

                    etPinCode.setText(
                            getString(
                                    document,
                                    "pinCode"
                            )
                    );

                    etEmergencyName.setText(
                            getString(
                                    document,
                                    "emergencyName"
                            )
                    );

                    etEmergencyRelation.setText(
                            getString(
                                    document,
                                    "emergencyRelation"
                            )
                    );

                    etEmergencyMobile.setText(
                            getString(
                                    document,
                                    "emergencyMobile"
                            )
                    );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Unable to load address details.",
                                Toast.LENGTH_SHORT
                        ).show()
                );
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

    private void goToNextStep() {

        Intent oldIntent = getIntent();

        Intent intent =
                new Intent(
                        AddStaffStep4.this,
                        AddStaffStep5.class
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
                "currentAddress",
                etCurrentAddress.getText()
                        .toString()
                        .trim()
        );

        intent.putExtra(
                "permanentAddress",
                etPermanentAddress.getText()
                        .toString()
                        .trim()
        );

        intent.putExtra(
                "city",
                etCity.getText()
                        .toString()
                        .trim()
        );

        intent.putExtra(
                "district",
                etDistrict.getText()
                        .toString()
                        .trim()
        );

        intent.putExtra(
                "state",
                etState.getText()
                        .toString()
                        .trim()
        );

        intent.putExtra(
                "pinCode",
                etPinCode.getText()
                        .toString()
                        .trim()
        );

        intent.putExtra(
                "emergencyName",
                etEmergencyName.getText()
                        .toString()
                        .trim()
        );

        intent.putExtra(
                "emergencyRelation",
                etEmergencyRelation.getText()
                        .toString()
                        .trim()
        );

        intent.putExtra(
                "emergencyMobile",
                etEmergencyMobile.getText()
                        .toString()
                        .trim()
        );

        startActivity(intent);
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