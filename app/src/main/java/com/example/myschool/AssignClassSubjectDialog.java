package com.example.myschool;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssignClassSubjectDialog {

    // =========================================================
    // CONTEXT / DIALOG
    // =========================================================

    private final Context context;

    private Dialog dialog;

    // =========================================================
    // TEACHER
    // =========================================================

    private Spinner spinnerTeacher;

    // =========================================================
    // ASSIGN CLASS
    // =========================================================

    private Spinner spinnerAssignClass;
    private Spinner spinnerAssignDivision;

    // =========================================================
    // ASSIGN SUBJECT
    // =========================================================

    private EditText etAssignSubject;

    private Spinner spinnerSubjectClass;
    private Spinner spinnerSubjectDivision;

    // =========================================================
    // RADIO
    // =========================================================

    private RadioGroup rgAssignmentType;

    private RadioButton rbAssignClass;
    private RadioButton rbAssignSubject;

    // =========================================================
    // LAYOUTS
    // =========================================================

    private View assignClassLayout;
    private View assignSubjectLayout;

    // =========================================================
    // BUTTONS
    // =========================================================

    private TextView btnCancel;

    private MaterialButton btnAssign;

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth mAuth;

    private FirebaseFirestore db;

    // =========================================================
    // TEACHER LIST
    // =========================================================

    private final List<String> teacherNames =
            new ArrayList<>();

    private final List<String> teacherIds =
            new ArrayList<>();

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public AssignClassSubjectDialog(
            Context context
    ) {

        this.context = context;
    }

    // =========================================================
    // SHOW
    // =========================================================

    public void show() {

        dialog =
                new Dialog(context);

        dialog.requestWindowFeature(
                Window.FEATURE_NO_TITLE
        );

        dialog.setContentView(
                R.layout.activity_assign_class_subject_dialog
        );

        dialog.setCancelable(true);

        Window window =
                dialog.getWindow();

        if (window != null) {

            window.setBackgroundDrawable(
                    new ColorDrawable(
                            Color.TRANSPARENT
                    )
            );

            WindowManager.LayoutParams params =
                    window.getAttributes();

            params.width =
                    (int) (
                            context.getResources()
                                    .getDisplayMetrics()
                                    .widthPixels
                                    * 0.94
                    );

            params.height =
                    WindowManager.LayoutParams.WRAP_CONTENT;

            window.setAttributes(
                    params
            );
        }

        initViews();

        setupClass();

        setupDivision();

        setupTeacher();

        setupSubjectClass();

        setupSubjectDivision();

        setupListeners();

        // Default = Assign Class
        rbAssignClass.setChecked(
                true
        );

        showAssignClass();

        dialog.show();
    }

    // =========================================================
    // INIT VIEWS
    // =========================================================

    private void initViews() {

        spinnerTeacher =
                dialog.findViewById(
                        R.id.spinnerAssignTeacher
                );

        spinnerAssignClass =
                dialog.findViewById(
                        R.id.spinnerAssignClass
                );

        spinnerAssignDivision =
                dialog.findViewById(
                        R.id.spinnerAssignDivision
                );

        etAssignSubject =
                dialog.findViewById(
                        R.id.etAssignSubject
                );

        spinnerSubjectClass =
                dialog.findViewById(
                        R.id.spinnerSubjectClass
                );

        spinnerSubjectDivision =
                dialog.findViewById(
                        R.id.spinnerSubjectDivision
                );

        rgAssignmentType =
                dialog.findViewById(
                        R.id.rgAssignmentType
                );

        rbAssignClass =
                dialog.findViewById(
                        R.id.rbAssignClass
                );

        rbAssignSubject =
                dialog.findViewById(
                        R.id.rbAssignSubject
                );

        assignClassLayout =
                dialog.findViewById(
                        R.id.assignClassLayout
                );

        assignSubjectLayout =
                dialog.findViewById(
                        R.id.assignSubjectLayout
                );

        btnCancel =
                dialog.findViewById(
                        R.id.btnCancelAssign
                );

        btnAssign =
                dialog.findViewById(
                        R.id.btnAssign
                );

        mAuth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();
    }

    // =========================================================
    // CLASS
    // =========================================================

    private void setupClass() {

        List<String> classes =
                new ArrayList<>();

        classes.add(
                "Select Class"
        );

        for (
                int i = 1;
                i <= 8;
                i++
        ) {

            classes.add(
                    "Class " + i
            );
        }

        setSpinnerAdapter(
                spinnerAssignClass,
                classes
        );

        setSpinnerAdapter(
                spinnerSubjectClass,
                new ArrayList<>(classes)
        );
    }

    // =========================================================
    // DIVISION
    // =========================================================

    private void setupDivision() {

        List<String> divisions =
                new ArrayList<>();

        divisions.add(
                "Select Division"
        );

        divisions.add("A");
        divisions.add("B");
        divisions.add("C");
        divisions.add("D");
        divisions.add("E");

        setSpinnerAdapter(
                spinnerAssignDivision,
                divisions
        );

        setSpinnerAdapter(
                spinnerSubjectDivision,
                new ArrayList<>(divisions)
        );
    }

    // =========================================================
    // TEACHER
    // =========================================================

    private void setupTeacher() {

        teacherNames.clear();

        teacherIds.clear();

        teacherNames.add(
                "Loading Teachers..."
        );

        teacherIds.add(
                ""
        );

        setSpinnerAdapter(
                spinnerTeacher,
                teacherNames
        );

        if (
                mAuth == null
                        || mAuth.getCurrentUser() == null
        ) {

            Toast.makeText(
                    context,
                    "Please login again.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String schoolId =
                mAuth.getCurrentUser()
                        .getUid();

        db.collection(
                        "staff"
                )
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

                            teacherNames.clear();

                            teacherIds.clear();

                            teacherNames.add(
                                    "Select Teacher"
                            );

                            teacherIds.add(
                                    ""
                            );

                            for (
                                    com.google.firebase.firestore.DocumentSnapshot doc
                                    : snapshot.getDocuments()
                            ) {

                                StaffModel staff =
                                        doc.toObject(
                                                StaffModel.class
                                        );

                                if (staff == null) {
                                    continue;
                                }

                                String name =
                                        buildName(
                                                staff
                                        );

                                teacherNames.add(
                                        name
                                );

                                teacherIds.add(
                                        doc.getId()
                                );
                            }

                            setSpinnerAdapter(
                                    spinnerTeacher,
                                    teacherNames
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            teacherNames.clear();

                            teacherIds.clear();

                            teacherNames.add(
                                    "Unable to load teachers"
                            );

                            teacherIds.add(
                                    ""
                            );

                            setSpinnerAdapter(
                                    spinnerTeacher,
                                    teacherNames
                            );

                            Toast.makeText(
                                    context,
                                    "Unable to load teachers.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                );
    }

    // =========================================================
    // SUBJECT CLASS
    // =========================================================

    private void setupSubjectClass() {

        // No dynamic subject spinner.
        // Subject is now EditText.
    }

    // =========================================================
    // SUBJECT DIVISION
    // =========================================================

    private void setupSubjectDivision() {

        // No extra listener required.
    }

    // =========================================================
    // LISTENERS
    // =========================================================

    private void setupListeners() {

        // -----------------------------------------------------
        // RADIO
        // -----------------------------------------------------

        rgAssignmentType.setOnCheckedChangeListener(
                (group, checkedId) -> {

                    if (
                            checkedId ==
                                    R.id.rbAssignClass
                    ) {

                        showAssignClass();

                    } else if (
                            checkedId ==
                                    R.id.rbAssignSubject
                    ) {

                        showAssignSubject();
                    }
                }
        );

        // -----------------------------------------------------
        // CANCEL
        // -----------------------------------------------------

        btnCancel.setOnClickListener(
                v -> dialog.dismiss()
        );

        // -----------------------------------------------------
        // ASSIGN
        // -----------------------------------------------------

        btnAssign.setOnClickListener(
                v -> saveAssignment()
        );
    }

    // =========================================================
    // SHOW CLASS
    // =========================================================

    private void showAssignClass() {

        assignClassLayout.setVisibility(
                View.VISIBLE
        );

        assignSubjectLayout.setVisibility(
                View.GONE
        );
    }

    // =========================================================
    // SHOW SUBJECT
    // =========================================================

    private void showAssignSubject() {

        assignClassLayout.setVisibility(
                View.GONE
        );

        assignSubjectLayout.setVisibility(
                View.VISIBLE
        );
    }

    // =========================================================
    // SAVE ASSIGNMENT
    // =========================================================

    private void saveAssignment() {

        // =========================================================
        // LOGIN CHECK
        // =========================================================

        if (
                mAuth == null
                        || mAuth.getCurrentUser() == null
        ) {

            Toast.makeText(
                    context,
                    "Please login again.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // =========================================================
        // TEACHER
        // =========================================================

        int teacherPosition =
                spinnerTeacher.getSelectedItemPosition();

        if (
                teacherPosition <= 0
                        || teacherPosition >= teacherIds.size()
        ) {

            Toast.makeText(
                    context,
                    "Please select teacher.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String teacherId =
                teacherIds.get(teacherPosition);

        String teacherName =
                teacherNames.get(teacherPosition);

        // =========================================================
        // VARIABLES
        // =========================================================

        String assignmentType;
        String className;
        String division;
        String subject;

        int classNumber;

        // =========================================================
        // ASSIGN CLASS
        // =========================================================

        if (rbAssignClass.isChecked()) {

            assignmentType = "CLASS";

            // -----------------------------------------------------
            // CLASS
            // -----------------------------------------------------

            int classPosition =
                    spinnerAssignClass.getSelectedItemPosition();

            if (classPosition <= 0) {

                Toast.makeText(
                        context,
                        "Please select class.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            classNumber = classPosition;

            className =
                    "Class " + classPosition;

            // -----------------------------------------------------
            // DIVISION
            // -----------------------------------------------------

            int divisionPosition =
                    spinnerAssignDivision.getSelectedItemPosition();

            if (divisionPosition <= 0) {

                Toast.makeText(
                        context,
                        "Please select division.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            division =
                    spinnerAssignDivision
                            .getSelectedItem()
                            .toString();

            subject = "";
        }

        // =========================================================
        // ASSIGN SUBJECT
        // =========================================================

        else if (rbAssignSubject.isChecked()) {

            assignmentType = "SUBJECT";

            // -----------------------------------------------------
            // SUBJECT
            // -----------------------------------------------------

            subject =
                    etAssignSubject
                            .getText()
                            .toString()
                            .trim();

            if (subject.isEmpty()) {

                etAssignSubject.setError(
                        "Subject is required"
                );

                etAssignSubject.requestFocus();

                Toast.makeText(
                        context,
                        "Please enter subject.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            // -----------------------------------------------------
            // CLASS
            // -----------------------------------------------------

            int classPosition =
                    spinnerSubjectClass.getSelectedItemPosition();

            if (classPosition <= 0) {

                Toast.makeText(
                        context,
                        "Please select class.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            classNumber = classPosition;

            className =
                    "Class " + classPosition;

            // -----------------------------------------------------
            // DIVISION
            // -----------------------------------------------------

            int divisionPosition =
                    spinnerSubjectDivision
                            .getSelectedItemPosition();

            if (divisionPosition <= 0) {

                Toast.makeText(
                        context,
                        "Please select division.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            division =
                    spinnerSubjectDivision
                            .getSelectedItem()
                            .toString();
        }

        else {

            Toast.makeText(
                    context,
                    "Please select assignment type.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // =========================================================
        // FINAL VARIABLES
        // =========================================================

        final String finalAssignmentType =
                assignmentType;

        final String finalClassName =
                className;

        final String finalDivision =
                division;

        final String finalSubject =
                subject;

        final int finalClassNumber =
                classNumber;

        // =========================================================
        // SCHOOL
        // =========================================================

        String schoolId =
                mAuth.getCurrentUser()
                        .getUid();

        // =========================================================
        // UPDATE STAFF DOCUMENT
        // =========================================================

        Map<String, Object> staffUpdate =
                new HashMap<>();

        staffUpdate.put(
                "assignedClass",
                finalClassName
        );

        staffUpdate.put(
                "assignedDivision",
                finalDivision
        );

        // Subject only when Assign Subject selected
        if (
                finalAssignmentType.equals("SUBJECT")
        ) {

            staffUpdate.put(
                    "subject",
                    finalSubject
            );
        }

        // =========================================================
        // BUTTON
        // =========================================================

        btnAssign.setEnabled(
                false
        );

        btnAssign.setText(
                "SAVING..."
        );

        // =========================================================
        // UPDATE STAFF
        // =========================================================

        db.collection("staff")
                .document(teacherId)
                .update(staffUpdate)
                .addOnSuccessListener(
                        unused -> {

                            // -------------------------------------------------
                            // ALSO SAVE ASSIGNMENT RECORD
                            // -------------------------------------------------

                            Map<String, Object> assignmentData =
                                    new HashMap<>();

                            assignmentData.put(
                                    "schoolId",
                                    schoolId
                            );

                            assignmentData.put(
                                    "teacherId",
                                    teacherId
                            );

                            assignmentData.put(
                                    "teacherName",
                                    teacherName
                            );

                            assignmentData.put(
                                    "assignmentType",
                                    finalAssignmentType
                            );

                            assignmentData.put(
                                    "className",
                                    finalClassName
                            );

                            assignmentData.put(
                                    "classNumber",
                                    finalClassNumber
                            );

                            assignmentData.put(
                                    "division",
                                    finalDivision
                            );

                            assignmentData.put(
                                    "subject",
                                    finalSubject
                            );

                            assignmentData.put(
                                    "active",
                                    true
                            );

                            assignmentData.put(
                                    "createdAt",
                                    FieldValue.serverTimestamp()
                            );

                            db.collection(
                                            "classSubjectAssignments"
                                    )
                                    .add(assignmentData)
                                    .addOnSuccessListener(
                                            documentReference -> {

                                                String message;

                                                if (
                                                        finalAssignmentType
                                                                .equals("CLASS")
                                                ) {

                                                    message =
                                                            "Class assigned successfully.";

                                                } else {

                                                    message =
                                                            "Subject assigned successfully.";
                                                }

                                                Toast.makeText(
                                                        context,
                                                        message,
                                                        Toast.LENGTH_SHORT
                                                ).show();

                                                dialog.dismiss();
                                            }
                                    )
                                    .addOnFailureListener(
                                            e -> {

                                                // Staff already updated
                                                btnAssign.setEnabled(
                                                        true
                                                );

                                                btnAssign.setText(
                                                        "ASSIGN"
                                                );

                                                Toast.makeText(
                                                        context,
                                                        "Staff updated, but assignment record failed.",
                                                        Toast.LENGTH_LONG
                                                ).show();
                                            }
                                    );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            btnAssign.setEnabled(
                                    true
                            );

                            btnAssign.setText(
                                    "ASSIGN"
                            );

                            Toast.makeText(
                                    context,
                                    "Unable to update staff: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }
    // =========================================================
    // SPINNER ADAPTER
    // =========================================================

    private void setSpinnerAdapter(
            Spinner spinner,
            List<String> items
    ) {

        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(
                        context,
                        android.R.layout.simple_spinner_item,
                        items
                ) {

                    @NonNull
                    @Override
                    public View getView(
                            int position,
                            View convertView,
                            android.view.ViewGroup parent
                    ) {

                        TextView text =
                                (TextView)
                                        super.getView(
                                                position,
                                                convertView,
                                                parent
                                        );

                        text.setTextColor(
                                Color.rgb(
                                        50,
                                        45,
                                        60
                                )
                        );

                        text.setTextSize(
                                14
                        );

                        text.setPadding(
                                12,
                                0,
                                12,
                                0
                        );

                        return text;
                    }

                    @Override
                    public View getDropDownView(
                            int position,
                            View convertView,
                            android.view.ViewGroup parent
                    ) {

                        TextView text =
                                (TextView)
                                        super.getDropDownView(
                                                position,
                                                convertView,
                                                parent
                                        );

                        text.setTextColor(
                                Color.DKGRAY
                        );

                        text.setTextSize(
                                14
                        );

                        text.setPadding(
                                12,
                                14,
                                12,
                                14
                        );

                        return text;
                    }
                };

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinner.setAdapter(
                adapter
        );
    }

    // =========================================================
    // BUILD NAME
    // =========================================================

    private String buildName(
            StaffModel staff
    ) {

        String first =
                safe(
                        staff.getFirstName()
                );

        String middle =
                safe(
                        staff.getMiddleName()
                );

        String last =
                safe(
                        staff.getLastName()
                );

        String name =
                (
                        first
                                + " "
                                + middle
                                + " "
                                + last
                ).trim();

        if (
                name.isEmpty()
        ) {

            return "Staff / Teacher";
        }

        return name;
    }

    // =========================================================
    // SAFE
    // =========================================================

    private String safe(
            String value
    ) {

        if (
                value == null
                        || value.trim().isEmpty()
        ) {

            return "";
        }

        return value.trim();
    }
}