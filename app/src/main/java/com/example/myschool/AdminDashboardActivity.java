package com.example.myschool;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {

    // =========================================================
    // BOTTOM NAVIGATION
    // =========================================================

    private BottomNavigationView bottomNavigation;


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_admin_dashboard
        );


        // =====================================================
        // INITIALIZE
        // =====================================================

        bottomNavigation = findViewById(
                R.id.bottomNavigation
        );


        // =====================================================
        // DEFAULT FRAGMENT
        // =====================================================

        if (savedInstanceState == null) {

            loadFragment(
                    new HomeFragment()
            );

            bottomNavigation.setSelectedItemId(
                    R.id.nav_home
            );
        }


        // =====================================================
        // BOTTOM NAVIGATION CLICK
        // =====================================================

        bottomNavigation.setOnItemSelectedListener(
                item -> {

                    int id = item.getItemId();


                    // =================================================
                    // HOME
                    // =================================================

                    if (id == R.id.nav_home) {

                        loadFragment(
                                new HomeFragment()
                        );

                        return true;
                    }


                    // =================================================
                    // STUDENTS
                    // =================================================

                    else if (id == R.id.nav_students) {

                        loadFragment(
                                new StudentsFragment()
                        );

                        return true;
                    }


                    // =================================================
                    // TEACHERS / STAFF
                    // =================================================

                    else if (id == R.id.nav_teachers) {

                        loadFragment(
                                new TeachersFragment()
                        );

                        return true;
                    }


                    // =================================================
                    // SCHOOL PROFILE
                    // =================================================

                    else if (id == R.id.nav_profile) {

                        loadFragment(
                                new SchoolProfileFragment()
                        );

                        return true;
                    }


                    return false;
                }
        );
    }


    // =========================================================
    // LOAD FRAGMENT
    // =========================================================

    private void loadFragment(Fragment fragment) {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragmentContainer,
                        fragment
                )
                .commit();
    }


    // =========================================================
    // LOGOUT
    // =========================================================

    public void logoutAdmin() {

        // -----------------------------------------------------
        // Firebase login session logout
        // -----------------------------------------------------

        FirebaseAuth.getInstance().signOut();


        // -----------------------------------------------------
        // Clear local login session
        // -----------------------------------------------------

        getSharedPreferences(
                "MySchoolPrefs",
                MODE_PRIVATE
        )
                .edit()
                .clear()
                .apply();


        // -----------------------------------------------------
        // Open Login Activity
        // -----------------------------------------------------

        Intent intent = new Intent(
                AdminDashboardActivity.this,
                LoginActivity.class
        );


        // Remove dashboard from back stack
        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );


        startActivity(intent);

        finish();
    }
}