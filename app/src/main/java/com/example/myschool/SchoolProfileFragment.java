package com.example.myschool;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;

public class SchoolProfileFragment extends Fragment {

    private MaterialCardView btnSchoolInformation;
    private MaterialCardView btnUpdateSchool;
    private MaterialCardView btnFacilities;
    private MaterialCardView btnAchievements;
    private MaterialCardView btnEnquiry;
    private MaterialCardView btnAdminAccount;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View view = inflater.inflate(
                R.layout.fragment_school_profile,
                container,
                false
        );

        initViews(view);
        setupListeners();

        return view;
    }

    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initViews(View view) {

        btnSchoolInformation =
                view.findViewById(
                        R.id.btnSchoolInformation
                );

        btnUpdateSchool =
                view.findViewById(
                        R.id.btnUpdateSchool
                );

        btnFacilities =
                view.findViewById(
                        R.id.btnFacilities
                );

        btnAchievements =
                view.findViewById(
                        R.id.btnAchievements
                );

        btnEnquiry =
                view.findViewById(
                        R.id.btnEnquiry
                );

        btnAdminAccount =
                view.findViewById(
                        R.id.btnAdminAccount
                );
    }

    // =========================================================
    // LISTENERS
    // =========================================================

    private void setupListeners() {

        // =====================================================
        // SCHOOL INFORMATION
        // =====================================================

        btnSchoolInformation.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            requireContext(),
                            SchoolInformationActivity.class
                    );

            startActivity(intent);
        });

        // =====================================================
        // UPDATE SCHOOL
        // =====================================================

        btnUpdateSchool.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            requireContext(),
                            UpdateSchoolActivity.class
                    );

            startActivity(intent);
        });

        // =====================================================
        // SCHOOL FACILITIES
        // =====================================================

        btnFacilities.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            requireContext(),
                            SchoolFacilitiesActivity.class
                    );

            startActivity(intent);
        });

        // =====================================================
        // ACHIEVEMENTS
        // =====================================================

        btnAchievements.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            requireContext(),
                            AchievementsActivity.class
                    );

            startActivity(intent);
        });

        // =====================================================
        // ENQUIRY
        // =====================================================

        btnEnquiry.setOnClickListener(v -> {

            Intent intent = new Intent(
                    requireContext(),
                    EnquiryActivity.class
            );

            startActivity(intent);
        });

        // =====================================================
        // ADMIN ACCOUNT
        // =====================================================

        btnAdminAccount.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            requireContext(),
                            AdminAccountActivity.class
                    );

            startActivity(intent);
        });
    }

    // =========================================================
    // TEMPORARY MESSAGE
    // =========================================================

    private void showMessage(
            String message
    ) {

        Toast.makeText(
                requireContext(),
                message,
                Toast.LENGTH_SHORT
        ).show();
    }
}