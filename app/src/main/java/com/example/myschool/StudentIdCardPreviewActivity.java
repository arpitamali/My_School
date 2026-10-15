package com.example.myschool;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class StudentIdCardPreviewActivity extends AppCompatActivity {

    private ImageView ivStudentPhoto;

    private TextView tvSchoolName;
    private TextView tvSchoolAddress;

    private TextView tvStudentName;
    private TextView tvClass;
    private TextView tvRollNumber;
    private TextView tvAcademicYear;
    private TextView tvAddress;
    private TextView tvPhone;

    private FirebaseFirestore db;
    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_student_id_card_preview
        );

        db =
                FirebaseFirestore.getInstance();

        auth =
                FirebaseAuth.getInstance();

        initViews();

        setupBack();

        loadStudent();
    }


    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initViews() {

        ivStudentPhoto =
                findViewById(
                        R.id.ivStudentPhoto
                );


        tvSchoolName =
                findViewById(
                        R.id.tvSchoolName
                );


        tvSchoolAddress =
                findViewById(
                        R.id.tvSchoolAddress
                );


        tvStudentName =
                findViewById(
                        R.id.tvStudentName
                );


        tvClass =
                findViewById(
                        R.id.tvClass
                );


        tvRollNumber =
                findViewById(
                        R.id.tvRollNumber
                );


        tvAcademicYear =
                findViewById(
                        R.id.tvAcademicYear
                );


        tvAddress =
                findViewById(
                        R.id.tvAddress
                );


        tvPhone =
                findViewById(
                        R.id.tvPhone
                );
    }


    // =========================================================
    // BACK BUTTON
    // =========================================================

    private void setupBack() {

        ImageButton btnBack =
                findViewById(
                        R.id.btnBack
                );

        btnBack.setOnClickListener(
                v -> finish()
        );
    }


    // =========================================================
    // LOAD STUDENT
    // =========================================================

    private void loadStudent() {

        String studentId =
                getIntent().getStringExtra(
                        "studentId"
                );


        if (studentId == null ||
                studentId.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Student information missing",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }


        db.collection("students")
                .document(studentId)
                .get()
                .addOnSuccessListener(
                        document -> {

                            if (!document.exists()) {

                                Toast.makeText(
                                        this,
                                        "Student not found",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }


                            showStudent(
                                    document
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    "Failed to load student",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                );
    }


    // =========================================================
    // SHOW STUDENT
    // =========================================================

    private void showStudent(
            DocumentSnapshot document) {


        // -----------------------------------------------------
        // PHOTO
        // -----------------------------------------------------

        String studentPhoto =
                getString(
                        document,
                        "studentPhoto"
                );


        if (!studentPhoto.isEmpty()) {

            try {

                ivStudentPhoto.setImageURI(
                        Uri.parse(studentPhoto)
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


        // -----------------------------------------------------
        // NAME
        // -----------------------------------------------------

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


        String studentName =
                (
                        firstName + " " +
                                middleName + " " +
                                lastName
                ).trim();


        if (studentName.isEmpty()) {

            studentName = "Student";
        }


        tvStudentName.setText(
                studentName
        );


        // -----------------------------------------------------
        // CLASS
        // -----------------------------------------------------

        String className =
                getString(
                        document,
                        "className"
                );


        String division =
                getString(
                        document,
                        "division"
                );


        String classText =
                "Class " +
                        className +
                        " - " +
                        division;


        tvClass.setText(
                classText
        );


        // -----------------------------------------------------
        // ROLL NUMBER
        // -----------------------------------------------------

        String rollNumber =
                getString(
                        document,
                        "rollNumber"
                );


        tvRollNumber.setText(
                "Roll No: " +
                        (rollNumber.isEmpty()
                                ? "-"
                                : rollNumber)
        );


        // -----------------------------------------------------
        // ACADEMIC YEAR
        // -----------------------------------------------------

        String academicYear =
                getString(
                        document,
                        "academicYear"
                );


        tvAcademicYear.setText(
                "Academic Year: " +
                        (academicYear.isEmpty()
                                ? "-"
                                : academicYear)
        );


        // -----------------------------------------------------
        // CURRENT ADDRESS
        // -----------------------------------------------------

        String address =
                getString(
                        document,
                        "currentAddress"
                );


        tvAddress.setText(
                "Address: " +
                        (address.isEmpty()
                                ? "-"
                                : address)
        );


        // -----------------------------------------------------
        // CONTACT
        // -----------------------------------------------------

        String fatherMobile =
                getString(
                        document,
                        "fatherMobile"
                );


        tvPhone.setText(
                "Contact: " +
                        (fatherMobile.isEmpty()
                                ? "-"
                                : fatherMobile)
        );


        // -----------------------------------------------------
        // LOAD SCHOOL
        // -----------------------------------------------------

        loadSchool();
    }


    // =========================================================
    // LOAD SCHOOL
    // =========================================================

    private void loadSchool() {

        if (auth.getCurrentUser() == null) {

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

                                tvSchoolName.setText(
                                        "MY SCHOOL"
                                );

                                tvSchoolAddress.setText(
                                        "School Address"
                                );

                                return;
                            }


                            String schoolName =
                                    getString(
                                            document,
                                            "schoolName"
                                    );


                            String schoolAddress =
                                    getString(
                                            document,
                                            "schoolAddress"
                                    );


                            if (schoolName.isEmpty()) {

                                tvSchoolName.setText(
                                        "MY SCHOOL"
                                );

                            } else {

                                tvSchoolName.setText(
                                        schoolName
                                );
                            }


                            if (schoolAddress.isEmpty()) {

                                tvSchoolAddress.setText(
                                        "School Address"
                                );

                            } else {

                                tvSchoolAddress.setText(
                                        schoolAddress
                                );
                            }
                        }
                );
    }


    // =========================================================
    // GET STRING
    // =========================================================

    private String getString(
            DocumentSnapshot document,
            String key) {

        Object value =
                document.get(key);


        if (value == null) {

            return "";
        }


        return String.valueOf(
                value
        ).trim();
    }
}