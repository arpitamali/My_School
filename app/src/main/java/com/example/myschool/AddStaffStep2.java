package com.example.myschool;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class AddStaffStep2 extends AppCompatActivity {

    private NestedScrollView scrollView;

    private ImageButton btnBack;
    private Button btnNext;

    private EditText etEmployeeId;
    private EditText etJoiningDate;
    private EditText etQualification;
    private EditText etExperience;

    private Spinner spEmploymentType;

    private boolean isEdit = false;
    private String staffId = "";

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        );

        setContentView(
                R.layout.activity_add_staff_step2
        );

        db = FirebaseFirestore.getInstance();

        isEdit =
                getIntent().getBooleanExtra(
                        "isEdit",
                        false
                );

        staffId =
                getIntent().getStringExtra(
                        "staffId"
                );

        initViews();
        setupSpinner();
        setupDatePicker();
        setupListeners();
        setupSystemBack();

        if (isEdit && staffId != null && !staffId.trim().isEmpty()) {
            loadExistingStaff();
        }
    }

    private void initViews() {

        scrollView =
                findViewById(R.id.scrollView);

        btnBack =
                findViewById(R.id.btnBack);

        btnNext =
                findViewById(R.id.btnNext);

        etEmployeeId =
                findViewById(R.id.etEmployeeId);

        etJoiningDate =
                findViewById(R.id.etJoiningDate);

        etQualification =
                findViewById(R.id.etQualification);

        etExperience =
                findViewById(R.id.etExperience);

        spEmploymentType =
                findViewById(R.id.spEmploymentType);
    }

    private void setupSpinner() {

        String[] employmentTypes = {
                "Select Employment Type",
                "Permanent",
                "Temporary",
                "Contract",
                "Part-Time",
                "Probation"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        employmentTypes
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spEmploymentType.setAdapter(adapter);
    }

    private void setupDatePicker() {

        etJoiningDate.setOnClickListener(v -> {

            Calendar calendar =
                    Calendar.getInstance();

            DatePickerDialog dialog =
                    new DatePickerDialog(
                            this,
                            (view, year, month, day) -> {

                                String date =
                                        String.format(
                                                "%02d/%02d/%04d",
                                                day,
                                                month + 1,
                                                year
                                        );

                                etJoiningDate.setText(date);
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                    );

            dialog.getDatePicker().setMaxDate(
                    System.currentTimeMillis()
            );

            dialog.show();
        });
    }

    private void setupListeners() {

        btnBack.setOnClickListener(v ->
                finish()
        );

        btnNext.setOnClickListener(v -> {

            if (!validateForm()) {
                return;
            }

            goToNextStep();
        });
    }

    private boolean validateForm() {

        String employeeId =
                etEmployeeId.getText()
                        .toString()
                        .trim();

        String joiningDate =
                etJoiningDate.getText()
                        .toString()
                        .trim();

        String qualification =
                etQualification.getText()
                        .toString()
                        .trim();

        if (employeeId.isEmpty()) {
            etEmployeeId.setError(
                    "Employee ID is required"
            );
            etEmployeeId.requestFocus();
            return false;
        }

        if (joiningDate.isEmpty()) {
            etJoiningDate.setError(
                    "Joining date is required"
            );
            etJoiningDate.requestFocus();
            return false;
        }

        if (qualification.isEmpty()) {
            etQualification.setError(
                    "Qualification is required"
            );
            etQualification.requestFocus();
            return false;
        }

        if (spEmploymentType.getSelectedItemPosition() == 0) {

            Toast.makeText(
                    this,
                    "Please select employment type",
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }

        return true;
    }

    private void loadExistingStaff() {

        db.collection("staff")
                .document(staffId)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {
                        return;
                    }

                    etEmployeeId.setText(
                            getString(document, "employeeId")
                    );

                    etJoiningDate.setText(
                            getString(document, "joiningDate")
                    );

                    etQualification.setText(
                            getString(document, "qualification")
                    );

                    etExperience.setText(
                            getString(document, "experience")
                    );

                    setSpinnerSelection(
                            spEmploymentType,
                            getString(
                                    document,
                                    "employmentType"
                            )
                    );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Unable to load professional information.",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void setSpinnerSelection(
            Spinner spinner,
            String value
    ) {

        if (value == null || value.trim().isEmpty()) {
            return;
        }

        ArrayAdapter adapter =
                (ArrayAdapter) spinner.getAdapter();

        int position =
                adapter.getPosition(value);

        if (position >= 0) {
            spinner.setSelection(position);
        }
    }

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

    private void goToNextStep() {

        Intent oldIntent = getIntent();

        Intent intent =
                new Intent(
                        AddStaffStep2.this,
                        AddStaffStep3.class
                );

        Bundle extras =
                oldIntent.getExtras();

        if (extras != null) {
            intent.putExtras(extras);
        }

        intent.putExtra("isEdit", isEdit);
        intent.putExtra("staffId", staffId);

        intent.putExtra(
                "employeeId",
                etEmployeeId.getText()
                        .toString()
                        .trim()
        );

        intent.putExtra(
                "joiningDate",
                etJoiningDate.getText()
                        .toString()
                        .trim()
        );

        intent.putExtra(
                "qualification",
                etQualification.getText()
                        .toString()
                        .trim()
        );

        intent.putExtra(
                "experience",
                etExperience.getText()
                        .toString()
                        .trim()
        );

        intent.putExtra(
                "employmentType",
                spEmploymentType.getSelectedItem()
                        .toString()
        );

        startActivity(intent);
    }

    private void setupSystemBack() {

        getOnBackPressedDispatcher()
                .addCallback(
                        this,
                        new OnBackPressedCallback(true) {
                            @Override
                            public void handleOnBackPressed() {
                                finish();
                            }
                        }
                );
    }
}