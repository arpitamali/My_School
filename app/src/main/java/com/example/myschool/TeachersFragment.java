package com.example.myschool;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


public class TeachersFragment extends Fragment {

    // =========================================================
    // VIEWS
    // =========================================================

    private TextView tvTotalTeachers;

    private MaterialCardView btnAddTeacher;
    private MaterialCardView btnSeeTeachers;
    private MaterialCardView btnTeacherAttendance;
    private MaterialCardView btnStaffMonthlyAttendance;
    private MaterialCardView btnAssignClass;
    private MaterialCardView btnSalary;
    private MaterialCardView btnTeacherId;
    private MaterialCardView btnSetSalary;


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
            @Nullable Bundle savedInstanceState
    ) {

        View view = inflater.inflate(
                R.layout.fragment_teachers,
                container,
                false
        );


        // =====================================================
        // FIREBASE
        // =====================================================

        mAuth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();


        // =====================================================
        // INITIALIZE
        // =====================================================

        initViews(view);


        // =====================================================
        // LISTENERS
        // =====================================================

        setupListeners();


        return view;
    }


    // =========================================================
    // ON RESUME
    // =========================================================

    @Override
    public void onResume() {

        super.onResume();

        loadActualStaffCount();
    }


    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initViews(
            View view
    ) {

        // -----------------------------------------------------
        // TOTAL
        // -----------------------------------------------------

        tvTotalTeachers =
                view.findViewById(
                        R.id.tvTotalTeachers
                );


        // -----------------------------------------------------
        // ADD TEACHER / STAFF
        // -----------------------------------------------------

        btnAddTeacher =
                view.findViewById(
                        R.id.btnAddTeacher
                );


        // -----------------------------------------------------
        // SEE TEACHERS / STAFF
        // -----------------------------------------------------

        btnSeeTeachers =
                view.findViewById(
                        R.id.btnSeeTeachers
                );


        // -----------------------------------------------------
        // ATTENDANCE
        // -----------------------------------------------------

        btnTeacherAttendance =
                view.findViewById(
                        R.id.btnTeacherAttendance
                );


        // -----------------------------------------------------
        // MONTHLY ATTENDANCE
        // -----------------------------------------------------

        btnStaffMonthlyAttendance =
                view.findViewById(
                        R.id.btnStaffMonthlyAttendance
                );


        // -----------------------------------------------------
        // ASSIGN CLASS & SUBJECT
        // -----------------------------------------------------

        btnAssignClass =
                view.findViewById(
                        R.id.btnAssignClass
                );


        // -----------------------------------------------------
        // SALARY / PAYMENT
        // -----------------------------------------------------

        btnSalary =
                view.findViewById(
                        R.id.btnSalary
                );


        // -----------------------------------------------------
        // TEACHER ID CARD
        // -----------------------------------------------------

        btnTeacherId =
                view.findViewById(
                        R.id.btnTeacherId
                );


        // -----------------------------------------------------
        // SET SALARY
        // -----------------------------------------------------

        btnSetSalary =
                view.findViewById(
                        R.id.btnSetSalary
                );
    }


    // =========================================================
    // STAFF COUNT
    // =========================================================

    private void loadActualStaffCount() {

        if (!isAdded()) {
            return;
        }


        // =====================================================
        // CHECK LOGIN
        // =====================================================

        if (mAuth == null ||
                mAuth.getCurrentUser() == null) {

            if (tvTotalTeachers != null) {

                tvTotalTeachers.setText(
                        "0"
                );
            }

            return;
        }


        // =====================================================
        // SCHOOL ID
        // =====================================================

        String schoolId =
                mAuth.getCurrentUser()
                        .getUid();


        // =====================================================
        // LOAD ACTIVE STAFF
        // =====================================================

        db.collection("staff")

                .whereEqualTo(
                        "schoolId",
                        schoolId
                )

                .whereEqualTo(
                        "active",
                        true
                )

                .get()

                .addOnSuccessListener(
                        snapshot -> {

                            if (!isAdded()) {
                                return;
                            }


                            int totalStaff =
                                    snapshot.size();


                            if (tvTotalTeachers != null) {

                                tvTotalTeachers.setText(
                                        String.valueOf(
                                                totalStaff
                                        )
                                );
                            }
                        }
                )

                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }


                            if (tvTotalTeachers != null) {

                                tvTotalTeachers.setText(
                                        "0"
                                );
                            }


                            Toast.makeText(
                                    requireContext(),
                                    "Unable to load staff count",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                );
    }


    // =========================================================
    // CLICK LISTENERS
    // =========================================================

    private void setupListeners() {


        // =====================================================
        // ADD TEACHER / STAFF
        // =====================================================

        if (btnAddTeacher != null) {

            btnAddTeacher.setOnClickListener(
                    v -> {

                        Intent intent =
                                new Intent(
                                        requireActivity(),
                                        AddStaffStep1.class
                                );

                        startActivity(
                                intent
                        );
                    }
            );
        }


        // =====================================================
        // SEE TEACHERS / STAFF
        // =====================================================

        if (btnSeeTeachers != null) {

            btnSeeTeachers.setOnClickListener(
                    v -> {

                        Intent intent =
                                new Intent(
                                        requireActivity(),
                                        SeeStaffActivity.class
                                );

                        startActivity(
                                intent
                        );
                    }
            );
        }


        // =====================================================
        // ATTENDANCE
        // =====================================================

        if (btnTeacherAttendance != null) {

            btnTeacherAttendance.setOnClickListener(
                    v -> {

                        Intent intent =
                                new Intent(
                                        requireActivity(),
                                        TeacherAttendanceActivity.class
                                );

                        startActivity(
                                intent
                        );
                    }
            );
        }


        // =====================================================
        // STAFF MONTHLY ATTENDANCE
        // =====================================================

        if (btnStaffMonthlyAttendance != null) {

            btnStaffMonthlyAttendance.setOnClickListener(
                    v -> {

                        Intent intent =
                                new Intent(
                                        requireActivity(),
                                        StaffMonthlyAttendanceActivity.class
                                );

                        startActivity(
                                intent
                        );
                    }
            );
        }


        // =====================================================
        // ASSIGN CLASS & SUBJECT
        // =====================================================

        if (btnAssignClass != null) {

            btnAssignClass.setOnClickListener(
                    v -> {

                        AssignClassSubjectDialog dialog =
                                new AssignClassSubjectDialog(
                                        requireContext()
                                );

                        dialog.show();
                    }
            );
        }


        // =====================================================
        // SALARY / PAYMENT
        // =====================================================

        if (btnSalary != null) {

            btnSalary.setOnClickListener(
                    v -> {

                        Intent intent =
                                new Intent(
                                        requireActivity(),
                                        SalaryPaymentActivity.class
                                );

                        startActivity(
                                intent
                        );
                    }
            );
        }


        // =====================================================
        // TEACHER ID CARD
        // =====================================================
        if (btnTeacherId != null) {

            btnTeacherId.setOnClickListener(v -> {

                Intent intent =
                        new Intent(
                                requireActivity(),
                                SelectTeacherForIdActivity.class
                        );

                intent.putExtra(
                        "idCardMode",
                        true
                );

                startActivity(intent);
            });
        }


        // =====================================================
        // SET SALARY
        // =====================================================

        if (btnSetSalary != null) {

            btnSetSalary.setOnClickListener(
                    v -> {

                        Intent intent =
                                new Intent(
                                        requireActivity(),
                                        SetSalaryActivity.class
                                );

                        startActivity(
                                intent
                        );
                    }
            );
        }
    }
}