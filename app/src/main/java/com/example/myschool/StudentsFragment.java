package com.example.myschool;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class StudentsFragment extends Fragment {

    // =========================================================
    // VIEWS
    // =========================================================

    private TextView tvTotalStudents;

    private MaterialCardView btnAddStudent;
    private MaterialCardView btnSeeStudents;
    private MaterialCardView btnAttendance;
    private MaterialCardView btnFees;
    private MaterialCardView btnIdCard;
    private MaterialCardView btnBonafide;
    private MaterialCardView btnReportCard;
    private MaterialCardView btnTransfer;


    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    // =========================================================
    // ON CREATE VIEW
    // =========================================================

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view =
                inflater.inflate(
                        R.layout.fragment_students,
                        container,
                        false
                );


        mAuth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();


        initViews(view);

        setupListeners();


        return view;
    }


    // =========================================================
    // ON RESUME
    // =========================================================

    @Override
    public void onResume() {

        super.onResume();

        loadStudentCount();
    }


    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initViews(View view) {

        tvTotalStudents =
                view.findViewById(
                        R.id.tvTotalStudents
                );


        btnAddStudent =
                view.findViewById(
                        R.id.btnAddStudent
                );


        btnSeeStudents =
                view.findViewById(
                        R.id.btnSeeStudents
                );


        btnAttendance =
                view.findViewById(
                        R.id.btnAttendance
                );


        btnFees =
                view.findViewById(
                        R.id.btnFees
                );


        btnIdCard =
                view.findViewById(
                        R.id.btnIdCard
                );


        btnBonafide =
                view.findViewById(
                        R.id.btnBonafide
                );


        btnReportCard =
                view.findViewById(
                        R.id.btnReportCard
                );


        btnTransfer =
                view.findViewById(
                        R.id.btnTransfer
                );
    }


    // =========================================================
    // STUDENT COUNT
    // =========================================================

    private void loadStudentCount() {

        if (!isAdded()) {
            return;
        }


        if (mAuth == null) {

            mAuth =
                    FirebaseAuth.getInstance();
        }


        if (db == null) {

            db =
                    FirebaseFirestore.getInstance();
        }


        if (mAuth.getCurrentUser() == null) {

            tvTotalStudents.setText(
                    "0"
            );

            return;
        }


        String schoolId =
                mAuth.getCurrentUser()
                        .getUid();


        // =====================================================
        // ACTUAL STUDENT COUNT
        // =====================================================

        db.collection("students")
                .whereEqualTo(
                        "schoolId",
                        schoolId
                )
                .get()
                .addOnSuccessListener(
                        querySnapshot -> {

                            if (!isAdded()) {
                                return;
                            }


                            int count =
                                    querySnapshot.size();


                            tvTotalStudents.setText(
                                    String.valueOf(
                                            count
                                    )
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }


                            tvTotalStudents.setText(
                                    "0"
                            );
                        }
                );
    }


    // =========================================================
    // LISTENERS
    // =========================================================

    private void setupListeners() {

        // =====================================================
        // ADD STUDENT
        // =====================================================

        btnAddStudent.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            requireActivity(),
                            AddStudentStep1.class
                    );


            startActivity(
                    intent
            );
        });


        // =====================================================
        // SEE STUDENTS
        // =====================================================

        btnSeeStudents.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            requireActivity(),
                            StudentListActivity.class
                    );

            startActivity(intent);
        });


        // =====================================================
        // ATTENDANCE
        // =====================================================

        btnAttendance.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            requireActivity(),
                            AllStudentAttendanceActivity.class
                    );


            startActivity(
                    intent
            );
        });


        // =====================================================
        // FEES
        // =====================================================

        btnFees.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            requireActivity(),
                            StudentFeesActivity.class
                    );


            startActivity(
                    intent
            );
        });


        // =====================================================
        // STUDENT ID CARD
        // =====================================================

        btnIdCard.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            requireActivity(),
                            StudentIdCardActivity.class
                    );


            startActivity(
                    intent
            );
        });


        // =====================================================
        // BONAFIDE CERTIFICATE
        // =====================================================

        btnBonafide.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            requireActivity(),
                            BonafideCertificateActivity.class
                    );


            startActivity(
                    intent
            );
        });


        // =====================================================
        // REPORT CARD
        // =====================================================

        // =====================================================
// REPORT CARD
// =====================================================

        btnReportCard.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            requireActivity(),
                            AllStudentReportCardActivity.class
                    );

            startActivity
                    (intent
                    );
        });


        // =====================================================
        // TRANSFER STUDENT
        // =====================================================

        btnTransfer.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            requireActivity(),
                            TransferStudentActivity.class
                    );


            startActivity(
                    intent
            );
        });
    }
}