package com.example.myschool;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SchoolInformationActivity
        extends AppCompatActivity {

    // =========================================================
    // VIEWS
    // =========================================================

    private ImageButton btnBack;

    private ImageView ivSchoolLogo;

    private TextView tvSchoolName;
    private TextView tvUdiseNumber;
    private TextView tvSchoolBranch;
    private TextView tvPrincipalName;
    private TextView tvSchoolAddress;


    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth auth;
    private FirebaseFirestore db;


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_school_information
        );


        // =====================================================
        // FIREBASE
        // =====================================================

        auth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();


        // =====================================================
        // INIT
        // =====================================================

        initViews();


        // =====================================================
        // BACK
        // =====================================================

        btnBack.setOnClickListener(
                v -> finish()
        );


        // =====================================================
        // LOAD SCHOOL
        // =====================================================

        loadSchoolInformation();
    }


    // =========================================================
    // INIT VIEWS
    // =========================================================

    private void initViews() {

        btnBack =
                findViewById(
                        R.id.btnBack
                );

        ivSchoolLogo =
                findViewById(
                        R.id.ivSchoolLogo
                );

        tvSchoolName =
                findViewById(
                        R.id.tvSchoolName
                );

        tvUdiseNumber =
                findViewById(
                        R.id.tvUdiseNumber
                );

        tvSchoolBranch =
                findViewById(
                        R.id.tvSchoolBranch
                );

        tvPrincipalName =
                findViewById(
                        R.id.tvPrincipalName
                );

        tvSchoolAddress =
                findViewById(
                        R.id.tvSchoolAddress
                );
    }


    // =========================================================
    // LOAD SCHOOL INFORMATION
    // =========================================================

    private void loadSchoolInformation() {

        if (
                auth.getCurrentUser() == null
        ) {

            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        String schoolId =
                auth.getCurrentUser()
                        .getUid();


        db.collection("schools")
                .document(schoolId)
                .get()
                .addOnSuccessListener(
                        document -> {

                            if (!document.exists()) {

                                Toast.makeText(
                                        this,
                                        "School information not found.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }


                            // =================================================
                            // SCHOOL NAME
                            // =================================================

                            String schoolName =
                                    getString(
                                            document,
                                            "schoolName"
                                    );


                            tvSchoolName.setText(
                                    safe(
                                            schoolName,
                                            "School Name"
                                    )
                            );


                            // =================================================
                            // UDISE NUMBER
                            // =================================================

                            String udise =
                                    getString(
                                            document,
                                            "udiseNumber"
                                    );


                            tvUdiseNumber.setText(
                                    safe(
                                            udise,
                                            "Not available"
                                    )
                            );


                            // =================================================
                            // SCHOOL BRANCH
                            // =================================================

                            String branch =
                                    getString(
                                            document,
                                            "schoolBranch"
                                    );


                            tvSchoolBranch.setText(
                                    safe(
                                            branch,
                                            "Not available"
                                    )
                            );


                            // =================================================
                            // PRINCIPAL
                            // =================================================

                            String principal =
                                    getString(
                                            document,
                                            "principalName"
                                    );


                            tvPrincipalName.setText(
                                    safe(
                                            principal,
                                            "Not available"
                                    )
                            );


                            // =================================================
                            // SCHOOL ADDRESS
                            // =================================================

                            String address =
                                    getString(
                                            document,
                                            "schoolAddress"
                                    );


                            if (
                                    address.isEmpty()
                            ) {

                                address =
                                        getString(
                                                document,
                                                "address"
                                        );
                            }


                            tvSchoolAddress.setText(
                                    safe(
                                            address,
                                            "Not available"
                                    )
                            );


                            // =================================================
                            // SCHOOL LOGO
                            // =================================================

                            String logoUri =
                                    getString(
                                            document,
                                            "schoolLogoUri"
                                    );


                            loadSchoolLogo(
                                    logoUri
                            );
                        }
                )
                .addOnFailureListener(
                        e ->
                                Toast.makeText(
                                        this,
                                        "Unable to load school information.",
                                        Toast.LENGTH_SHORT
                                ).show()
                );
    }


    // =========================================================
    // LOAD LOGO
    // =========================================================

    private void loadSchoolLogo(
            String logoUri
    ) {

        if (
                logoUri == null ||
                        logoUri.trim().isEmpty()
        ) {

            ivSchoolLogo.setImageResource(
                    android.R.drawable.ic_menu_gallery
            );

            return;
        }


        try {

            ivSchoolLogo.setImageURI(
                    Uri.parse(
                            logoUri
                    )
            );

        } catch (Exception e) {

            ivSchoolLogo.setImageResource(
                    android.R.drawable.ic_menu_gallery
            );
        }
    }


    // =========================================================
    // GET STRING
    // =========================================================

    private String getString(
            com.google.firebase.firestore.DocumentSnapshot document,
            String key
    ) {

        Object value =
                document.get(key);


        if (value == null) {

            return "";
        }


        return String.valueOf(
                value
        ).trim();
    }


    // =========================================================
    // SAFE
    // =========================================================

    private String safe(
            String value,
            String fallback
    ) {

        if (
                value == null ||
                        value.trim().isEmpty()
        ) {

            return fallback;
        }


        return value.trim();
    }
}