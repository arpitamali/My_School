package com.example.myschool;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TrialAccountInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trial_account_info);

        Button btnDemoAdmin = findViewById(R.id.btnDemoAdmin);
        Button btnDemoTeacher = findViewById(R.id.btnDemoTeacher);

        btnDemoAdmin.setOnClickListener(v -> openDemo("Admin"));
        btnDemoTeacher.setOnClickListener(v -> openDemo("Teacher"));
    }

    private void openDemo(String role) {
        Intent intent = new Intent(this, DemoDashboardActivity.class);
        intent.putExtra("demoRole", role);
        startActivity(intent);
    }
}
