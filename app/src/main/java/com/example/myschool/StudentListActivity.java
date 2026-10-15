package com.example.myschool;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class StudentListActivity extends AppCompatActivity {

    // =====================================================
    // VIEWS
    // =====================================================

    private ImageButton btnBack;
    private TextView tvStudentCount;
    private TextView tvNoStudents;
    private LinearLayout studentContainer;
    private EditText etSearchStudent;


    // =====================================================
    // FIREBASE
    // =====================================================

    private FirebaseFirestore db;
    private FirebaseAuth auth;


    // =====================================================
    // STUDENTS
    // =====================================================

    private final List<DocumentSnapshot> studentList =
            new ArrayList<>();

    // Complete list from Firebase
    private final List<DocumentSnapshot> allStudents =
            new ArrayList<>();


    // =====================================================
    // ON CREATE
    // =====================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_student_list
        );


        db =
                FirebaseFirestore.getInstance();

        auth =
                FirebaseAuth.getInstance();


        initViews();

        setupBackButton();

        setupSearch();

        loadStudents();
    }


    // =====================================================
    // INIT VIEWS
    // =====================================================

    private void initViews() {

        btnBack =
                findViewById(
                        R.id.btnBack
                );


        tvStudentCount =
                findViewById(
                        R.id.tvStudentCount
                );


        tvNoStudents =
                findViewById(
                        R.id.tvNoStudents
                );


        studentContainer =
                findViewById(
                        R.id.studentContainer
                );


        etSearchStudent =
                findViewById(
                        R.id.etSearchStudent
                );
    }


    // =====================================================
    // BACK
    // =====================================================

    private void setupBackButton() {

        btnBack.setOnClickListener(
                v -> finish()
        );
    }


    // =====================================================
    // SEARCH
    // =====================================================

    private void setupSearch() {

        if (etSearchStudent == null) {
            return;
        }


        etSearchStudent.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }


                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        filterStudents(
                                s.toString()
                        );
                    }


                    @Override
                    public void afterTextChanged(
                            Editable s) {
                    }
                }
        );
    }


    // =====================================================
    // FILTER STUDENTS
    // =====================================================

    private void filterStudents(String query) {

        query =
                query == null
                        ? ""
                        : query.trim().toLowerCase();


        studentList.clear();


        // -------------------------------------------------
        // EMPTY SEARCH
        // -------------------------------------------------

        if (query.isEmpty()) {

            studentList.addAll(
                    allStudents
            );

        } else {

            // -------------------------------------------------
            // SEARCH EACH STUDENT
            // -------------------------------------------------

            for (DocumentSnapshot student :
                    allStudents) {

                if (student == null) {
                    continue;
                }


                String firstName =
                        getString(
                                student,
                                "firstName"
                        ).toLowerCase();


                String middleName =
                        getString(
                                student,
                                "middleName"
                        ).toLowerCase();


                String lastName =
                        getString(
                                student,
                                "lastName"
                        ).toLowerCase();


                String studentName =
                        (
                                firstName
                                        + " "
                                        + middleName
                                        + " "
                                        + lastName
                        )
                                .replaceAll(
                                        "\\s+",
                                        " "
                                )
                                .trim();


                String admissionNumber =
                        getString(
                                student,
                                "admissionNumber"
                        ).toLowerCase();


                String studentId =
                        student.getId()
                                .toLowerCase();


                String className =
                        getString(
                                student,
                                "className"
                        ).toLowerCase();


                String division =
                        getString(
                                student,
                                "division"
                        ).toLowerCase();


                String studentEmail =
                        getString(
                                student,
                                "studentEmail"
                        ).toLowerCase();


                String studentPhone =
                        getString(
                                student,
                                "studentPhone"
                        ).toLowerCase();


                if (
                        studentName.contains(query)
                                || firstName.contains(query)
                                || middleName.contains(query)
                                || lastName.contains(query)
                                || admissionNumber.contains(query)
                                || studentId.contains(query)
                                || className.contains(query)
                                || division.contains(query)
                                || studentEmail.contains(query)
                                || studentPhone.contains(query)
                ) {

                    studentList.add(
                            student
                    );
                }
            }
        }


        displayStudents();
    }


    // =====================================================
    // LOAD STUDENTS
    // =====================================================

    private void loadStudents() {

        if (auth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_SHORT
            ).show();

            showEmpty();

            return;
        }


        String schoolId =
                auth.getCurrentUser()
                        .getUid();


        db.collection("students")
                .whereEqualTo(
                        "schoolId",
                        schoolId
                )
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            studentList.clear();

                            allStudents.clear();


                            allStudents.addAll(
                                    snapshot.getDocuments()
                            );


                            studentList.addAll(
                                    allStudents
                            );


                            displayStudents();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            showEmpty();


                            Toast.makeText(
                                    this,
                                    "Failed to load students.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    // =====================================================
    // DISPLAY STUDENTS
    // =====================================================

    private void displayStudents() {

        studentContainer.removeAllViews();


        int count =
                studentList.size();


        // -------------------------------------------------
        // NO STUDENTS
        // -------------------------------------------------

        if (count == 0) {

            tvStudentCount.setText(
                    "0 Students"
            );


            tvNoStudents.setVisibility(
                    View.VISIBLE
            );


            return;
        }


        tvNoStudents.setVisibility(
                View.GONE
        );


        // -------------------------------------------------
        // COUNT
        // -------------------------------------------------

        String searchText = "";

        if (etSearchStudent != null) {

            searchText =
                    etSearchStudent
                            .getText()
                            .toString()
                            .trim();
        }


        if (!searchText.isEmpty()) {

            if (count == 1) {

                tvStudentCount.setText(
                        "1 Student found"
                );

            } else {

                tvStudentCount.setText(
                        count +
                                " Students found"
                );
            }

        } else {

            if (count == 1) {

                tvStudentCount.setText(
                        "1 Student"
                );

            } else {

                tvStudentCount.setText(
                        count +
                                " Students"
                );
            }
        }


        // -------------------------------------------------
        // ADD STUDENT CARDS
        // -------------------------------------------------

        for (DocumentSnapshot student :
                studentList) {

            addStudentCard(
                    student
            );
        }
    }


    // =====================================================
    // STUDENT CARD
    // =====================================================

    private void addStudentCard(
            DocumentSnapshot student) {


        LinearLayout card =
                new LinearLayout(this);


        card.setOrientation(
                LinearLayout.VERTICAL
        );


        card.setPadding(
                16,
                15,
                16,
                15
        );


        card.setBackgroundResource(
                R.drawable.fee_student_card
        );


        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );


        cardParams.setMargins(
                0,
                0,
                0,
                12
        );


        card.setLayoutParams(
                cardParams
        );


        // =====================================================
        // NAME
        // =====================================================

        String firstName =
                getString(
                        student,
                        "firstName"
                );


        String middleName =
                getString(
                        student,
                        "middleName"
                );


        String lastName =
                getString(
                        student,
                        "lastName"
                );


        String studentName =
                (
                        firstName +
                                " " +
                                middleName +
                                " " +
                                lastName
                ).trim();


        if (studentName.isEmpty()) {

            studentName =
                    getString(
                            student,
                            "studentName"
                    );
        }


        if (studentName.isEmpty()) {

            studentName =
                    "Student";
        }


        TextView tvName =
                new TextView(this);


        tvName.setText(
                studentName
        );


        tvName.setTextSize(
                18
        );


        tvName.setTextColor(
                Color.rgb(
                        35,
                        25,
                        60
                )
        );


        tvName.setTypeface(
                null,
                Typeface.BOLD
        );


        card.addView(
                tvName
        );


        // =====================================================
        // CLASS
        // =====================================================

        String className =
                getString(
                        student,
                        "className"
                );


        String division =
                getString(
                        student,
                        "division"
                );


        String classText =
                "Class: " +
                        className;


        if (!division.isEmpty()) {

            classText +=
                    " - " +
                            division;
        }


        TextView tvClass =
                new TextView(this);


        tvClass.setText(
                classText
        );


        tvClass.setTextSize(
                13
        );


        tvClass.setTextColor(
                Color.GRAY
        );


        tvClass.setPadding(
                0,
                4,
                0,
                6
        );


        card.addView(
                tvClass
        );


        // =====================================================
        // VIEW STUDENT
        // =====================================================

        TextView tvView =
                new TextView(this);


        tvView.setText(
                "VIEW STUDENT  ›"
        );


        tvView.setTextSize(
                13
        );


        tvView.setTypeface(
                null,
                Typeface.BOLD
        );


        tvView.setTextColor(
                Color.rgb(
                        75,
                        31,
                        168
                )
        );


        tvView.setGravity(
                Gravity.END
        );


        tvView.setPadding(
                0,
                8,
                0,
                2
        );


        card.addView(
                tvView
        );


        // =====================================================
        // CLICK
        // =====================================================

        View.OnClickListener listener =
                v -> openStudentDetails(
                        student
                );


        card.setOnClickListener(
                listener
        );


        tvView.setOnClickListener(
                listener
        );


        studentContainer.addView(
                card
        );
    }


    // =====================================================
    // OPEN STUDENT DETAILS
    // =====================================================

    private void openStudentDetails(
            DocumentSnapshot student) {


        String studentId =
                student.getId();


        if (studentId == null ||
                studentId.isEmpty()) {

            Toast.makeText(
                    this,
                    "Student ID missing.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        Intent intent =
                new Intent(
                        StudentListActivity.this,
                        StudentDetailsActivity.class
                );


        // Student ID
        intent.putExtra(
                "studentId",
                studentId
        );


        // Name
        intent.putExtra(
                "firstName",
                getString(
                        student,
                        "firstName"
                )
        );


        intent.putExtra(
                "middleName",
                getString(
                        student,
                        "middleName"
                )
        );


        intent.putExtra(
                "lastName",
                getString(
                        student,
                        "lastName"
                )
        );


        // Photo
        intent.putExtra(
                "studentPhoto",
                getString(
                        student,
                        "studentPhoto"
                )
        );


        // Personal
        intent.putExtra(
                "dateOfBirth",
                getString(
                        student,
                        "dateOfBirth"
                )
        );


        intent.putExtra(
                "gender",
                getString(
                        student,
                        "gender"
                )
        );


        intent.putExtra(
                "bloodGroup",
                getString(
                        student,
                        "bloodGroup"
                )
        );


        intent.putExtra(
                "aadhaar",
                getString(
                        student,
                        "aadhaar"
                )
        );


        intent.putExtra(
                "birthPlace",
                getString(
                        student,
                        "birthPlace"
                )
        );


        intent.putExtra(
                "nationality",
                getString(
                        student,
                        "nationality"
                )
        );


        intent.putExtra(
                "motherTongue",
                getString(
                        student,
                        "motherTongue"
                )
        );


        intent.putExtra(
                "religion",
                getString(
                        student,
                        "religion"
                )
        );


        // Academic
        intent.putExtra(
                "className",
                getString(
                        student,
                        "className"
                )
        );


        intent.putExtra(
                "division",
                getString(
                        student,
                        "division"
                )
        );


        intent.putExtra(
                "academicYear",
                getString(
                        student,
                        "academicYear"
                )
        );


        // Parent
        intent.putExtra(
                "fatherName",
                getString(
                        student,
                        "fatherName"
                )
        );


        intent.putExtra(
                "fatherMobile",
                getString(
                        student,
                        "fatherMobile"
                )
        );


        intent.putExtra(
                "fatherEmail",
                getString(
                        student,
                        "fatherEmail"
                )
        );


        intent.putExtra(
                "fatherOccupation",
                getString(
                        student,
                        "fatherOccupation"
                )
        );


        intent.putExtra(
                "motherName",
                getString(
                        student,
                        "motherName"
                )
        );


        intent.putExtra(
                "motherMobile",
                getString(
                        student,
                        "motherMobile"
                )
        );


        intent.putExtra(
                "motherEmail",
                getString(
                        student,
                        "motherEmail"
                )
        );


        intent.putExtra(
                "motherOccupation",
                getString(
                        student,
                        "motherOccupation"
                )
        );


        // Address
        intent.putExtra(
                "currentAddress",
                getString(
                        student,
                        "currentAddress"
                )
        );


        intent.putExtra(
                "permanentAddress",
                getString(
                        student,
                        "permanentAddress"
                )
        );


        intent.putExtra(
                "city",
                getString(
                        student,
                        "city"
                )
        );


        intent.putExtra(
                "district",
                getString(
                        student,
                        "district"
                )
        );


        intent.putExtra(
                "state",
                getString(
                        student,
                        "state"
                )
        );


        intent.putExtra(
                "pinCode",
                getString(
                        student,
                        "pinCode"
                )
        );


        // Fees
        intent.putExtra(
                "totalFees",
                getString(
                        student,
                        "totalFees"
                )
        );


        intent.putExtra(
                "paidFees",
                getString(
                        student,
                        "paidFees"
                )
        );


        intent.putExtra(
                "remainingFees",
                getString(
                        student,
                        "remainingFees"
                )
        );


        intent.putExtra(
                "paymentMode",
                getString(
                        student,
                        "paymentMode"
                )
        );


        // Login
        intent.putExtra(
                "studentEmail",
                getString(
                        student,
                        "studentEmail"
                )
        );


        intent.putExtra(
                "studentPhone",
                getString(
                        student,
                        "studentPhone"
                )
        );


        startActivity(
                intent
        );
    }


    // =====================================================
    // FIRESTORE STRING
    // =====================================================

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


    // =====================================================
    // EMPTY
    // =====================================================

    private void showEmpty() {

        studentList.clear();

        allStudents.clear();


        if (studentContainer != null) {

            studentContainer.removeAllViews();
        }


        if (tvStudentCount != null) {

            tvStudentCount.setText(
                    "0 Students"
            );
        }


        if (tvNoStudents != null) {

            tvNoStudents.setText(
                    "No students found"
            );

            tvNoStudents.setVisibility(
                    View.VISIBLE
            );
        }
    }
}