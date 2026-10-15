package com.example.myschool;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class AddStaffStep6 extends AppCompatActivity {

    private ImageButton btnBack;

    private ImageView ivStaffPhoto;

    private TextView tvStaffName;
    private TextView tvPersonalInfo;
    private TextView tvProfessionalInfo;
    private TextView tvRoleInfo;
    private TextView tvAddressInfo;
    private TextView tvLoginInfo;

    private CheckBox checkConfirm;

    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_add_staff_step6
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

        checkConfirm =
                findViewById(R.id.checkConfirm);

        btnNext =
                findViewById(R.id.btnNext);
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

        String role =
                display(intent, "role");

        String subject =
                display(intent, "subject");

        String assignedClass =
                display(intent, "assignedClass");

        String assignedDivision =
                display(intent, "assignedDivision");

        if (
                staffType.equalsIgnoreCase("Teacher")
        ) {

            tvRoleInfo.setText(
                    "Staff Type: " +
                            staffType +

                            "\nRole: " +
                            role +

                            "\nSubject: " +
                            subject +

                            "\nAssigned Class: " +
                            assignedClass +

                            "\nAssigned Division: " +
                            assignedDivision
            );

        } else {

            tvRoleInfo.setText(
                    "Staff Type: " +
                            staffType +

                            "\nRole: " +
                            role
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

        btnNext.setOnClickListener(v -> {

            if (!checkConfirm.isChecked()) {

                Toast.makeText(
                        this,
                        "Please confirm the information first",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            Intent intent =
                    new Intent(
                            AddStaffStep6.this,
                            AddStaffStep7.class
                    );

            if (getIntent().getExtras() != null) {

                intent.putExtras(
                        getIntent().getExtras()
                );
            }

            startActivity(intent);
        });
    }

    private String getExtra(
            Intent intent,
            String key
    ) {

        String value =
                intent.getStringExtra(key);

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