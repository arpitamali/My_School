package com.example.myschool;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TeacherDashboardActivity extends AppCompatActivity {

    private BottomNavigationView teacherBottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setNavigationBarColor(
                android.graphics.Color.WHITE
        );
        setContentView(R.layout.activity_teacher_dashboard);
        getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        );
        teacherBottomNavigation =
                findViewById(R.id.teacherBottomNavigation);

        teacherBottomNavigation.setOnItemSelectedListener(item -> {

            Fragment selectedFragment;

            int itemId = item.getItemId();

            if (itemId == R.id.nav_teacher_home) {

                selectedFragment = new TeacherHomeFragment();

            } else if (itemId == R.id.nav_teacher_my_class) {

                selectedFragment = new MyClassFragment();

            } else if (itemId == R.id.nav_teacher_all_classes) {

                selectedFragment = new AllClassesFragment();

            } else if (itemId == R.id.nav_teacher_profile) {

                selectedFragment = new TeacherProfileFragment();

            } else {

                return false;
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.teacherFragmentContainer,
                            selectedFragment
                    )
                    .commit();

            return true;
        });

        // Open Home initially
        if (savedInstanceState == null) {

            teacherBottomNavigation.setSelectedItemId(
                    R.id.nav_teacher_home
            );
        }
    }
}