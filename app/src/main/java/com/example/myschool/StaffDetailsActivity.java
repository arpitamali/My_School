package com.example.myschool;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class StaffDetailsActivity extends AppCompatActivity {

    private ImageButton btnBack;

    private ImageView ivProfile;
    private TextView tvName;
    private TextView tvEmployeeId;

    private TextView tvPersonalInfo;
    private TextView tvProfessionalInfo;
    private TextView tvRoleInfo;
    private TextView tvAddressInfo;
    private TextView tvEmergencyInfo;
    private TextView tvLoginInfo;

    private FirebaseFirestore db;

    private String staffId = "";

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_staff_details
        );

        db = FirebaseFirestore.getInstance();

        initViews();

        btnBack.setOnClickListener(
                v -> finish()
        );

        staffId =
                getIntent().getStringExtra(
                        "staffId"
                );

        if (
                staffId == null
                        ||
                        staffId.trim().isEmpty()
        ) {

            Toast.makeText(
                    this,
                    "Staff not found.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        loadStaff(
                staffId.trim()
        );
    }

    private void initViews() {

        btnBack =
                findViewById(
                        R.id.btnBack
                );

        ivProfile =
                findViewById(
                        R.id.ivProfile
                );

        tvName =
                findViewById(
                        R.id.tvName
                );

        tvEmployeeId =
                findViewById(
                        R.id.tvEmployeeId
                );

        tvPersonalInfo =
                findViewById(
                        R.id.tvPersonalInfo
                );

        tvProfessionalInfo =
                findViewById(
                        R.id.tvProfessionalInfo
                );

        tvRoleInfo =
                findViewById(
                        R.id.tvRoleInfo
                );

        tvAddressInfo =
                findViewById(
                        R.id.tvAddressInfo
                );

        tvEmergencyInfo =
                findViewById(
                        R.id.tvEmergencyInfo
                );

        tvLoginInfo =
                findViewById(
                        R.id.tvLoginInfo
                );
    }

    private void loadStaff(
            String staffId
    ) {

        db.collection("staff")
                .document(staffId)
                .get()
                .addOnSuccessListener(
                        document -> {

                            if (!document.exists()) {

                                Toast.makeText(
                                        this,
                                        "Staff not found.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            // =====================================================
                            // PHOTO
                            // =====================================================

                            loadPhoto(
                                    getString(
                                            document,
                                            "staffPhoto"
                                    )
                            );

                            // =====================================================
                            // NAME
                            // =====================================================

                            String firstName =
                                    getString(
                                            document,
                                            "firstName"
                                    );

                            String middleName =
                                    getString(
                                            document,
                                            "middleName"
                                    );

                            String lastName =
                                    getString(
                                            document,
                                            "lastName"
                                    );

                            String fullName =
                                    (
                                            firstName
                                                    + " "
                                                    + middleName
                                                    + " "
                                                    + lastName
                                    ).trim();

                            if (fullName.isEmpty()) {
                                fullName =
                                        "Staff / Teacher";
                            }

                            tvName.setText(
                                    fullName
                            );

                            tvEmployeeId.setText(
                                    "Employee ID: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "employeeId"
                                                    )
                                            )
                            );

                            // =====================================================
                            // PERSONAL INFORMATION
                            // =====================================================

                            tvPersonalInfo.setText(

                                    "First Name: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "firstName"
                                                    )
                                            )

                                            + "\nMiddle Name: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "middleName"
                                                    )
                                            )

                                            + "\nLast Name: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "lastName"
                                                    )
                                            )

                                            + "\nDate of Birth: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "dateOfBirth"
                                                    )
                                            )

                                            + "\nGender: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "gender"
                                                    )
                                            )

                                            + "\nBlood Group: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "bloodGroup"
                                                    )
                                            )

                                            + "\nAadhaar: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "aadhaar"
                                                    )
                                            )

                                            + "\nBirth Place: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "birthPlace"
                                                    )
                                            )

                                            + "\nNationality: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "nationality"
                                                    )
                                            )

                                            + "\nMother Tongue: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "motherTongue"
                                                    )
                                            )

                                            + "\nReligion: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "religion"
                                                    )
                                            )

                                            + "\nCategory: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "category"
                                                    )
                                            )

                                            + "\nCaste: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "caste"
                                                    )
                                            )

                                            + "\nSub-Caste: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "subCaste"
                                                    )
                                            )

                                            + "\nSpecial Needs: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "specialNeeds"
                                                    )
                                            )
                            );

                            // =====================================================
                            // PROFESSIONAL INFORMATION
                            // =====================================================

                            tvProfessionalInfo.setText(

                                    "Employee ID: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "employeeId"
                                                    )
                                            )

                                            + "\nJoining Date: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "joiningDate"
                                                    )
                                            )

                                            + "\nQualification: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "qualification"
                                                    )
                                            )

                                            + "\nExperience: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "experience"
                                                    )
                                            )

                                            + "\nEmployment Type: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "employmentType"
                                                    )
                                            )
                            );

                            // =====================================================
                            // ROLE INFORMATION
                            // =====================================================

                            tvRoleInfo.setText(

                                    "Staff Type: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "staffType"
                                                    )
                                            )

                                            + "\nRole: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "role"
                                                    )
                                            )

                                            + "\nSubject: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "subject"
                                                    )
                                            )

                                            + "\nAssigned Class: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "assignedClass"
                                                    )
                                            )

                                            + "\nAssigned Division: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "assignedDivision"
                                                    )
                                            )
                            );

                            // =====================================================
                            // ADDRESS
                            // =====================================================

                            tvAddressInfo.setText(

                                    "Current Address: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "currentAddress"
                                                    )
                                            )

                                            + "\n\nPermanent Address: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "permanentAddress"
                                                    )
                                            )

                                            + "\n\nCity / Village: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "city"
                                                    )
                                            )

                                            + "\nDistrict: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "district"
                                                    )
                                            )

                                            + "\nState: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "state"
                                                    )
                                            )

                                            + "\nPIN Code: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "pinCode"
                                                    )
                                            )
                            );

                            // =====================================================
                            // EMERGENCY CONTACT
                            // =====================================================

                            tvEmergencyInfo.setText(

                                    "Name: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "emergencyName"
                                                    )
                                            )

                                            + "\nRelation: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "emergencyRelation"
                                                    )
                                            )

                                            + "\nMobile: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "emergencyMobile"
                                                    )
                                            )
                            );

                            // =====================================================
                            // LOGIN INFORMATION
                            // =====================================================

                            tvLoginInfo.setText(

                                    "Email: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "staffEmail"
                                                    )
                                            )

                                            + "\nMobile: "
                                            +
                                            safe(
                                                    getString(
                                                            document,
                                                            "staffPhone"
                                                    )
                                            )
                            );
                        }
                )
                .addOnFailureListener(
                        e ->
                                Toast.makeText(
                                        this,
                                        "Unable to load staff details.",
                                        Toast.LENGTH_SHORT
                                ).show()
                );
    }

    private void loadPhoto(
            String photo
    ) {

        if (
                photo == null
                        ||
                        photo.trim().isEmpty()
        ) {

            ivProfile.setImageResource(
                    android.R.drawable.ic_menu_camera
            );

            return;
        }

        try {

            ivProfile.setImageURI(
                    Uri.parse(photo)
            );

        } catch (Exception e) {

            ivProfile.setImageResource(
                    android.R.drawable.ic_menu_camera
            );
        }
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

    private String safe(
            String value
    ) {

        if (
                value == null
                        ||
                        value.trim().isEmpty()
        ) {
            return "Not provided";
        }

        return value.trim();
    }
}