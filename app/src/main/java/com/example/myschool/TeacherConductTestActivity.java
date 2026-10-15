package com.example.myschool;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TeacherConductTestActivity extends AppCompatActivity {

    // ---------------------------------------------------------
    // VIEWS
    // ---------------------------------------------------------

    private Spinner spinnerClass;
    private Spinner spinnerSubject;

    private EditText etTestTopic;
    private EditText etTestDate;
    private EditText etTestTime;

    private Button btnConductTest;
    private AppCompatImageButton btnBack;

    private ProgressBar progressBar;

    private RecyclerView recyclerTests;

    // ---------------------------------------------------------
    // FIREBASE
    // ---------------------------------------------------------

    private FirebaseFirestore db;

    private String staffId;
    private String schoolId = "";

    // ---------------------------------------------------------
    // LISTS
    // ---------------------------------------------------------

    private final List<String> classList =
            new ArrayList<>();

    private final List<String> subjectList =
            new ArrayList<>();

    private final List<TestModel> testList =
            new ArrayList<>();

    // ---------------------------------------------------------
    // ADAPTERS
    // ---------------------------------------------------------

    private ArrayAdapter<String> classAdapter;
    private ArrayAdapter<String> subjectAdapter;

    private ConductTestAdapter testAdapter;

    private ListenerRegistration testListener;

    // ---------------------------------------------------------
    // ON CREATE
    // ---------------------------------------------------------

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_teacher_conduct_test
        );

        db = FirebaseFirestore.getInstance();

        staffId =
                getIntent().getStringExtra("staffId");

        initializeViews();
        setupSpinners();
        setupRecyclerView();
        setupClicks();

        loadTeacher();
    }

    // ---------------------------------------------------------
    // INITIALIZE VIEWS
    // ---------------------------------------------------------

    private void initializeViews() {

        btnBack =
                findViewById(
                        R.id.btnConductTestBack
                );

        spinnerClass =
                findViewById(
                        R.id.spinnerConductTestClass
                );

        spinnerSubject =
                findViewById(
                        R.id.spinnerConductTestSubject
                );

        etTestTopic =
                findViewById(
                        R.id.etConductTestTopic
                );

        etTestDate =
                findViewById(
                        R.id.etConductTestDate
                );

        etTestTime =
                findViewById(
                        R.id.etConductTestTime
                );

        btnConductTest =
                findViewById(
                        R.id.btnConductTest
                );

        progressBar =
                findViewById(
                        R.id.conductTestProgressBar
                );

        recyclerTests =
                findViewById(
                        R.id.recyclerConductTests
                );

        etTestDate.setFocusable(false);
        etTestDate.setClickable(true);

        etTestTime.setFocusable(false);
        etTestTime.setClickable(true);
    }

    // ---------------------------------------------------------
    // SPINNERS
    // ---------------------------------------------------------

    private void setupSpinners() {

        // CLASS SPINNER
        classAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        classList
                );

        classAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerClass.setAdapter(
                classAdapter
        );

        // SUBJECT SPINNER
        subjectAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        subjectList
                );

        subjectAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerSubject.setAdapter(
                subjectAdapter
        );

        // CLASS SELECTION
        spinnerClass.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {

                        if (position > 0) {

                            String selectedClass =
                                    classList.get(position);

                            loadSubjects(
                                    selectedClass
                            );

                        } else {

                            subjectList.clear();

                            subjectList.add(
                                    "Select Subject"
                            );

                            subjectAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent
                    ) {
                    }
                }
        );
    }

    // ---------------------------------------------------------
    // RECYCLER VIEW
    // ---------------------------------------------------------

    private void setupRecyclerView() {

        recyclerTests.setLayoutManager(
                new LinearLayoutManager(this)
        );

        testAdapter =
                new ConductTestAdapter(
                        this,
                        testList
                );

        recyclerTests.setAdapter(
                testAdapter
        );
    }

    // ---------------------------------------------------------
    // CLICK EVENTS
    // ---------------------------------------------------------

    private void setupClicks() {

        btnBack.setOnClickListener(
                v -> finish()
        );

        etTestDate.setOnClickListener(
                v -> showDatePicker()
        );

        etTestTime.setOnClickListener(
                v -> showTimePicker()
        );

        btnConductTest.setOnClickListener(
                v -> saveConductTest()
        );
    }

    // ---------------------------------------------------------
    // LOAD TEACHER
    // ---------------------------------------------------------

    private void loadTeacher() {

        if (TextUtils.isEmpty(staffId)) {

            Toast.makeText(
                    this,
                    "Teacher ID not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        db.collection("staff")
                .document(staffId)
                .get()
                .addOnSuccessListener(
                        documentSnapshot -> {

                            if (!documentSnapshot.exists()) {

                                Toast.makeText(
                                        this,
                                        "Teacher not found",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            schoolId =
                                    getStringValue(
                                            documentSnapshot,
                                            "schoolId"
                                    );

                            loadClasses();

                            listenToTests();
                        }
                )
                .addOnFailureListener(
                        e -> Toast.makeText(
                                this,
                                "Failed to load teacher",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    // ---------------------------------------------------------
    // LOAD CLASSES
    // ---------------------------------------------------------

    private void loadClasses() {

        classList.clear();

        classList.add(
                "Select Class"
        );

        if (TextUtils.isEmpty(schoolId)) {

            classAdapter.notifyDataSetChanged();

            return;
        }

        db.collection("students")
                .whereEqualTo(
                        "schoolId",
                        schoolId
                )
                .get()
                .addOnSuccessListener(
                        querySnapshot -> {

                            Set<String> uniqueClasses =
                                    new HashSet<>();

                            for (
                                    DocumentSnapshot doc :
                                    querySnapshot.getDocuments()
                            ) {

                                String status =
                                        getStringValue(
                                                doc,
                                                "status"
                                        );

                                if (
                                        "Deleted"
                                                .equalsIgnoreCase(
                                                        status
                                                )
                                ) {
                                    continue;
                                }

                                String className =
                                        getStringValue(
                                                doc,
                                                "className"
                                        );

                                if (
                                        !TextUtils.isEmpty(
                                                className
                                        )
                                ) {

                                    uniqueClasses.add(
                                            className
                                    );
                                }
                            }

                            classList.addAll(
                                    uniqueClasses
                            );

                            classAdapter.notifyDataSetChanged();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            classAdapter.notifyDataSetChanged();

                            Toast.makeText(
                                    this,
                                    "Unable to load classes",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                );
    }

    // ---------------------------------------------------------
    // LOAD SUBJECTS
    // ---------------------------------------------------------
    // Fixed subject spinner.
    // It does NOT depend only on staff collection.
    // ---------------------------------------------------------

    private void loadSubjects(
            String selectedClass
    ) {

        subjectList.clear();

        subjectList.add(
                "Select Subject"
        );

        // COMMON SUBJECTS
        String[] commonSubjects = {

                "English",
                "Marathi",
                "Hindi",
                "Mathematics",
                "Science",
                "Social Science",
                "Computer",
                "EVS",
                "Physical Education",
                "Art",
                "Other"
        };

        for (
                String subject :
                commonSubjects
        ) {

            if (
                    !subjectList.contains(
                            subject
                    )
            ) {

                subjectList.add(
                        subject
                );
            }
        }

        subjectAdapter.notifyDataSetChanged();

        // -----------------------------------------------------
        // ALSO CHECK STAFF COLLECTION FOR CUSTOM SUBJECTS
        // -----------------------------------------------------

        if (TextUtils.isEmpty(schoolId)) {
            return;
        }

        db.collection("staff")
                .whereEqualTo(
                        "schoolId",
                        schoolId
                )
                .get()
                .addOnSuccessListener(
                        querySnapshot -> {

                            for (
                                    DocumentSnapshot doc :
                                    querySnapshot.getDocuments()
                            ) {

                                String assignedClass =
                                        getStringValue(
                                                doc,
                                                "assignedClass"
                                        );

                                if (
                                        !normalizeClass(
                                                assignedClass
                                        ).equals(
                                                normalizeClass(
                                                        selectedClass
                                                )
                                        )
                                ) {
                                    continue;
                                }

                                String subject =
                                        getStringValue(
                                                doc,
                                                "subject"
                                        );

                                if (
                                        !TextUtils.isEmpty(
                                                subject
                                        )
                                                &&
                                                !subjectList.contains(
                                                        subject
                                                )
                                ) {

                                    subjectList.add(
                                            subject
                                    );
                                }
                            }

                            subjectAdapter.notifyDataSetChanged();
                        }
                )
                .addOnFailureListener(
                        e -> subjectAdapter.notifyDataSetChanged()
                );
    }

    // ---------------------------------------------------------
    // DATE PICKER
    // ---------------------------------------------------------

    private void showDatePicker() {

        Calendar calendar =
                Calendar.getInstance();

        DatePickerDialog dialog =
                new DatePickerDialog(
                        this,
                        (view, year, month, dayOfMonth) -> {

                            String date =
                                    String.format(
                                            Locale.getDefault(),
                                            "%02d/%02d/%04d",
                                            dayOfMonth,
                                            month + 1,
                                            year
                                    );

                            etTestDate.setText(
                                    date
                            );
                        },
                        calendar.get(
                                Calendar.YEAR
                        ),
                        calendar.get(
                                Calendar.MONTH
                        ),
                        calendar.get(
                                Calendar.DAY_OF_MONTH
                        )
                );

        dialog.show();
    }

    // ---------------------------------------------------------
    // TIME PICKER
    // ---------------------------------------------------------

    private void showTimePicker() {

        Calendar calendar =
                Calendar.getInstance();

        TimePickerDialog dialog =
                new TimePickerDialog(
                        this,
                        (view, hourOfDay, minute) -> {

                            Calendar selected =
                                    Calendar.getInstance();

                            selected.set(
                                    Calendar.HOUR_OF_DAY,
                                    hourOfDay
                            );

                            selected.set(
                                    Calendar.MINUTE,
                                    minute
                            );

                            SimpleDateFormat sdf =
                                    new SimpleDateFormat(
                                            "hh:mm a",
                                            Locale.getDefault()
                                    );

                            String time =
                                    sdf.format(
                                            selected.getTime()
                                    );

                            etTestTime.setText(
                                    time
                            );
                        },
                        calendar.get(
                                Calendar.HOUR_OF_DAY
                        ),
                        calendar.get(
                                Calendar.MINUTE
                        ),
                        false
                );

        dialog.show();
    }

    // ---------------------------------------------------------
    // SAVE CONDUCT TEST
    // ---------------------------------------------------------

    private void saveConductTest() {

        if (
                spinnerClass.getSelectedItemPosition()
                        <= 0
        ) {

            Toast.makeText(
                    this,
                    "Please select class",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (
                spinnerSubject.getSelectedItemPosition()
                        <= 0
        ) {

            Toast.makeText(
                    this,
                    "Please select subject",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String className =
                spinnerClass
                        .getSelectedItem()
                        .toString();

        String subject =
                spinnerSubject
                        .getSelectedItem()
                        .toString();

        String topic =
                etTestTopic
                        .getText()
                        .toString()
                        .trim();

        String testDate =
                etTestDate
                        .getText()
                        .toString()
                        .trim();

        String testTime =
                etTestTime
                        .getText()
                        .toString()
                        .trim();

        if (TextUtils.isEmpty(topic)) {

            etTestTopic.setError(
                    "Enter test topic"
            );

            etTestTopic.requestFocus();

            return;
        }

        if (TextUtils.isEmpty(testDate)) {

            Toast.makeText(
                    this,
                    "Please select test date",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (TextUtils.isEmpty(testTime)) {

            Toast.makeText(
                    this,
                    "Please select test time",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        progressBar.setVisibility(
                View.VISIBLE
        );

        btnConductTest.setEnabled(
                false
        );

        String testId =
                db.collection("conductTests")
                        .document()
                        .getId();

        Map<String, Object> data =
                new HashMap<>();

        data.put(
                "testId",
                testId
        );

        data.put(
                "schoolId",
                schoolId
        );

        data.put(
                "createdBy",
                staffId
        );

        data.put(
                "className",
                className
        );

        data.put(
                "subject",
                subject
        );

        data.put(
                "topic",
                topic
        );

        data.put(
                "testDate",
                testDate
        );

        data.put(
                "testTime",
                testTime
        );

        data.put(
                "createdAt",
                FieldValue.serverTimestamp()
        );

        db.collection("conductTests")
                .document(testId)
                .set(data)
                .addOnSuccessListener(
                        unused -> {

                            progressBar.setVisibility(
                                    View.GONE
                            );

                            btnConductTest.setEnabled(
                                    true
                            );

                            Toast.makeText(
                                    this,
                                    "Test added successfully",
                                    Toast.LENGTH_SHORT
                            ).show();

                            clearForm();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            progressBar.setVisibility(
                                    View.GONE
                            );

                            btnConductTest.setEnabled(
                                    true
                            );

                            Toast.makeText(
                                    this,
                                    "Failed: " +
                                            e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // ---------------------------------------------------------
    // REALTIME TEST LIST
    // ---------------------------------------------------------

    private void listenToTests() {

        if (TextUtils.isEmpty(schoolId)) {
            return;
        }

        if (testListener != null) {
            testListener.remove();
        }

        testListener =
                db.collection("conductTests")
                        .whereEqualTo(
                                "schoolId",
                                schoolId
                        )
                        .addSnapshotListener(
                                (querySnapshot, error) -> {

                                    if (error != null) {

                                        Toast.makeText(
                                                this,
                                                "Unable to load tests",
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        return;
                                    }

                                    testList.clear();

                                    if (
                                            querySnapshot != null
                                    ) {

                                        for (
                                                DocumentSnapshot doc :
                                                querySnapshot.getDocuments()
                                        ) {

                                            TestModel test =
                                                    new TestModel();

                                            test.testId =
                                                    getStringValue(
                                                            doc,
                                                            "testId"
                                                    );

                                            test.className =
                                                    getStringValue(
                                                            doc,
                                                            "className"
                                                    );

                                            test.subject =
                                                    getStringValue(
                                                            doc,
                                                            "subject"
                                                    );

                                            test.topic =
                                                    getStringValue(
                                                            doc,
                                                            "topic"
                                                    );

                                            test.testDate =
                                                    getStringValue(
                                                            doc,
                                                            "testDate"
                                                    );

                                            test.testTime =
                                                    getStringValue(
                                                            doc,
                                                            "testTime"
                                                    );

                                            testList.add(
                                                    test
                                            );
                                        }
                                    }

                                    testAdapter.notifyDataSetChanged();
                                }
                        );
    }

    // ---------------------------------------------------------
    // EDIT TEST
    // ---------------------------------------------------------

    public void editTest(
            TestModel test
    ) {

        if (
                test == null ||
                        TextUtils.isEmpty(
                                test.testId
                        )
        ) {
            return;
        }

        View dialogView =
                getLayoutInflater().inflate(
                        R.layout.dialog_edit_conduct_test,
                        null
                );

        Spinner editSubject =
                dialogView.findViewById(
                        R.id.spinnerEditTestSubject
                );

        EditText editTopic =
                dialogView.findViewById(
                        R.id.etEditTestTopic
                );

        EditText editDate =
                dialogView.findViewById(
                        R.id.etEditTestDate
                );

        EditText editTime =
                dialogView.findViewById(
                        R.id.etEditTestTime
                );

        // -----------------------------------------------------
        // SUBJECT LIST
        // -----------------------------------------------------

        ArrayList<String> editSubjects =
                new ArrayList<>();

        editSubjects.add(
                "Select Subject"
        );

        String[] subjects = {

                "English",
                "Marathi",
                "Hindi",
                "Mathematics",
                "Science",
                "Social Science",
                "Computer",
                "EVS",
                "Physical Education",
                "Art",
                "Other"
        };

        for (
                String subject :
                subjects
        ) {

            editSubjects.add(
                    subject
            );
        }

        // If Firebase has custom subject
        if (
                !TextUtils.isEmpty(
                        test.subject
                )
                        &&
                        !editSubjects.contains(
                                test.subject
                        )
        ) {

            editSubjects.add(
                    test.subject
            );
        }

        ArrayAdapter<String> editSubjectAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        editSubjects
                );

        editSubjectAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        editSubject.setAdapter(
                editSubjectAdapter
        );

        int subjectPosition =
                editSubjects.indexOf(
                        test.subject
                );

        if (subjectPosition >= 0) {

            editSubject.setSelection(
                    subjectPosition
            );
        }

        // -----------------------------------------------------
        // SET EXISTING VALUES
        // -----------------------------------------------------

        editTopic.setText(
                test.topic
        );

        editDate.setText(
                test.testDate
        );

        editTime.setText(
                test.testTime
        );

        editDate.setFocusable(false);
        editDate.setClickable(true);

        editTime.setFocusable(false);
        editTime.setClickable(true);

        // -----------------------------------------------------
        // EDIT DATE
        // -----------------------------------------------------

        editDate.setOnClickListener(
                v -> {

                    Calendar calendar =
                            Calendar.getInstance();

                    DatePickerDialog dialog =
                            new DatePickerDialog(
                                    this,
                                    (view, year, month, day) -> {

                                        editDate.setText(
                                                String.format(
                                                        Locale.getDefault(),
                                                        "%02d/%02d/%04d",
                                                        day,
                                                        month + 1,
                                                        year
                                                )
                                        );
                                    },
                                    calendar.get(
                                            Calendar.YEAR
                                    ),
                                    calendar.get(
                                            Calendar.MONTH
                                    ),
                                    calendar.get(
                                            Calendar.DAY_OF_MONTH
                                    )
                            );

                    dialog.show();
                }
        );

        // -----------------------------------------------------
        // EDIT TIME
        // -----------------------------------------------------

        editTime.setOnClickListener(
                v -> {

                    Calendar calendar =
                            Calendar.getInstance();

                    TimePickerDialog dialog =
                            new TimePickerDialog(
                                    this,
                                    (view, hour, minute) -> {

                                        Calendar selected =
                                                Calendar.getInstance();

                                        selected.set(
                                                Calendar.HOUR_OF_DAY,
                                                hour
                                        );

                                        selected.set(
                                                Calendar.MINUTE,
                                                minute
                                        );

                                        SimpleDateFormat sdf =
                                                new SimpleDateFormat(
                                                        "hh:mm a",
                                                        Locale.getDefault()
                                                );

                                        editTime.setText(
                                                sdf.format(
                                                        selected.getTime()
                                                )
                                        );
                                    },
                                    calendar.get(
                                            Calendar.HOUR_OF_DAY
                                    ),
                                    calendar.get(
                                            Calendar.MINUTE
                                    ),
                                    false
                            );

                    dialog.show();
                }
        );

        // -----------------------------------------------------
        // DIALOG
        // -----------------------------------------------------

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                "Edit Test"
                        )
                        .setView(
                                dialogView
                        )
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Update",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                d -> {

                    Button updateButton =
                            dialog.getButton(
                                    AlertDialog.BUTTON_POSITIVE
                            );

                    updateButton.setOnClickListener(
                            v -> {

                                // SUBJECT
                                if (
                                        editSubject
                                                .getSelectedItemPosition()
                                                <= 0
                                ) {

                                    Toast.makeText(
                                            this,
                                            "Please select subject",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    return;
                                }

                                String newSubject =
                                        editSubject
                                                .getSelectedItem()
                                                .toString();

                                String newTopic =
                                        editTopic
                                                .getText()
                                                .toString()
                                                .trim();

                                String newDate =
                                        editDate
                                                .getText()
                                                .toString()
                                                .trim();

                                String newTime =
                                        editTime
                                                .getText()
                                                .toString()
                                                .trim();

                                if (
                                        TextUtils.isEmpty(
                                                newTopic
                                        )
                                ) {

                                    editTopic.setError(
                                            "Enter test topic"
                                    );

                                    editTopic.requestFocus();

                                    return;
                                }

                                if (
                                        TextUtils.isEmpty(
                                                newDate
                                        )
                                ) {

                                    Toast.makeText(
                                            this,
                                            "Please select date",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    return;
                                }

                                if (
                                        TextUtils.isEmpty(
                                                newTime
                                        )
                                ) {

                                    Toast.makeText(
                                            this,
                                            "Please select time",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    return;
                                }

                                // -------------------------------------------------
                                // FIRESTORE UPDATE
                                // -------------------------------------------------

                                Map<String, Object> update =
                                        new HashMap<>();

                                update.put(
                                        "subject",
                                        newSubject
                                );

                                update.put(
                                        "topic",
                                        newTopic
                                );

                                update.put(
                                        "testDate",
                                        newDate
                                );

                                update.put(
                                        "testTime",
                                        newTime
                                );

                                db.collection(
                                                "conductTests"
                                        )
                                        .document(
                                                test.testId
                                        )
                                        .update(
                                                update
                                        )
                                        .addOnSuccessListener(
                                                unused -> {

                                                    Toast.makeText(
                                                            this,
                                                            "Test updated successfully",
                                                            Toast.LENGTH_SHORT
                                                    ).show();

                                                    dialog.dismiss();
                                                }
                                        )
                                        .addOnFailureListener(
                                                e -> Toast.makeText(
                                                        this,
                                                        "Update failed: " +
                                                                e.getMessage(),
                                                        Toast.LENGTH_LONG
                                                ).show()
                                        );
                            }
                    );
                }
        );

        dialog.show();
    }

    // ---------------------------------------------------------
    // DELETE TEST
    // ---------------------------------------------------------

    public void deleteTest(
            TestModel test
    ) {

        if (
                test == null ||
                        TextUtils.isEmpty(
                                test.testId
                        )
        ) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(
                        "Delete Test"
                )
                .setMessage(
                        "Are you sure you want to delete this test?"
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> {

                            db.collection(
                                            "conductTests"
                                    )
                                    .document(
                                            test.testId
                                    )
                                    .delete()
                                    .addOnSuccessListener(
                                            unused -> Toast.makeText(
                                                    this,
                                                    "Test deleted successfully",
                                                    Toast.LENGTH_SHORT
                                            ).show()
                                    )
                                    .addOnFailureListener(
                                            e -> Toast.makeText(
                                                    this,
                                                    "Delete failed: " +
                                                            e.getMessage(),
                                                    Toast.LENGTH_LONG
                                            ).show()
                                    );
                        }
                )
                .show();
    }

    // ---------------------------------------------------------
    // CLEAR FORM
    // ---------------------------------------------------------

    private void clearForm() {

        spinnerClass.setSelection(
                0
        );

        spinnerSubject.setSelection(
                0
        );

        etTestTopic.setText("");

        etTestDate.setText("");

        etTestTime.setText("");
    }

    // ---------------------------------------------------------
    // GET STRING FROM FIRESTORE
    // ---------------------------------------------------------

    private String getStringValue(
            DocumentSnapshot doc,
            String field
    ) {

        Object value =
                doc.get(field);

        return value != null
                ? String.valueOf(value)
                : "";
    }

    // ---------------------------------------------------------
    // NORMALIZE CLASS
    // ---------------------------------------------------------

    private String normalizeClass(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .toLowerCase()
                .replace(
                        "class",
                        ""
                )
                .replace(
                        " ",
                        ""
                );
    }

    // ---------------------------------------------------------
    // DESTROY
    // ---------------------------------------------------------

    @Override
    protected void onDestroy() {

        if (testListener != null) {

            testListener.remove();

            testListener = null;
        }

        super.onDestroy();
    }

    // ---------------------------------------------------------
    // TEST MODEL
    // ---------------------------------------------------------

    public static class TestModel {

        String testId;

        String className;

        String subject;

        String topic;

        String testDate;

        String testTime;
    }
}