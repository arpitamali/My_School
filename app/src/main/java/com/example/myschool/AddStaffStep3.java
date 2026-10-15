package com.example.myschool;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddStaffStep3 extends AppCompatActivity {

    private Spinner spStaffType;
    private Spinner spRole;

    private EditText etSubject;
    private EditText etAssignedClass;
    private EditText etAssignedDivision;

    private LinearLayout teacherAssignmentLayout;

    private ImageButton btnTopBack;
    private Button btnNext;

    private boolean isEdit = false;
    private String staffId = "";

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_add_staff_step3
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
        setupStaffTypeSpinner();
        setupStaffTypeListener();
        setupButtons();
        setupSystemBack();

        updateRolesAndVisibility();

        if (isEdit && staffId != null && !staffId.trim().isEmpty()) {
            loadExistingStaff();
        }
    }

    private void initViews() {

        spStaffType =
                findViewById(R.id.spStaffType);

        spRole =
                findViewById(R.id.spRole);

        etSubject =
                findViewById(R.id.etSubject);

        etAssignedClass =
                findViewById(R.id.etAssignedClass);

        etAssignedDivision =
                findViewById(R.id.etAssignedDivision);

        teacherAssignmentLayout =
                findViewById(
                        R.id.teacherAssignmentLayout
                );

        btnTopBack =
                findViewById(R.id.btnTopBack);

        btnNext =
                findViewById(R.id.btnNext);
    }

    private void setupStaffTypeSpinner() {

        String[] staffTypes = {
                "Select Staff Type",
                "Teacher",
                "Non-Teaching Staff",
                "Administrative Staff"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        staffTypes
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spStaffType.setAdapter(adapter);
    }

    private void setupStaffTypeListener() {

        spStaffType.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {
                        updateRolesAndVisibility();
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent
                    ) {
                    }
                }
        );
    }

    private void updateRolesAndVisibility() {

        String staffType = "";

        if (spStaffType.getSelectedItem() != null) {
            staffType =
                    spStaffType
                            .getSelectedItem()
                            .toString();
        }

        if (staffType.equals("Teacher")) {

            String[] roles = {
                    "Select Role",
                    "Teacher"
            };

            setRoleAdapter(roles);

            teacherAssignmentLayout.setVisibility(
                    View.VISIBLE
            );

        } else if (
                staffType.equals("Non-Teaching Staff")
        ) {

            String[] roles = {
                    "Select Role",
                    "Clerk",
                    "Accountant",
                    "Librarian",
                    "Lab Assistant",
                    "Peon",
                    "Security",
                    "Other"
            };

            setRoleAdapter(roles);

            teacherAssignmentLayout.setVisibility(
                    View.GONE
            );

        } else if (
                staffType.equals("Administrative Staff")
        ) {

            String[] roles = {
                    "Select Role",
                    "Principal",
                    "Vice Principal",
                    "Clerk",
                    "Accountant",
                    "Other"
            };

            setRoleAdapter(roles);

            teacherAssignmentLayout.setVisibility(
                    View.GONE
            );

        } else {

            setRoleAdapter(
                    new String[]{
                            "Select Role"
                    }
            );

            teacherAssignmentLayout.setVisibility(
                    View.GONE
            );
        }
    }

    private void setRoleAdapter(
            String[] roles
    ) {

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        roles
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spRole.setAdapter(adapter);
    }

    private void setupButtons() {

        btnTopBack.setOnClickListener(
                v -> finish()
        );

        btnNext.setOnClickListener(
                v -> {

                    if (!validateForm()) {
                        return;
                    }

                    goToNextStep();
                }
        );
    }

    private boolean validateForm() {

        String staffType =
                spStaffType.getSelectedItem()
                        .toString();

        String role =
                spRole.getSelectedItem()
                        .toString();

        if (
                staffType.equals("Select Staff Type")
        ) {

            Toast.makeText(
                    this,
                    "Please select staff type",
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }

        if (
                role.equals("Select Role")
        ) {

            Toast.makeText(
                    this,
                    "Please select role",
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }

        if (staffType.equals("Teacher")) {

            String subject =
                    etSubject.getText()
                            .toString()
                            .trim();

            if (subject.isEmpty()) {

                etSubject.setError(
                        "Subject is required"
                );

                etSubject.requestFocus();

                return false;
            }
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

                    String staffType =
                            getString(
                                    document,
                                    "staffType"
                            );

                    String role =
                            getString(
                                    document,
                                    "role"
                            );

                    String subject =
                            getString(
                                    document,
                                    "subject"
                            );

                    String assignedClass =
                            getString(
                                    document,
                                    "assignedClass"
                            );

                    String assignedDivision =
                            getString(
                                    document,
                                    "assignedDivision"
                            );

                    setSpinnerSelection(
                            spStaffType,
                            staffType
                    );

                    spStaffType.post(() -> {

                        updateRolesAndVisibility();

                        setSpinnerSelection(
                                spRole,
                                role
                        );

                        etSubject.setText(subject);
                        etAssignedClass.setText(
                                assignedClass
                        );
                        etAssignedDivision.setText(
                                assignedDivision
                        );
                    });

                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Unable to load role information.",
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
                        AddStaffStep3.this,
                        AddStaffStep4.class
                );

        Bundle extras =
                oldIntent.getExtras();

        if (extras != null) {
            intent.putExtras(extras);
        }

        intent.putExtra(
                "isEdit",
                isEdit
        );

        intent.putExtra(
                "staffId",
                staffId
        );

        intent.putExtra(
                "staffType",
                spStaffType.getSelectedItem()
                        .toString()
        );

        intent.putExtra(
                "role",
                spRole.getSelectedItem()
                        .toString()
        );

        intent.putExtra(
                "subject",
                etSubject.getText()
                        .toString()
                        .trim()
        );

        intent.putExtra(
                "assignedClass",
                etAssignedClass.getText()
                        .toString()
                        .trim()
        );

        intent.putExtra(
                "assignedDivision",
                etAssignedDivision.getText()
                        .toString()
                        .trim()
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