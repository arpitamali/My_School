package com.example.myschool;

import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class TeacherIDCardActivity extends AppCompatActivity {

    // =========================================================
    // VIEWS
    // =========================================================

    private ImageButton btnBack;

    private ImageView ivTeacherPhoto;

    private TextView tvSchoolName;
    private TextView tvSchoolAddress;

    private TextView tvTeacherName;
    private TextView tvQualification;
    private TextView tvEmail;
    private TextView tvPhone;
    private TextView tvAddress;


    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseFirestore db;


    // =========================================================
    // STAFF ID
    // =========================================================

    private String staffId = "";


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_teacher_idcard);

        db = FirebaseFirestore.getInstance();


        // =====================================================
        // INIT VIEWS
        // =====================================================

        initViews();


        // =====================================================
        // BACK
        // =====================================================

        btnBack.setOnClickListener(
                v -> finish()
        );


        // =====================================================
        // GET STAFF ID
        // =====================================================

        staffId = getIntent().getStringExtra("staffId");


        if (staffId == null || staffId.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Staff ID not found.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }


        staffId = staffId.trim();


        // =====================================================
        // LOAD TEACHER
        // =====================================================

        loadTeacher();
    }


    // =========================================================
    // INIT VIEWS
    // =========================================================

    private void initViews() {

        btnBack = findViewById(R.id.btnBack);

        ivTeacherPhoto = findViewById(R.id.ivTeacherPhoto);

        tvSchoolName = findViewById(R.id.tvSchoolName);

        tvSchoolAddress = findViewById(R.id.tvSchoolAddress);

        tvTeacherName = findViewById(R.id.tvTeacherName);

        tvQualification = findViewById(R.id.tvQualification);

        tvEmail = findViewById(R.id.tvEmail);

        tvPhone = findViewById(R.id.tvPhone);

        tvAddress = findViewById(R.id.tvAddress);
    }


    // =========================================================
    // LOAD TEACHER
    // =========================================================

    private void loadTeacher() {

        db.collection("staff")
                .document(staffId)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {

                        Toast.makeText(
                                this,
                                "Teacher not found.",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }


                    // =================================================
                    // NAME
                    // =================================================

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
                            )
                                    .replaceAll("\\s+", " ")
                                    .trim();


                    if (fullName.isEmpty()) {

                        fullName = "Teacher";
                    }


                    tvTeacherName.setText(fullName);


                    // =================================================
                    // QUALIFICATION
                    // =================================================

                    String qualification =
                            safe(
                                    getString(
                                            document,
                                            "qualification"
                                    )
                            );


                    // =================================================
                    // EMAIL
                    // =================================================

                    String email =
                            safe(
                                    getString(
                                            document,
                                            "staffEmail"
                                    )
                            );


                    // =================================================
                    // PHONE
                    // =================================================

                    String phone =
                            safe(
                                    getString(
                                            document,
                                            "staffPhone"
                                    )
                            );


                    // =================================================
                    // ADDRESS
                    // =================================================

                    String address =
                            safe(
                                    buildTeacherAddress(document)
                            );


                    // =================================================
                    // SHOW DETAILS
                    // =================================================

                    tvQualification.setText(
                            Html.fromHtml(
                                    "<b>Qualification:</b> "
                                            + qualification
                            )
                    );


                    tvEmail.setText(
                            Html.fromHtml(
                                    "<b>Email:</b> "
                                            + email
                            )
                    );


                    tvPhone.setText(
                            Html.fromHtml(
                                    "<b>Phone:</b> "
                                            + phone
                            )
                    );


                    tvAddress.setText(
                            Html.fromHtml(
                                    "<b>Address:</b> "
                                            + address
                            )
                    );


                    // =================================================
                    // PHOTO
                    // =================================================

                    loadPhoto(
                            getString(
                                    document,
                                    "staffPhoto"
                            )
                    );


                    // =================================================
                    // SCHOOL
                    // =================================================

                    String schoolId =
                            getString(
                                    document,
                                    "schoolId"
                            );


                    loadSchool(schoolId);

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Unable to load teacher.",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }


    // =========================================================
    // LOAD SCHOOL
    // =========================================================

    private void loadSchool(String schoolId) {

        if (schoolId == null ||
                schoolId.trim().isEmpty()) {

            setDefaultSchool();

            return;
        }


        db.collection("schools")
                .document(schoolId)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {

                        setDefaultSchool();

                        return;
                    }


                    // =================================================
                    // SCHOOL NAME
                    // =================================================

                    String schoolName =
                            document.getString(
                                    "schoolName"
                            );


                    if (schoolName == null ||
                            schoolName.trim().isEmpty()) {

                        schoolName = "School Name";
                    }


                    // =================================================
                    // SCHOOL ADDRESS
                    // =================================================

                    String schoolAddress =
                            document.getString(
                                    "schoolAddress"
                            );


                    // =================================================
                    // FALLBACK: ADDRESS
                    // =================================================

                    if (schoolAddress == null ||
                            schoolAddress.trim().isEmpty()) {

                        schoolAddress =
                                document.getString(
                                        "address"
                                );
                    }


                    // =================================================
                    // FALLBACK: CITY / DISTRICT / STATE
                    // =================================================

                    if (schoolAddress == null ||
                            schoolAddress.trim().isEmpty()) {

                        String city =
                                document.getString(
                                        "city"
                                );

                        String district =
                                document.getString(
                                        "district"
                                );

                        String state =
                                document.getString(
                                        "state"
                                );


                        StringBuilder builder =
                                new StringBuilder();


                        if (city != null &&
                                !city.trim().isEmpty()) {

                            builder.append(city);
                        }


                        if (district != null &&
                                !district.trim().isEmpty()) {

                            if (builder.length() > 0) {

                                builder.append(", ");
                            }

                            builder.append(district);
                        }


                        if (state != null &&
                                !state.trim().isEmpty()) {

                            if (builder.length() > 0) {

                                builder.append(", ");
                            }

                            builder.append(state);
                        }


                        schoolAddress =
                                builder.toString();
                    }


                    if (schoolAddress == null ||
                            schoolAddress.trim().isEmpty()) {

                        schoolAddress = "School Address";
                    }


                    // =================================================
                    // SHOW SCHOOL DATA
                    // =================================================

                    tvSchoolName.setText(
                            schoolName
                    );

                    tvSchoolAddress.setText(
                            schoolAddress
                    );

                })
                .addOnFailureListener(
                        e -> setDefaultSchool()
                );
    }


    // =========================================================
    // DEFAULT SCHOOL
    // =========================================================

    private void setDefaultSchool() {

        tvSchoolName.setText(
                "School Name"
        );

        tvSchoolAddress.setText(
                "School Address"
        );
    }


    // =========================================================
    // PHOTO
    // =========================================================

    private void loadPhoto(String photo) {

        if (photo == null ||
                photo.trim().isEmpty()) {

            ivTeacherPhoto.setImageResource(
                    android.R.drawable.ic_menu_camera
            );

            return;
        }


        try {

            ivTeacherPhoto.setImageURI(
                    Uri.parse(photo)
            );

        } catch (Exception e) {

            ivTeacherPhoto.setImageResource(
                    android.R.drawable.ic_menu_camera
            );
        }
    }


    // =========================================================
    // TEACHER ADDRESS
    // =========================================================

    private String buildTeacherAddress(
            DocumentSnapshot document
    ) {

        String currentAddress =
                getString(
                        document,
                        "currentAddress"
                );


        String city =
                getString(
                        document,
                        "city"
                );


        String district =
                getString(
                        document,
                        "district"
                );


        String state =
                getString(
                        document,
                        "state"
                );


        String pinCode =
                getString(
                        document,
                        "pinCode"
                );


        StringBuilder builder =
                new StringBuilder();


        // =====================================================
        // CURRENT ADDRESS
        // =====================================================

        if (!currentAddress.trim().isEmpty()) {

            builder.append(
                    currentAddress.trim()
            );
        }


        // =====================================================
        // CITY
        // =====================================================

        if (!city.trim().isEmpty()) {

            if (builder.length() > 0) {

                builder.append(", ");
            }

            builder.append(
                    city.trim()
            );
        }


        // =====================================================
        // DISTRICT
        // =====================================================

        if (!district.trim().isEmpty()) {

            if (builder.length() > 0) {

                builder.append(", ");
            }

            builder.append(
                    district.trim()
            );
        }


        // =====================================================
        // STATE
        // =====================================================

        if (!state.trim().isEmpty()) {

            if (builder.length() > 0) {

                builder.append(", ");
            }

            builder.append(
                    state.trim()
            );
        }


        // =====================================================
        // PIN CODE
        // =====================================================

        if (!pinCode.trim().isEmpty()) {

            if (builder.length() > 0) {

                builder.append(" - ");
            }

            builder.append(
                    pinCode.trim()
            );
        }


        return builder.toString();
    }


    // =========================================================
    // GET STRING
    // =========================================================

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


    // =========================================================
    // SAFE
    // =========================================================

    private String safe(String value) {

        if (value == null ||
                value.trim().isEmpty()) {

            return "Not provided";
        }


        return value.trim();
    }
}